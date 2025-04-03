package org.jetbrains.plugins.TypeFinder.widgets

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.impl.status.EditorBasedWidget
import javax.swing.JOptionPane
import java.awt.event.MouseEvent
import com.intellij.util.Consumer

class TypeFindWidget(project: Project) : EditorBasedWidget(project), StatusBarWidget.TextPresentation {
    private var text = "Type detector"  // Make `text` mutable

    fun updateText(t: String) {
        text = t
        statusBar?.updateWidget("TypeFindWidget")
    }

    override fun ID(): String = "TypeFindWidget"

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun getText(): String = text

    override fun getAlignment(): Float = javax.swing.SwingConstants.LEFT.toFloat()

    override fun getTooltipText(): String = text

    override fun getClickConsumer(): Consumer<MouseEvent> = Consumer {
        JOptionPane.showMessageDialog(null, text, "Python Type Finder", JOptionPane.INFORMATION_MESSAGE)
    }
}