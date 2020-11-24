package me.fornever.avaloniarider.previewer.framebuffer.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.actionSystem.ex.ActionUtil
import me.fornever.avaloniarider.previewer.framebuffer.FrameBufferComponent

class ToggleGridAction(private val component: Lazy<FrameBufferComponent>) : ToggleAction(){

    init {
        ActionUtil.copyFrom(this, IdeActions.ACTION_RERUN)
        templatePresentation.icon = AllIcons.Graph.Grid
    }

    override fun isSelected(p0: AnActionEvent): Boolean {
        if (!component.isInitialized())
            return false

        return component.value.isGridVisible()
    }

    override fun setSelected(p0: AnActionEvent, p1: Boolean) {
        component.value.toggleGrid()
    }
}
