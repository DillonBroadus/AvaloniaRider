package me.fornever.avaloniarider.previewer.framebuffer.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.project.DumbAwareAction
import me.fornever.avaloniarider.previewer.framebuffer.FrameBufferComponent

class ZoomInAction(private val component: Lazy<FrameBufferComponent>): DumbAwareAction("Zoom In", "Zoom In", AllIcons.Graph.ZoomIn) {

    init {
        ActionUtil.copyFrom(this,  IdeActions.ACTION_REDO)
        templatePresentation.icon = AllIcons.Graph.ZoomIn
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = (this.component.isInitialized() && !this.component.value.isMaximumScale())
    }

    override fun actionPerformed(p0: AnActionEvent) {
        this.component.value.increaseScale()
    }
}
