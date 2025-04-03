package org.jetbrains.plugins.TypeFinder.widgets

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NotNull

class TypeFindWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): @NotNull String = "TypeFindWidget"

    override fun getDisplayName(): @Nls @NotNull String = "Move the Cursor to the line with variable to display its type"

    override fun isAvailable(project: @NotNull Project): Boolean = true

    override fun createWidget(project: @NotNull Project): @NotNull StatusBarWidget = TypeFindWidget(project)

    override fun disposeWidget(widget: @NotNull StatusBarWidget) {
        widget.dispose()
    }
}