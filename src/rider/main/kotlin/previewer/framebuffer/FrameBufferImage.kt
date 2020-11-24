package me.fornever.avaloniarider.previewer.framebuffer

import com.intellij.ui.paint.PaintUtil
import com.intellij.util.JBHiDPIScaledImage
import com.intellij.util.ui.UIUtil
import me.fornever.avaloniarider.controlmessages.FrameMessage
import java.awt.*
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import javax.swing.JComponent
import javax.swing.border.EmptyBorder
import kotlin.math.ceil

class FrameBufferImage: JComponent() {

    private class Dimension(val width: Double, val height: Double) {

    }

    private val DEFAULT_SCALE: Int = 100

    private var scale: Int = DEFAULT_SCALE
    private var isGridVisible: Boolean = false

    private var scaleSet: IntArray = intArrayOf(25, 50, 100, 150, 200, 300, 400, 800)

    private var imageBuffer: BufferedImage? = null

    private fun fromByte(b: Byte): Int = b.toInt() and 0xFF

    private fun getHiDPIDimensions(width: Double, height: Double) : Dimension {
        val defaultConfig = this.graphicsConfiguration?.device?.defaultConfiguration

        val scaleX = defaultConfig?.defaultTransform?.scaleX ?: 1.0
        val scaleY = defaultConfig?.defaultTransform?.scaleY ?: 1.0

        val hiDpiWidth = (width / scaleX)
        val hiDpiHeight = (height / scaleY)

        return Dimension(hiDpiWidth, hiDpiHeight)
    }

    private fun getHiDPIDimensions(width: Int, height: Int) : Dimension {
        return getHiDPIDimensions(width.toDouble(), height.toDouble())
    }

    private fun updateScaledDimensions(width: Int, height: Int): Boolean {
        val scaledDimensions  = getHiDPIDimensions(width, height)

        if (this.width.toDouble() != ceil(scaledDimensions.width) || this.height.toDouble() != ceil(scaledDimensions.height)) {
            this.preferredSize = Dimension(ceil(scaledDimensions.width).toInt(), ceil(scaledDimensions.height).toInt())
            this.revalidate()
            return true
        }

        return false
    }

    fun updateDimensions(width: Int, height: Int, forceRepaint: Boolean = false) {
        if (updateScaledDimensions(width, height) && forceRepaint) {
            this.repaint()
        }
    }

    fun isDefaultScale() : Boolean {
        return this.scale == DEFAULT_SCALE
    }

    fun isMinimumScale() : Boolean {
        return this.scaleSet.indexOf(this.scale) <= 0
    }

    fun isMaximumScale() : Boolean {
        return this.scaleSet.indexOf(this.scale) >= this.scaleSet.lastIndex
    }

    fun setDefaultScale() {
        if (this.scale != DEFAULT_SCALE) {
            this.scale = DEFAULT_SCALE
            this.imageBuffer = null
        }
    }

    fun increaseScale() {
        val idx = this.scaleSet.indexOf(this.scale) + 1
        if (idx > this.scaleSet.size)
            return

        val newScale = this.scaleSet[idx]

        if (this.scale != newScale) {
            this.scale = newScale
            this.imageBuffer = null
        }
    }


    fun decreaseScale() {

        val idx = this.scaleSet.indexOf(this.scale) - 1
        if (idx < 0)
            return

        val newScale = this.scaleSet[idx]

        if (this.scale != newScale) {
            this.scale = newScale
            this.imageBuffer = null
        }
    }

    fun toggleGrid() {
        isGridVisible = !isGridVisible
    }

    fun isGridVisible() : Boolean {
        return isGridVisible
    }

    fun renderFrame(frame: FrameMessage?) {

        if (this.imageBuffer == null /*|| this.imageBuffer!!.width != frame!!.width || this.imageBuffer!!.height != frame!!.height*/) {

            val hiDpiDimensions  = getHiDPIDimensions(frame!!.width, frame!!.height)

            val hiDpiWidth = hiDpiDimensions.width
            val hiDpiHeight = hiDpiDimensions.height

            this.imageBuffer = UIUtil.createImage(this.graphicsConfiguration, hiDpiWidth * (scale / 100.0), hiDpiHeight * (scale / 100.0), BufferedImage.TYPE_INT_ARGB, PaintUtil.RoundingMode.CEIL)
            //this.imageBuffer = UIUtil.createImage(this.graphicsConfiguration, frame!!.width * scale, frame!!.height * scale, BufferedImage.TYPE_INT_ARGB, PaintUtil.RoundingMode.CEIL)

            if (this.imageBuffer is JBHiDPIScaledImage) {
                this.updateDimensions((this.imageBuffer as JBHiDPIScaledImage).getUserWidth(this), (this.imageBuffer as JBHiDPIScaledImage).getUserHeight(this))
            }
            else {
                this.updateDimensions(this.imageBuffer!!.width, this.imageBuffer!!.height)
            }

            //updateDimensions(frame!!.width, frame!!.height, false)
            //this.image = UIUtil.createImage(this, frame!!.width, frame!!.height, BufferedImage.TYPE_INT_RGB)
        }

        //Only render every x frame
        //if (frame!!.sequenceId % 3 != 1L)
        //Skip every second frame
        if (frame!!.sequenceId % 2 == 1L)
            return

        renderPixels(frame!!, this.imageBuffer!!)

        //this.updateScaledDimensions(this.imageBuffer!!.getWidth(this), this.imageBuffer!!.getHeight(this))
    }

    fun getColorArrayFromFrame(frame: FrameMessage): IntArray {
        val colorData = IntArray((frame.data.size / 4))

        for (y in 0 until frame.height) {
            for (x in 0 until frame.width) {
                val pixelIndex = y * frame.width * 4 + x * 4
                val bytes = frame.data.slice(pixelIndex..pixelIndex + 3)
                colorData[x + y * frame.width] = getColorAsRgb(bytes)
            }
        }

        return colorData
    }

    fun getColor(byteArray: List<Byte>): Color {
        return Color(fromByte(byteArray[0]), fromByte(byteArray[1]), fromByte(byteArray[2]), fromByte(byteArray[3]))
    }

    fun getColorAsRgb(byteArray: List<Byte>): Int {
        return getColor(byteArray).rgb
    }

    fun renderPixels(frame: FrameMessage, image: BufferedImage) {

        val pixelData = (image.raster.dataBuffer as DataBufferInt).data
        val colorData = getColorArrayFromFrame(frame)

        val xRatio = ((frame.width shl 16) / image.width) + 1
        val yRatio = ((frame.height shl 16) / image.height) + 1

        var xOffset: Int
        var yOffset: Int

        val gridColor = Color.gray.rgb

        for (iHeight in 0 until image.height) {
            for (iWidth in 0 until image.width) {

                if (isGridVisible && scale >= 100 && (((iHeight + 1) % (scale / 100)) == 1 || ((iWidth + 1) % (scale / 100)) == 1)) {
                    pixelData[iHeight * image.width + iWidth] = gridColor
                    continue
                }

                xOffset = iWidth * xRatio shr 16
                yOffset = iHeight * yRatio shr 16
                val pixelIndex = yOffset * frame.width + xOffset
                var pixelColor = colorData[pixelIndex]

                pixelData[iHeight * image.width + iWidth] = pixelColor

                //pixelData[iHeight * image.width + iWidth] = if ((iHeight % 4) == 0 || (iWidth % 4) == 0) pixelColor xor 0x00ffffff else pixelColor
                //pixelData[iHeight * image.width + iWidth] = if ((iHeight % 4) == 0 || (iWidth % 4) == 0) Color.white.rgb else pixelColor

            }
        }

        this.repaint()
    }

    override fun isVisible(): Boolean {
        return this.imageBuffer != null && this.imageBuffer!!.width > 0 && this.imageBuffer!!.height > 0
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        if (imageBuffer != null) {
            val graphics2D = g as Graphics2D
            graphics2D.drawImage(imageBuffer, 0, 0, this.width, this.height, this)
            //graphics2D.drawImage(this.imageBuffer!!.getScaledInstance(this.imageBuffer!!.width, this.imageBuffer!!.height, Image.SCALE_DEFAULT), 0, 0,  this.width,  this.height, this)
        }
    }

    init {
        border = EmptyBorder(0, 0, 0, 0)
        isOpaque = false
        background = Color(0, 0, 0, 0)
        isDoubleBuffered = true
    }
}
