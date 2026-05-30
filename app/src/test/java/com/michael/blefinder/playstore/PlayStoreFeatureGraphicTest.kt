package com.michael.blefinder.playstore

import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Category(PlayStoreScreenshotTests::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w1024dp-h500dp-mdpi")
class PlayStoreFeatureGraphicTest {
    @Test
    fun feature_graphic() {
        capturePlayStoreImage("feature-graphic.png") {
            PlayStoreFeatureGraphic()
        }
    }
}
