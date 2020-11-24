package me.fornever.avaloniarider.previewer.framebuffer.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.project.DumbAwareAction
import me.fornever.avaloniarider.previewer.framebuffer.FrameBufferComponent

class ZoomDefaultAction(private val component: Lazy<FrameBufferComponent>) : DumbAwareAction(){

    init {
        ActionUtil.copyFrom(this, IdeActions.ACTION_REFRESH)
        templatePresentation.icon = AllIcons.Graph.ActualZoom
        //this.displayTextInToolbar()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = (this.component.isInitialized() && !this.component.value.isDefaultScale())
    }

    override fun actionPerformed(p0: AnActionEvent) {
        this.component.value.setDefaultScale()
    }
}
