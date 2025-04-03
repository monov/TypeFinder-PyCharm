package org.jetbrains.plugins.TypeFinder.activity;

import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import org.jetbrains.plugins.TypeFinder.utils.determineVariableType
import org.jetbrains.plugins.TypeFinder.widgets.TypeFindWidget

class VariableTypeStatusBarUpdater : ProjectActivity {
    override suspend fun execute(project: Project) {
        val editorFactory = EditorFactory.getInstance()
        val statusBar = com.intellij.openapi.wm.WindowManager.getInstance().getStatusBar(project)

        editorFactory.eventMulticaster.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) {
                event.editor.let { editor ->
                    val variableInfo = logVariableName(editor, event.caret)
                    val widget = statusBar?.getWidget("TypeFindWidget") as? TypeFindWidget
                    widget?.updateText(variableInfo)
                }
            }
        }, project)
    }

    private fun logVariableName(editor: Editor, caret: Caret?): String {
        if (caret == null) return "Caret is not in editor"
        val document = editor.document
        val offset = caret.offset

        val lineNumber = document.getLineNumber(offset)
        val lineStartOffset = document.getLineStartOffset(lineNumber)
        val lineEndOffset = document.getLineEndOffset(lineNumber)
        val line = document.getText(TextRange(lineStartOffset, lineEndOffset))

        val variableRegex = Regex(
            "\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*(" +
                    "\"[^\"]*\"|'[^']*'" +                       // Strings
                    "|-?\\d+[+-]\\d+j" +                         // Complex with real and imaginary parts
                    "|-?\\d*j" +                                 // Pure imaginary
                    "|-?\\d+\\.\\d+" +                           // Float
                    "|-?\\d+" +                                  // Integers
                    "|True|False" +                              // Boolean
                    "|None" +                                    // NoneType
                    "|\\[.*?]" +                                 // Lists
                    "|\\(.*?\\)" +                               // Tuples
                    "|\\{[^}]*}" +                               // Sets & Dicts
                    "|range\\(.*?\\)" +                          // Range (specifically identified)
                    "|[a-zA-Z_][a-zA-Z0-9_]*\\(.*?\\)" +         // Other function calls (including set())
                    ")"
        )

        val matches = variableRegex.findAll(line)
        var closestVariableName: String? = null
        var closestVariableType: String? = null
        var closestDistance = Int.MAX_VALUE

        for (matchResult in matches) {
            val variableName = matchResult.groups[1]?.value
            val variableValue = matchResult.groups[2]?.value

            if (variableName != null && variableValue != null) {
                val variableStart = line.indexOf(matchResult.value)
                if (variableStart != -1) {
                    val variableOffset = lineStartOffset + variableStart
                    val distance = Math.abs(offset - variableOffset)
                    if (distance < closestDistance) {
                        closestDistance = distance
                        closestVariableName = variableName
                        closestVariableType = determineVariableType(variableValue)
                    }
                }
            }
        }

        if (closestVariableName != null) {
            return "$closestVariableName: $closestVariableType"
        }

        return "Not variable"
    }
}
