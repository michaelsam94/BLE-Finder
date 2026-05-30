package com.michael.blefinder.playstore

import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val PHONE = "w360dp-h640dp-xxhdpi"
private const val TABLET = "w800dp-h1280dp-xhdpi"

@RunWith(RobolectricTestRunner::class)
@Category(PlayStoreScreenshotTests::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class PlayStoreScreenshotTest {
    @Test
    @Config(qualifiers = PHONE)
    fun phone_01_dashboard() {
        capturePlayStoreImage("phone/01_dashboard.png") {
            PlayStoreScreenshotFrame(PlayStoreScene.Dashboard)
        }
    }

    @Test
    @Config(qualifiers = PHONE)
    fun phone_02_filters() {
        capturePlayStoreImage("phone/02_filters.png") {
            PlayStoreScreenshotFrame(PlayStoreScene.Filters)
        }
    }

    @Test
    @Config(qualifiers = PHONE)
    fun phone_03_radar() {
        capturePlayStoreImage("phone/03_radar.png") {
            PlayStoreScreenshotFrame(PlayStoreScene.Radar)
        }
    }

    @Test
    @Config(qualifiers = PHONE)
    fun phone_04_detail() {
        capturePlayStoreImage("phone/04_detail.png") {
            PlayStoreScreenshotFrame(PlayStoreScene.Detail)
        }
    }

    @Test
    @Config(qualifiers = TABLET)
    fun tablet_01_dashboard() {
        capturePlayStoreImage("tablet/01_dashboard.png") {
            PlayStoreScreenshotFrame(PlayStoreScene.Dashboard)
        }
    }

    @Test
    @Config(qualifiers = TABLET)
    fun tablet_02_filters() {
        capturePlayStoreImage("tablet/02_filters.png") {
            PlayStoreScreenshotFrame(PlayStoreScene.Filters)
        }
    }

    @Test
    @Config(qualifiers = TABLET)
    fun tablet_03_radar() {
        capturePlayStoreImage("tablet/03_radar.png") {
            PlayStoreScreenshotFrame(PlayStoreScene.Radar)
        }
    }

    @Test
    @Config(qualifiers = TABLET)
    fun tablet_04_detail() {
        capturePlayStoreImage("tablet/04_detail.png") {
            PlayStoreScreenshotFrame(PlayStoreScene.Detail)
        }
    }
}
