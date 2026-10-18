# 🎬 Video Watermark Blur & Logo Overlay Studio

A complete, production-ready **Streamlit** web application utilizing **OpenCV** and **MoviePy** to blur existing video watermarks and overlay custom transparent PNG logos.

---

## 🚀 Features

- **Video Upload**: Supports `.mp4`, `.mov`, `.avi`, and `.mkv`.
- **Watermark Blur / Obfuscation**:
  - Multiple algorithms: **Gaussian Blur**, **Pixelate/Mosaic**, **Median Blur**, and **Solid Blackout Box**.
  - Interactive bounding box coordinate inputs (`X`, `Y`, `Width`, `Height`).
  - One-click presets: Bottom-Right (TikTok/Reels), Top-Right (TV/YouTube), Bottom-Left, Top-Left, or Custom.
  - Adjustable blur kernel strength slider.
- **Transparent PNG Logo Overlay**:
  - Full alpha-channel transparency support for PNG & WebP images.
  - Scale factor control (proportional resizing).
  - Opacity / transparency blending slider (0% to 100%).
  - Position presets: Replace Blurred Watermark, Corner Placements, Center, or Custom Coordinates.
- **Side-by-Side Live Frame Preview**:
  - Visual bounding box marker on original frame.
  - Instant real-time preview on any video timestamp without having to render the full video.
- **Fast Full Video Render**:
  - MoviePy pipeline preserves audio tracks and original frame rate.
  - Real-time progress bar.
  - Direct `.mp4` video download button.

---

## 🛠️ Installation & Local Run

### 1. Clone or Copy Files
Ensure you have `app.py` and `requirements.txt` in your working directory.

### 2. Create Virtual Environment (Recommended)
```bash
python3 -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
```

### 3. Install Dependencies
```bash
pip install -r requirements.txt
```

> **Note on FFmpeg**: MoviePy uses `imageio_ffmpeg` automatically. If your system requires native ffmpeg, install it via:
> - Ubuntu/Debian: `sudo apt update && sudo apt install -y ffmpeg`
> - macOS: `brew install ffmpeg`
> - Windows: `winget install Gyan.FFmpeg` or download from official site.

### 4. Launch the Streamlit App
```bash
streamlit run app.py
```
Open your browser at `http://localhost:8501`.

---

## ☁️ Deployment Guide

### Deploying to Streamlit Community Cloud (Free)
1. Push `app.py` and `requirements.txt` to a GitHub repository.
2. Sign in to [share.streamlit.io](https://share.streamlit.io/).
3. Click **"New App"** -> Select your repository, branch, and `app.py`.
4. Click **"Deploy"**! Streamlit Cloud will install packages from `requirements.txt` automatically.

### Deploying with Docker
A `Dockerfile` is provided:
```dockerfile
FROM python:3.10-slim

WORKDIR /app

RUN apt-get update && apt-get install -y \
    ffmpeg \
    libsm6 \
    libxext6 \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY app.py .

EXPOSE 8501

ENTRYPOINT ["streamlit", "run", "app.py", "--server.port=8501", "--server.address=0.0.0.0"]
```

Build and run:
```bash
docker build -t watermark-studio .
docker run -p 8501:8501 watermark-studio
```
