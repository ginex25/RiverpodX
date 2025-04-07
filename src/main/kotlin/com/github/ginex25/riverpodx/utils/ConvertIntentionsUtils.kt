package com.github.ginex25.riverpodx.utils

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartComponent
import com.jetbrains.lang.dart.psi.DartMethodDeclaration

class ConvertIntentionsUtils {
    fun isConsumerWidget(psiElement: PsiElement): Boolean {
        return psiElement.text.equals("ConsumerWidget")
    }

    fun isConsumerStatefulWidget(psiElement: PsiElement): Boolean {
        return psiElement.text.contains("ConsumerStatefulWidget")
    }

    fun isStatelessWidget(psiElement: PsiElement): Boolean {
        return psiElement.text.contains("StatelessWidget")
    }

    fun isStatefulWidget(psiElement: PsiElement): Boolean {
        return psiElement.text.contains("StatefulWidget")
    }

    fun convertConsumerToConsumerStateful(project: Project, editor: Editor, element: PsiElement) {

        val classElement: DartClass = getDartClass(element) ?: return
        val className = classElement.name ?: return

        val fields: List<DartComponent> = classElement.fields.filterNot { it.name == "key" }

        val constructor = classElement.constructors.firstOrNull()?.text ?: "  const $className({super.key});\n"
        val methods: List<DartComponent> = classElement.methods
        val buildMethod = methods.find { it.name == "build" } as? DartMethodDeclaration

        val widgetClass = StringBuilder().apply {
            append("class $className extends ConsumerStatefulWidget {\n")
            fields.forEach { append("${it.text};\n") }
            append(constructor)
            append("  @override\n")
            append("  ${className}State createState() => ${className}State();\n")
            append("}\n\n")
        }.toString()

        val customCode: Pair<String, String>? = extractCustomCode(classElement)


        val stateClass = StringBuilder().apply {
            append("class ${className}State extends ConsumerState<$className> {\n\n")
            if (customCode != null) {
                append("    ${adjustFieldReferencesForStateClass(fields, customCode.first)}\n")
            }
            append("  @override\n")
            if (buildMethod != null) {
                var buildMethodText = buildMethod.text.replace(
                    "Widget build(BuildContext context, WidgetRef ref)",
                    "Widget build(BuildContext context)"
                )

                buildMethodText = buildMethodText.replace("@override", "")

                buildMethodText = adjustFieldReferencesForStateClass(fields, buildMethodText)

                append(buildMethodText.trim())
            } else {
                append("  Widget build(BuildContext context) {\n")
                append("    return Container();\n")
                append("  }\n")
            }
            if (customCode != null) {
                append("    ${adjustFieldReferencesForStateClass(fields, customCode.second)}\n")
            }
            append("}\n")
        }.toString()

        val newClassText = widgetClass + stateClass

        val document = editor.document

        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(classElement.textRange.startOffset, classElement.textRange.endOffset, newClassText)

            PsiDocumentManager.getInstance(project).commitDocument(document)

            val psiFileUpdated =
                PsiDocumentManager.getInstance(project).getPsiFile(document) ?: return@runWriteCommandAction

            CodeStyleManager.getInstance(project).reformat(psiFileUpdated)
        }
    }

    private fun adjustFieldReferencesForStateClass(
        fields: List<DartComponent>,
        text: String
    ): String {
        var modifiedText = text

        for (field in fields) {
            val regex = Regex("""\$\b${field.name}\b""")

            modifiedText = regex.replace(modifiedText) { matchResult ->
                "\${widget.${matchResult.value.substring(1)}}"
            }

            val wordRegex = Regex("""\w+\s*\(([^)]*)\)""")

            val fieldRegex = Regex("""\b${field.name}\b""")

            modifiedText = wordRegex.replace(modifiedText) { matchResult ->
                val fullMatch = matchResult.value
                val args = matchResult.groupValues[1]
                val newArgs = fieldRegex.replace(args) { "widget.${it.value}" }

                fullMatch.replace(args, newArgs)
            }
        }

        return modifiedText
    }

    private fun extractCustomCode(classElement: DartClass): Pair<String, String>? {
        val documentText = classElement.containingFile.fileDocument.text

        val fileEnd = classElement.textRange.endOffset - 1

        val constructor = classElement.constructors.firstOrNull() ?: return null
        val startOffset = constructor.textRange.endOffset

        val buildMethod = classElement.methods.find { it.name == "build" } as? DartMethodDeclaration ?: return null

        val buildStart =
            buildMethod.textRange.startOffset
        val buildEnd = buildMethod.textRange.endOffset

        val textBetween: String
        try {
            textBetween = documentText.substring(startOffset, buildStart)
        } catch (e: Exception) {
            return null
        }


        val textAfterBuild: String
        try {
            textAfterBuild = documentText.substring(buildEnd, fileEnd)
        } catch (e: Exception) {
            return null
        }


        return Pair(textBetween, textAfterBuild)
    }

    private fun getDartClass(element: PsiElement): DartClass? {
        return PsiTreeUtil.getParentOfType(element, DartClass::class.java)
    }
}