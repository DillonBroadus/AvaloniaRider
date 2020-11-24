package me.fornever.avaloniarider.previewer.framebuffer.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.project.DumbAwareAction
import me.fornever.avaloniarider.previewer.framebuffer.FrameBufferComponent

class ZoomOutAction(private val component: Lazy<FrameBufferComponent>) : DumbAwareAction(){

    init {
        ActionUtil.copyFrom(this, IdeActions.ACTION_RERUN)
        templatePresentation.icon = AllIcons.Graph.ZoomOut
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = (this.component.isInitialized() && !this.component.value.isMinimumScale())
    }

    override fun actionPerformed(p0: AnActionEvent) {
        this.component.value.decreaseScale()
    }
}
