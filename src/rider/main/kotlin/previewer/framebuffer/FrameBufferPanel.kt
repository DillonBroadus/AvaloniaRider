package me.fornever.avaloniarider.previewer.framebuffer

import com.intellij.util.ui.UIUtil
import me.fornever.avaloniarider.controlmessages.FrameMessage
import org.jdesktop.swingx.border.DropShadowBorder
import java.awt.*
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.border.CompoundBorder
import javax.swing.border.LineBorder

class FrameBufferPanel: JComponent() {

    private class CustomLineBorder(color: Color = UIUtil.getBoundsColor(), thickness: Int = 1) : LineBorder(color, 1)
    {
        public fun setLineColor(color: Color) {
            super.lineColor = lineColor
        }
    }

    private val frameBufferImage = lazy { FrameBufferImage() }

    private var dropShadowBorder = DropShadowBorder(UIUtil.getPanelBackground().darker(), 8, 1.0f, 16, true, true, true, true)
    private var lineBorder = CustomLineBorder(UIUtil.getBoundsColor(), 1)

    fun updateDimensions(width: Int, height: Int) {
        frameBufferImage.value.updateDimensions(width, height, true)
        this.revalidate()
    }

    fun isDefaultScale() = frameBufferImage.value.isDefaultScale()

    fun isMinimumScale() = frameBufferImage.value.isMinimumScale()

    fun isMaximumScale() = frameBufferImage.value.isMaximumScale()

    fun setDefaultScale() = frameBufferImage.value.setDefaultScale()

    fun decreaseScale() = frameBufferImage.value.decreaseScale()

    fun increaseScale() = frameBufferImage.value.increaseScale()

    fun toggleGrid() = frameBufferImage.value.toggleGrid()

    fun isGridVisible() = frameBufferImage.value.isGridVisible()

    fun renderFrame(frame: FrameMessage?) = frameBufferImage.value.renderFrame(frame)

    fun imageWidth() = frameBufferImage.value.width

    fun imageHeight() = frameBufferImage.value.height

    fun imageDimension() = Dimension(imageHeight(), imageWidth())

    override fun isVisible() = this.frameBufferImage.value.isVisible

    override fun updateUI() {
        dropShadowBorder.shadowColor = UIUtil.getPanelBackground().darker()
        lineBorder.lineColor = UIUtil.getBoundsColor()
        super.updateUI()
    }

    public fun getBorderSize() : Dimension {
        var compoundBorder = this.border as CompoundBorder

        var borderWidth = compoundBorder.outsideBorder.getBorderInsets(this).left +
            compoundBorder.outsideBorder.getBorderInsets(this).right +
            compoundBorder.insideBorder.getBorderInsets(this).left +
            compoundBorder.insideBorder.getBorderInsets(this).right

        var borderHeight = compoundBorder.outsideBorder.getBorderInsets(this).top +
            compoundBorder.outsideBorder.getBorderInsets(this).bottom +
            compoundBorder.insideBorder.getBorderInsets(this).top +
            compoundBorder.insideBorder.getBorderInsets(this).bottom

        return Dimension(borderWidth, borderHeight)
    }

    public fun getOffset() : Point {
        var location = this.location
        var compoundBorder = this.border as CompoundBorder

        var leftOffset = location.x + compoundBorder.outsideBorder.getBorderInsets(this).left + compoundBorder.insideBorder.getBorderInsets(this).left
        var topOffset = location.y + compoundBorder.outsideBorder.getBorderInsets(this).top + compoundBorder.insideBorder.getBorderInsets(this).top

        return Point(leftOffset, topOffset)
    }

    init {
        isDoubleBuffered = true
        layout = FlowLayout(FlowLayout.CENTER, 0, 0)
        isOpaque = false

        border = CompoundBorder(
            dropShadowBorder,
            lineBorder
        )

        add(frameBufferImage.value)
    }
}
