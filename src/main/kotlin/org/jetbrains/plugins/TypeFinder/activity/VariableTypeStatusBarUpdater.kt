package org.jetbrains.plugins.TypeFinder.activity;

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import org.jetbrains.plugins.TypeFinder.widgets.TypeFindWidget
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.jetbrains.python.psi.*


class VariableTypeStatusBarUpdater : ProjectActivity {
    override suspend fun execute(project: Project) {
        val editorFactory = EditorFactory.getInstance()
        val statusBar = com.intellij.openapi.wm.WindowManager.getInstance().getStatusBar(project)

        editorFactory.eventMulticaster.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) {
                event.editor.let { editor ->
                    val variableInfo = getVariableInfo(editor, event.caret, project)
                    val widget = statusBar?.getWidget("TypeFindWidget") as? TypeFindWidget
                    widget?.updateText(variableInfo)
                }
            }
        }, project)
    }

    private fun getVariableInfo(editor: Editor, caret: Caret?, project: Project): String {
        if (caret == null) return "Caret is not in editor"

        val document = editor.document
        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document) ?: return "No PSI file"
        val offset = caret.offset

        val element = psiFile.findElementAt(offset) ?: return "No element at caret"
        val assignment = PsiTreeUtil.getParentOfType(element, PyAssignmentStatement::class.java) ?: return "Not an assignment"

        val assignedValue = assignment.assignedValue ?: return "No assigned value"
        thisLogger().warn("$assignedValue")
        val variableName = (assignment.targets.firstOrNull() as? PsiNamedElement)?.name ?: return "No variable name"

        val variableType = determineVariableType(assignedValue) ?: "Unknown type"
        return "$variableName: $variableType"
    }

    private fun determineVariableType(expression: PyExpression): String? {
//        thisLogger().warn(expression.elementType.toString())
        return when (expression) {
            is PyStringLiteralExpression -> "str"
            is PyNumericLiteralExpression -> if (expression.isIntegerLiteral) "int" else "float"
            is PyBoolLiteralExpression -> "bool"
            is PyNoneLiteralExpression -> "NoneType"
            is PyListLiteralExpression -> "list"
            is PyParenthesizedExpression -> "tuple"
            is PyDictLiteralExpression -> "dict"
            is PySetLiteralExpression -> "set"
            is PyCallExpression -> {
                val callee = expression.callee?.text
                if (callee != null && callee.startsWith("range")) {
                    "range"
                }else if(callee != null && callee == "set") {
                    "set"
                } else {
                    "Function Call"
                }
            }
            is PyPrefixExpression -> {
                val operand = expression.operand
                if (operand is PyNumericLiteralExpression) {
                    if (operand.isIntegerLiteral) "int" else "float"
                } else {
                    "not supported"
                }
            }
            else -> null
        }
    }
}
