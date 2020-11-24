package me.fornever.avaloniarider.idea.editor

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.AsyncProcessIcon
import com.intellij.util.ui.UIUtil
import com.jetbrains.rd.platform.util.application
import com.jetbrains.rd.util.lifetime.Lifetime
import me.fornever.avaloniarider.controlmessages.FrameMessage
import me.fornever.avaloniarider.controlmessages.UpdateXamlResultMessage
import me.fornever.avaloniarider.idea.concurrency.adviseOnUiThread
import me.fornever.avaloniarider.plainTextToHtml
import me.fornever.avaloniarider.previewer.AvaloniaMessageMouseListener
import me.fornever.avaloniarider.previewer.AvaloniaPreviewerSessionController
import me.fornever.avaloniarider.previewer.AvaloniaPreviewerSessionController.Status
import me.fornever.avaloniarider.previewer.framebuffer.FrameBufferComponent
import me.fornever.avaloniarider.previewer.framebuffer.actions.*
import me.fornever.avaloniarider.previewer.nonTransparent
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder


class BitmapPreviewEditorComponent(lifetime: Lifetime, controller: AvaloniaPreviewerSessionController) : JPanel() {
    companion object {
        private val logger = Logger.getInstance(BitmapPreviewEditorComponent::class.java)
    }

    private val dimensionLabel = JBLabel().apply {
        horizontalAlignment = SwingConstants.LEFT
        border = EmptyBorder(0, 2, 0, 2)
    }

    private val mainScrollView = JBScrollPane().apply {
        this.isOpaque = false
    }

    private val frameBufferView = lazy {
        FrameBufferComponent()
    }

    private val spinnerView = lazy {
        AsyncProcessIcon.Big("Loading").apply {
            isOpaque = false
            background = UIUtil.getPanelBackground()
        }
    }
    private val errorLabel = lazy {
        JBLabel().apply {
            setCopyable(true)
        }
    }
    private val errorView = lazy {
        JPanel().apply {
            layout = GridBagLayout()
            add(JLabel(AllIcons.General.Error))
            add(errorLabel.value)
        }
    }
    private val terminatedView = lazy { JLabel("Previewer has been terminated") }

    private var status = Status.Idle


    private fun updateInfoLabel(width: Int, height: Int) {

        if (width <= 0 && height <= 0)
        {
            dimensionLabel.text = ""
            return
        }

        dimensionLabel.text = "${width}x${height}"
    }

    init {
        layout = BorderLayout()
        add(mainScrollView, BorderLayout.CENTER)

        add(JPanel().apply {
            this.layout = BorderLayout(0, 0)

            val actionGroup = DefaultActionGroup()
            actionGroup.add(ToggleGridAction(frameBufferView))
            actionGroup.addSeparator()
            actionGroup.add(ZoomInAction(frameBufferView))
            actionGroup.add(ZoomOutAction(frameBufferView))
            actionGroup.add(ZoomDefaultAction(frameBufferView))
            actionGroup.addSeparator()

            val actionManager = ActionManager.getInstance()
            val actionToolbar = actionManager.createActionToolbar("AvaloniaBitmapPreviewToolbar", actionGroup, true)

            actionToolbar.setTargetComponent(this)

            add(actionToolbar.component, BorderLayout.WEST)
            add(dimensionLabel, BorderLayout.EAST)

        }, BorderLayout.NORTH)

        controller.requestViewportResize.advise(lifetime) {
            frameBufferView.value.updateDimensions(it.width.toInt(), it.height.toInt())
            updateInfoLabel(it.width.toInt(), it.height.toInt())
        }

        controller.status.adviseOnUiThread(lifetime, ::handleStatus)
        controller.updateXamlResult.adviseOnUiThread(lifetime, ::handleXamlResult)
        controller.criticalError.adviseOnUiThread(lifetime, ::handleCriticalError)

        controller.frame.adviseOnUiThread(lifetime) { frame ->
            if (nonTransparent(frame)) // TODO[F]: Remove after fix of https://github.com/AvaloniaUI/Avalonia/issues/4264
                handleFrame(frame)
            controller.acknowledgeFrame(frame)
        }
    }

    private fun handleStatus(newStatus: Status) {
        application.assertIsDispatchThread()
        mainScrollView.viewport.view = when (newStatus) {
            Status.Idle, Status.Connecting -> spinnerView.value
            Status.Working -> frameBufferView.value
            Status.XamlError -> errorView.value
            Status.Suspended -> spinnerView.value
            Status.Terminated -> terminatedView.value
        }

        status = newStatus
        logger.info("Status: $status")
    }

    private fun handleFrame(frame: FrameMessage) {
        application.assertIsDispatchThread()
        if (status != Status.Working) {
            logger.warn("Had to skip a frame because it came during status $status")
            return
        }

        val frameBuffer = frameBufferView.value
        if (frame.height <= 0 || frame.width <= 0) {
            updateInfoLabel(0, 0)
            frameBuffer.updateFrame(null)
            return
        }

        frameBuffer.updateFrame(frame)
    }

    private fun handleXamlResult(message: UpdateXamlResultMessage) {
        val errorMessage =
            if (message.exception == null && message.error == null) ""
            else {
                val buffer = StringBuilder()
                val exception = message.exception
                if (exception != null) {
                    val exceptionType = exception.exceptionType ?: "Error"
                    buffer.append(exceptionType)
                    if (exception.lineNumber != null || exception.linePosition != null) {
                        buffer.append(" at ").append(when {
                            exception.lineNumber != null && exception.linePosition != null ->
                                "${exception.lineNumber}:${exception.linePosition}"
                            exception.lineNumber != null -> "${exception.lineNumber}"
                            else -> "position ${exception.linePosition}"
                        })
                    }

                    if (exception.message != null) {
                        buffer.append(": ").append(exception.message)
                    }
                }
                if (message.error != null) {
                    if (message.exception != null) buffer.append("\n")
                    buffer.append(message.error)
                }
                buffer.toString()
            }

        if (errorMessage.isNotEmpty())
            errorLabel.value.text = errorMessage.plainTextToHtml()
    }

    private fun handleCriticalError(error: Throwable) {
        terminatedView.value.text = "Previewer has been terminated: ${error.localizedMessage}".plainTextToHtml()
    }
}
