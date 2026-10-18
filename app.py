import os
import tempfile
import cv2
import numpy as np
from PIL import Image
import streamlit as st

# =========================================================
# PAGE CONFIGURATION / पेज कॉन्फ़िगरेशन
# =========================================================
st.set_page_config(
    page_title="Video Watermark Editor & Branding Tool",
    page_icon="🎬",
    layout="wide",
    initial_sidebar_state="expanded"
)

st.title("🎬 Video Watermark Editor & Branding Tool")
st.markdown("### पुराना वाटरमार्क हटाएँ/ब्लर करें और अपना नया पारदर्शी लोगो लगाएँ।")
st.caption("Remove or blur existing watermarks and overlay your custom transparent brand logo.")

# =========================================================
# HELPER FUNCTIONS / सहायक फ़ंक्शन
# =========================================================
def apply_blur(frame, x, y, w, h, blur_ksize=51):
    """
    ब्लर एरिया को बाउंड्री के भीतर सुरक्षित रूप से प्रोसेस करता है।
    Safely applies Gaussian blur within frame boundaries.
    """
    fh, fw = frame.shape[:2]
    x1, y1 = max(0, min(int(x), fw - 1)), max(0, min(int(y), fh - 1))
    x2, y2 = max(0, min(int(x + w), fw)), max(0, min(int(y + h), fh))

    if x2 > x1 and y2 > y1:
        roi = frame[y1:y2, x1:x2]
        k = int(blur_ksize) if int(blur_ksize) % 2 == 1 else int(blur_ksize) + 1
        k = max(3, k)
        blurred_roi = cv2.GaussianBlur(roi, (k, k), 0)
        frame[y1:y2, x1:x2] = blurred_roi
    return frame


def apply_logo(frame, logo_rgba, x, y, size_percent=15, opacity=1.0):
    """
    पारदर्शी PNG लोगो को अल्फा ब्लेंडिंग के साथ वीडियो फ्रेम पर लगाता है।
    Overlays transparent PNG logo with true alpha compositing and boundary clipping.
    """
    if logo_rgba is None:
        return frame

    fh, fw = frame.shape[:2]
    lh_orig, lw_orig = logo_rgba.shape[:2]

    # लोगो का आस्पेक्ट रेशियो बनाए रखते हुए रीसाइज़ करें
    aspect_ratio = lw_orig / float(lh_orig)
    new_w = max(10, int(fw * (size_percent / 100.0)))
    new_h = max(10, int(new_w / aspect_ratio))
    logo_resized = cv2.resize(logo_rgba, (new_w, new_h), interpolation=cv2.INTER_AREA)

    x1, y1 = int(x), int(y)
    x2, y2 = x1 + new_w, y1 + new_h

    # बाउंड्री क्लिपिंग (Boundary clipping)
    fx1 = max(0, x1)
    fy1 = max(0, y1)
    fx2 = min(fw, x2)
    fy2 = min(fh, y2)

    if fx2 <= fx1 or fy2 <= fy1:
        return frame

    lx1 = fx1 - x1
    ly1 = fy1 - y1
    lx2 = lx1 + (fx2 - fx1)
    ly2 = ly1 + (fy2 - fy1)

    logo_crop = logo_resized[ly1:ly2, lx1:lx2]

    # अल्फा चैनल एक्सट्रैक्शन और ब्लेंडिंग
    if logo_crop.shape[2] == 4:
        alpha = (logo_crop[:, :, 3] / 255.0) * float(opacity)
        alpha = np.dstack([alpha, alpha, alpha])
        fg = logo_crop[:, :, :3].astype(np.float32)
    else:
        alpha = np.ones((logo_crop.shape[0], logo_crop.shape[1], 3), dtype=np.float32) * float(opacity)
        fg = logo_crop[:, :, :3].astype(np.float32)

    bg = frame[fy1:fy2, fx1:fx2].astype(np.float32)
    blended = alpha * fg + (1.0 - alpha) * bg
    frame[fy1:fy2, fx1:fx2] = np.clip(blended, 0, 255).astype(np.uint8)

    return frame


# =========================================================
# FILE UPLOADERS / फाइल अपलोड
# =========================================================
col_up1, col_up2 = st.columns(2)
with col_up1:
    uploaded_video = st.file_uploader(
        "1. वीडियो अपलोड करें (MP4, MOV, AVI)",
        type=["mp4", "mov", "avi"],
        help="वह वीडियो चुनें जिससे वाटरमार्क हटाना है।"
    )

with col_up2:
    uploaded_logo = st.file_uploader(
        "2. अपना नया लोगो अपलोड करें (PNG transparent)",
        type=["png", "webp"],
        help="पारदर्शी बैकग्राउंड वाला PNG लोगो अपलोड करें।"
    )

# लोगो लोड करें
logo_img = None
if uploaded_logo is not None:
    pil_logo = Image.open(uploaded_logo).convert("RGBA")
    logo_img = np.array(pil_logo)

# =========================================================
# MAIN APP WORKFLOW / मुख्य कार्यप्रवाह
# =========================================================
if uploaded_video:
    # वीडियो को टेम्पररी फाइल में सेव करें
    tfile = tempfile.NamedTemporaryFile(delete=False, suffix=".mp4")
    tfile.write(uploaded_video.read())
    video_path = tfile.name

    cap = cv2.VideoCapture(video_path)
    width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    fps = int(cap.get(cv2.CAP_PROP_FPS)) or 25
    total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))

    # =====================================================
    # SIDEBAR SETTINGS / साइडबार सेटिंग्स
    # =====================================================
    st.sidebar.header("⚙️ सेटिंग्स (Settings)")
    st.sidebar.markdown(f"**वीडियो रेजोल्यूशन:** `{width} x {height}` | **FPS:** `{fps}`")

    # 1. वाटरमार्क ब्लर सेटिंग्स
    st.sidebar.subheader("💧 पुराना वाटरमार्क ब्लर करें")
    enable_blur = st.sidebar.checkbox("वाटरमार्क ब्लर सक्षम करें", value=True)

    # क्विक प्रीसेट्स (Quick Presets)
    preset = st.sidebar.selectbox(
        "क्विक पोजीशन प्रीसेट",
        ["कस्टम (Custom)", "नीचे-दाएं (Bottom-Right)", "ऊपर-दाएं (Top-Right)", "नीचे-बाएं (Bottom-Left)", "ऊपर-बाएं (Top-Left)"]
    )

    def_w = min(150, width // 4)
    def_h = min(60, height // 8)
    if preset == "नीचे-दाएं (Bottom-Right)":
        def_x, def_y = width - def_w - 30, height - def_h - 30
    elif preset == "ऊपर-दाएं (Top-Right)":
        def_x, def_y = width - def_w - 30, 30
    elif preset == "नीचे-बाएं (Bottom-Left)":
        def_x, def_y = 30, height - def_h - 30
    elif preset == "ऊपर-बाएं (Top-Left)":
        def_x, def_y = 30, 30
    else:
        def_x, def_y = 50, 50

    blur_x = st.sidebar.slider("Blur X Position (क्षैतिज)", 0, max(1, width - 10), def_x)
    blur_y = st.sidebar.slider("Blur Y Position (लंबवत)", 0, max(1, height - 10), def_y)
    blur_w = st.sidebar.slider("Blur Width (चौड़ाई)", 10, max(20, width // 2), def_w)
    blur_h = st.sidebar.slider("Blur Height (ऊंचाई)", 10, max(20, height // 3), def_h)
    blur_ksize = st.sidebar.slider("Blur Strength (ब्लर की तीव्रता)", 11, 101, 51, step=2)

    # 2. नया लोगो सेटिंग्स
    st.sidebar.subheader("🖼️ नया लोगो सेटिंग्स")
    if uploaded_logo is not None:
        logo_size = st.sidebar.slider("Logo Size (% वीडियो चौड़ाई)", 5, 50, 16)
        logo_x = st.sidebar.slider("Logo X Position", 0, max(1, width - 50), max(0, width - int(width * (logo_size / 100)) - 30))
        logo_y = st.sidebar.slider("Logo Y Position", 0, max(1, height - 50), max(0, height - int(height * (logo_size / 100)) - 30))
        logo_opacity = st.sidebar.slider("Logo Opacity (पारदर्शिता)", 0.1, 1.0, 1.0, step=0.05)
    else:
        st.sidebar.info("💡 नया लोगो लगाने के लिए ऊपर PNG फाइल अपलोड करें।")
        logo_x, logo_y, logo_size, logo_opacity = 0, 0, 15, 1.0

    # =====================================================
    # LIVE PREVIEW SECTION / लाइव प्रीव्यू
    # =====================================================
    st.subheader("👁️ लाइव फ्रेम प्रीव्यू (Live Frame Preview)")
    st.caption("पूरा वीडियो प्रोसेस करने से पहले फ्रेम देखकर पोज़ीशन सुनिश्चित करें:")

    preview_frame_idx = st.slider(
        "फ्रेम नंबर चुनें (Seek Frame)",
        min_value=0,
        max_value=max(1, total_frames - 1),
        value=min(15, total_frames // 4)
    )

    cap.set(cv2.CAP_PROP_POS_FRAMES, preview_frame_idx)
    ret, sample_bgr = cap.read()

    if ret and sample_bgr is not None:
        sample_rgb = cv2.cvtColor(sample_bgr, cv2.COLOR_BGR2RGB)
        preview_rgb = sample_rgb.copy()

        # ब्लर अप्लाई करें
        if enable_blur:
            preview_rgb = apply_blur(preview_rgb, blur_x, blur_y, blur_w, blur_h, blur_ksize)

        # लोगो अप्लाई करें
        if uploaded_logo is not None:
            preview_rgb = apply_logo(preview_rgb, logo_img, logo_x, logo_y, logo_size, logo_opacity)

        col_p1, col_p2 = st.columns(2)
        with col_p1:
            st.markdown("**मूल फ्रेम (बॉक्स मार्कर के साथ)**")
            guide_frame = sample_rgb.copy()
            if enable_blur:
                cv2.rectangle(
                    guide_frame,
                    (blur_x, blur_y),
                    (min(width, blur_x + blur_w), min(height, blur_y + blur_h)),
                    (255, 60, 60),
                    3
                )
            st.image(guide_frame, use_container_width=True)

        with col_p2:
            st.markdown("**फाइनल आउटपुट प्रीव्यू (ब्लर + नया लोगो)**")
            st.image(preview_rgb, use_container_width=True)

    # =====================================================
    # PROCESSING SECTION / वीडियो प्रोसेसिंग
    # =====================================================
    st.markdown("---")
    st.subheader("🚀 वीडियो प्रोसेसिंग (Start Processing)")

    # ऑडियो प्रिजर्वेशन विकल्प (Audio preservation toggle)
    preserve_audio = st.checkbox("मूल ऑडियो सुरक्षित रखें (Preserve Original Audio with MoviePy)", value=True)

    if st.button("▶️ प्रोसेसिंग शुरू करें (Process Video)", type="primary", use_container_width=True):
        progress_bar = st.progress(0.0)
        status_box = st.empty()
        status_box.info("⚙️ वीडियो प्रोसेस हो रहा है, कृपया इंतज़ार करें...")

        output_path = tempfile.NamedTemporaryFile(delete=False, suffix=".mp4").name

        try:
            if preserve_audio:
                # MoviePy के साथ प्रोसेसिंग (ऑडियो सुरक्षित रहता है और वेब पर तुरंत प्ले होता है)
                from moviepy.editor import VideoFileClip

                clip = VideoFileClip(video_path)
                frame_count = [0]
                tot = max(1, int(clip.duration * (clip.fps or 25)))

                def frame_processor(gf, t):
                    frame = gf(t)
                    frame_count[0] += 1
                    if frame_count[0] % 10 == 0 or frame_count[0] >= tot:
                        pct = min(1.0, frame_count[0] / float(tot))
                        progress_bar.progress(pct)
                        status_box.text(f"प्रोसेसिंग: फ्रेम {frame_count[0]}/{tot} ({int(pct * 100)}%)")

                    # ब्लर और लोगो लगाएं
                    out_f = frame.copy()
                    if enable_blur:
                        out_f = apply_blur(out_f, blur_x, blur_y, blur_w, blur_h, blur_ksize)
                    if uploaded_logo is not None:
                        out_f = apply_logo(out_f, logo_img, logo_x, logo_y, logo_size, logo_opacity)
                    return out_f

                processed_clip = clip.fl(frame_processor)
                status_box.text("💾 अंतिम H.264 वीडियो तैयार हो रहा है...")
                processed_clip.write_videofile(
                    output_path,
                    codec="libx264",
                    audio_codec="aac",
                    temp_audiofile=tempfile.mktemp(suffix=".m4a"),
                    remove_temp=True,
                    threads=4,
                    logger=None
                )
            else:
                # प्योर OpenCV VideoWriter पाइपलाइन
                cap.set(cv2.CAP_PROP_POS_FRAMES, 0)
                # वेब कम्पेटिबल fourcc
                fourcc = cv2.VideoWriter_fourcc(*'mp4v')
                out = cv2.VideoWriter(output_path, fourcc, fps, (width, height))

                processed_count = 0
                while cap.isOpened():
                    ret, frame_bgr = cap.read()
                    if not ret:
                        break

                    # OpenCV में कलर BGR होता है
                    if enable_blur:
                        frame_bgr = apply_blur(frame_bgr, blur_x, blur_y, blur_w, blur_h, blur_ksize)

                    if uploaded_logo is not None:
                        # BGR में लोगो कन्वर्ट करके लगाएं
                        frame_rgb = cv2.cvtColor(frame_bgr, cv2.COLOR_BGR2RGB)
                        frame_rgb = apply_logo(frame_rgb, logo_img, logo_x, logo_y, logo_size, logo_opacity)
                        frame_bgr = cv2.cvtColor(frame_rgb, cv2.COLOR_RGB2BGR)

                    out.write(frame_bgr)
                    processed_count += 1
                    if processed_count % 15 == 0:
                        pct = min(1.0, processed_count / float(max(1, total_frames)))
                        progress_bar.progress(pct)
                        status_box.text(f"प्रोसेसिंग: फ्रेम {processed_count}/{total_frames} ({int(pct * 100)}%)")

                out.release()

            progress_bar.progress(1.0)
            status_box.success("🎉 वीडियो सफलतापूर्वक तैयार हो गया! (Video Processed Successfully!)")

            # वीडियो प्लेयर
            st.video(output_path)

            # डाउनलोड बटन
            with open(output_path, "rb") as file:
                st.download_button(
                    label="📥 डाउनलोड वीडियो (Download Video)",
                    data=file.read(),
                    file_name="watermarked_processed_video.mp4",
                    mime="video/mp4",
                    use_container_width=True
                )

        except Exception as e:
            status_box.error(f"❌ प्रोसेसिंग में त्रुटि आई: {str(e)}")

    cap.release()
else:
    st.info("👈 कृपया ऊपर दिए गए बॉक्स से वीडियो और अपना नया लोगो अपलोड करें।")
