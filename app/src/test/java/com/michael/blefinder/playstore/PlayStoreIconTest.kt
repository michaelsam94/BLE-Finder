package com.michael.blefinder.playstore

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import androidx.test.core.app.ApplicationProvider
import com.michael.blefinder.R
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Category(PlayStoreScreenshotTests::class)
@Config(sdk = [35])
class PlayStoreIconTest {
    private companion object {
        const val IconSize = 512
        const val MaxIconBytes = 1_048_576
    }

    @Test
    fun app_icon_512() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val masterIcon = File("../ble-finder-icon-master.png").canonicalFile
        val output = File("../play-store/app-icon-512.png").canonicalFile
        output.parentFile?.mkdirs()

        if (masterIcon.exists()) {
            writeResizedMasterIcon(masterIcon, output)
        } else {
            val bitmap = Bitmap.createBitmap(IconSize, IconSize, Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            val drawable = requireNotNull(
                ResourcesCompat.getDrawable(context.resources, R.mipmap.ic_launcher, context.theme)
            )
            canvas.drawColor(context.getColor(R.color.ic_launcher_background))
            drawable.setBounds(0, 0, IconSize, IconSize)
            drawable.draw(canvas)
            output.outputStream().use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
        }

        val decoded = requireNotNull(BitmapFactory.decodeFile(output.path)) {
            "App icon must be a decodable PNG or JPEG"
        }
        assertEquals("App icon width must be 512 px", IconSize.toLong(), decoded.width.toLong())
        assertEquals("App icon height must be 512 px", IconSize.toLong(), decoded.height.toLong())
        assertTrue("App icon must be <= 1 MB, got ${output.length()} bytes", output.length() <= MaxIconBytes)
        assertTrue("App icon must not be blank or near-black", hasVisibleArtwork(decoded))
    }

    private fun hasVisibleArtwork(bitmap: Bitmap): Boolean {
        var brightSamples = 0
        var variedSamples = 0
        var previous = bitmap.getPixel(0, 0)

        for (y in 0 until bitmap.height step 32) {
            for (x in 0 until bitmap.width step 32) {
                val color = bitmap.getPixel(x, y)
                val brightness = Color.red(color) + Color.green(color) + Color.blue(color)
                if (brightness > 90) brightSamples += 1
                if (color != previous) variedSamples += 1
                previous = color
            }
        }

        return brightSamples > 8 && variedSamples > 8
    }

    private fun writeResizedMasterIcon(input: File, output: File) {
        val source = requireNotNull(ImageIO.read(input)) {
            "Master icon must be a decodable PNG or JPEG"
        }
        val resized = BufferedImage(IconSize, IconSize, BufferedImage.TYPE_INT_RGB)
        val graphics = resized.createGraphics()
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            graphics.drawImage(source, 0, 0, IconSize, IconSize, null)
        } finally {
            graphics.dispose()
        }
        ImageIO.write(resized, "png", output)
    }
}
