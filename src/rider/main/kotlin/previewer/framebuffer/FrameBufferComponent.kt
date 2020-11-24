package me.fornever.avaloniarider.previewer.framebuffer

import com.intellij.ui.ColorUtil
import com.intellij.util.ui.UIUtil
import me.fornever.avaloniarider.controlmessages.FrameMessage
import java.awt.*
import java.awt.image.BufferedImage
import java.util.*
import javax.swing.JComponent

class FrameBufferComponent : JComponent() {

    //private var scale: Double = 1.0

    private val tileCache: HashMap<Color, BufferedImage> = HashMap<Color, BufferedImage>()

    private fun getTileImage(color: Color) : BufferedImage {

        var cachedBitmap = tileCache[color]

        if (cachedBitmap == null)
        {
            cachedBitmap = UIUtil.createImage(this, 16, 16, BufferedImage.TYPE_INT_RGB).apply {

                var graphics2D = this.createGraphics()

                val secondaryColor = if (ColorUtil.isDark(color)) color.brighter() else color.darker()

                var row = 0
                while (row < this.height) {
                    var col = 0
                    while (col < this.width) {
                        var x = (col * 8)
                        var y = (row * 8)
                        if (row % 2 == col % 2) graphics2D.color = color else graphics2D.color = secondaryColor
                        graphics2D.fillRect(x, y, 8, 8)
                        col++
                    }
                    row++
                }

                graphics2D.dispose()
            }

            this.tileCache[color] = cachedBitmap
        }

        return cachedBitmap!!
    }

    private val frameBufferPanel = FrameBufferPanel()

    fun updateDimensions(width: Int, height: Int) {
        frameBufferPanel.updateDimensions(width, height)
        this.repaint()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val graphics2D = g as Graphics2D
        val checkerBoardPaint = TexturePaint(getTileImage(this.background), Rectangle(0, 0, 16, 16))

        graphics2D.paint = checkerBoardPaint
        graphics2D.fill(graphics2D.clip)
    }

    fun isDefaultScale() = frameBufferPanel.isDefaultScale()

    fun isMinimumScale() = frameBufferPanel.isMinimumScale()

    fun isMaximumScale() = frameBufferPanel.isMaximumScale()

    fun setDefaultScale() = frameBufferPanel.setDefaultScale()

    fun decreaseScale() = frameBufferPanel.decreaseScale()

    fun increaseScale() = frameBufferPanel.increaseScale()

    fun toggleGrid() = frameBufferPanel.toggleGrid()

    fun isGridVisible() = frameBufferPanel.isGridVisible()

    fun updateFrame(frame: FrameMessage?) = frameBufferPanel.renderFrame(frame)

    init {
        layout = GridBagLayout()

        isDoubleBuffered = true

        add(frameBufferPanel)
    }
}
