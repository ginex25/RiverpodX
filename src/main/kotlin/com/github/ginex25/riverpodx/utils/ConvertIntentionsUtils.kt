package com.github.ginex25.riverpodx.utils

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.dart.psi.*
import com.jetbrains.lang.dart.util.DartElementGenerator

class ConvertIntentionsUtils {
    private fun isDartClass(psiElement: PsiElement): Boolean {
        return PsiTreeUtil.getParentOfType(psiElement, DartClass::class.java) != null
    }

    fun isConsumerWidget(psiElement: PsiElement): Boolean {
        return psiElement.text.equals("ConsumerWidget") && isDartClass(psiElement)
    }

    fun isConsumerStatefulWidget(psiElement: PsiElement): Boolean {
        return psiElement.text.contains("ConsumerStatefulWidget") && isDartClass(psiElement)
    }

    fun isStatelessWidget(psiElement: PsiElement): Boolean {
        return psiElement.text.contains("StatelessWidget") && isDartClass(psiElement)
    }

    fun isStatefulWidget(psiElement: PsiElement): Boolean {
        return psiElement.text.contains("StatefulWidget") && isDartClass(psiElement)
    }


    fun convertToConsumerStateful(project: Project, editor: Editor, element: PsiElement) {
        val dartClass = getDartClass(element) ?: return

        val className = dartClass.name ?: return

        val constructor = dartClass.constructors.firstOrNull()?.text ?: ""

        val fields: List<DartComponent> = dartClass.fields.filter { it.isFinal && it.name != "key" }

        val fieldUsages = findUsage(dartClass, fields)

        val methods = dartClass.methods

        val buildMethod: DartComponent? = methods.find { it.name == "build" }


        WriteCommandAction.runWriteCommandAction(project) {
            adjustFieldReferencesForStateClass(project, fieldUsages)

            val customCode: Pair<String, String>? = extractCustomCode(dartClass)

            val stateClass = StringBuilder().apply {
                append("class $className extends ConsumerStatefulWidget {\n")
                fields.forEach { append("${it.text};\n") }
                append(constructor)
                append("  @override\n")
                append("  ${className}State createState() => ${className}State();\n")
                append("}\n\n")
            }.toString()

            val widgetClass = StringBuilder().apply {
                append("class ${className}State extends ConsumerState<$className> {\n")
                if (customCode != null) {
                    append("    ${customCode.first}\n")
                }
                append("  @override\n")
                if (buildMethod != null) {
                    var buildMethodText = buildMethod.text.replace(
                        "Widget build(BuildContext context, WidgetRef ref)", "Widget build(BuildContext context)"
                    )

                    buildMethodText = buildMethodText.replace("@override", "")

                    append(buildMethodText.trim())
                } else {
                    append("  Widget build(BuildContext context) {\n")
                    append("    return Container();\n")
                    append("  }\n")
                }
                if (customCode != null) {
                    append("    ${customCode.second}\n")
                }
                append("}")
            }.toString()

            val newClassText = stateClass + widgetClass

            val document = editor.document

            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document)

            document.replaceString(dartClass.textRange.startOffset, dartClass.textRange.endOffset, newClassText)

            val psiFileUpdated =
                PsiDocumentManager.getInstance(project).getPsiFile(document) ?: return@runWriteCommandAction

            CodeStyleManager.getInstance(project).reformat(psiFileUpdated)
        }
    }

    private fun findUsage(
        dartClass: DartClass, fields: List<DartComponent>
    ): MutableMap<String, MutableList<DartReferenceExpression>> {
        val results = mutableMapOf<String, MutableList<DartReferenceExpression>>()

        val fieldNames = fields.mapNotNull { it.name }.toSet()

        dartClass.accept(object : DartRecursiveVisitor() {
            override fun visitReferenceExpression(expression: DartReferenceExpression) {
                super.visitReferenceExpression(expression)
                val name = expression.text

                if (name !in fieldNames) return

                val parameter = PsiTreeUtil.getParentOfType(expression, DartFieldFormalParameter::class.java)
                val isConstructor = parameter?.text?.contains("this.${expression.text}") == true

                if (isConstructor) return

                val resolved = expression.reference?.resolve()

                val component = resolved?.parent as? DartComponent

                if (component is DartComponent && component.name == name) {
                    results.getOrPut(name) { mutableListOf() }.add(expression)
                }
            }

        })

        return results
    }

    private fun adjustFieldReferencesForStateClass(
        project: Project, references: Map<String, List<DartReferenceExpression>>
    ) {
        val alreadyReplaced = mutableSetOf<DartStringLiteralExpression>()

        references.forEach { (originalName, expressions) ->
            expressions.forEach { expression ->


                val stringLiteral = PsiTreeUtil.getParentOfType(expression, DartStringLiteralExpression::class.java)

                if (stringLiteral != null && stringLiteral !in alreadyReplaced) {
                    alreadyReplaced += stringLiteral

                    var newString = stringLiteral.text

                    references.keys.forEach { fieldName ->
                        newString = newString.replace("$$fieldName", "\${widget.$fieldName}")
                    }

                    val newLiteral = DartElementGenerator.createExpressionFromText(project, newString)

                    if (newLiteral is DartStringLiteralExpression) {
                        stringLiteral.replace(newLiteral)
                    }

                    return@forEach
                }

                val newText = "widget.$originalName"

                val newReference = DartElementGenerator.createReferenceFromText(project, newText)

                if (newReference is DartReferenceExpression) {
                    expression.replace(newReference)
                }
            }
        }
    }


    private fun extractCustomCode(classElement: DartClass): Pair<String, String>? {
        val documentText = classElement.containingFile.fileDocument.text

        val fileEnd = classElement.textRange.endOffset - 1

        val constructor = classElement.constructors.firstOrNull() ?: return null
        val startOffset = constructor.textRange.endOffset

        val buildMethod = classElement.methods.find { it.name == "build" } as? DartMethodDeclaration ?: return null

        val buildStart = buildMethod.textRange.startOffset
        val buildEnd = buildMethod.textRange.endOffset

        val textBetween: String
        try {
            textBetween = documentText.substring(startOffset, buildStart).trim()
        } catch (e: Exception) {
            return null
        }


        val textAfterBuild: String
        try {
            textAfterBuild = documentText.substring(buildEnd, fileEnd).trim()
        } catch (e: Exception) {
            return null
        }


        return Pair(textBetween, textAfterBuild)
    }

    private fun getDartClass(element: PsiElement): DartClass? {
        return PsiTreeUtil.getParentOfType(element, DartClass::class.java)
    }
}