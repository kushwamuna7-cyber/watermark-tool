package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testVideoResolutions() {
    val res1080p = com.example.data.VideoResolution.RES_1080P
    assertEquals(1920, res1080p.width)
    assertEquals(1080, res1080p.height)
    assertEquals("16:9", res1080p.aspectLabel)

    val resVertical = com.example.data.VideoResolution.RES_VERTICAL
    assertEquals(1080, resVertical.width)
    assertEquals(1920, resVertical.height)
    assertEquals("9:16", resVertical.aspectLabel)
  }

  @Test
  fun testBlurConfigDefaults() {
    val config = com.example.data.BlurConfig()
    assertTrue(config.enabled)
    assertEquals(com.example.data.BlurType.GAUSSIAN, config.blurType)
    assertTrue(config.width > 0)
    assertTrue(config.height > 0)
  }
}
