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

    /**
     * Converts a regular StatefulWidget into a ConsumerStatefulWidget with a corresponding ConsumerState class.
     *
     * - Extracts fields, constructor, and custom code from the original class.
     * - Adjusts field references by prepending `widget.` where necessary.
     * - Moves the `build()` method into the new `State` class and updates its signature.
     * - Combines all parts into a new widget and state class definition.
     * - Replaces the original Dart class in the editor with the new Consumer-based code.
     *
     * @param project The current IntelliJ project.
     * @param editor The active editor.
     * @param element The PSI element used to locate the Dart class.
     */
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

            val stateClass = buildString {
                append("class $className extends ConsumerStatefulWidget {\n")
                fields.forEach { append("${it.text};\n") }
                append(constructor)
                append("  @override\n")
                append("  ${className}State createState() => ${className}State();\n")
                append("}\n\n")
            }

            val widgetClass = buildString {
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
            }

            val newClassText = stateClass + widgetClass

            applyWriteCommandAction(
                project,
                editor,
                dartClass.textRange.startOffset,
                dartClass.textRange.endOffset,
                newClassText
            )
        }
    }

    /**
     * Converts a StatefulWidget class and its associated State class into a ConsumerWidget.
     *
     * - Extracts fields, constructor, and custom code from the original classes.
     * - Replaces the `build()` method to include `WidgetRef ref`.
     * - Merges relevant code from the State class into the new ConsumerWidget class.
     * - Removes `widget.` references as they are no longer needed.
     * - Replaces the original class definitions in the editor with the generated code.
     *
     * @param project The current IntelliJ project.
     * @param editor The active editor.
     * @param element The PSI element used to locate the Dart class.
     */
    fun convertToConsumer(project: Project, editor: Editor, element: PsiElement) {
        val dartClass = getDartClass(element) ?: return

        val className = dartClass.name ?: return

        val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return

        val stateClass =
            file.children.filterIsInstance<DartClass>().firstOrNull { it.name?.contains(className + "State") == true }
                ?: return

        val constructor = dartClass.constructors.firstOrNull()?.text ?: ""

        val fields: List<DartComponent> = dartClass.fields.filter { it.isFinal && it.name != "key" }

        val methods = stateClass.methods

        val buildMethod: DartComponent? = methods.find { it.name == "build" }

        val customCode: Pair<String, String>? = extractCustomCode(stateClass)

        var newClassText = buildString {
            append("class $className extends ConsumerWidget {\n")
            fields.forEach { append("${it.text};\n") }
            append(constructor)

            if (customCode != null) {
                append("    ${customCode.first}\n")
            }

            if (buildMethod != null) {
                var buildMethodText = buildMethod.text.replace(
                    "Widget build(BuildContext context)", "Widget build(BuildContext context, WidgetRef ref)"
                )

                buildMethodText = buildMethodText.replace("@override", "")

                append(buildMethodText.trim())
            } else {
                append("  Widget build(BuildContext context, WidgetRef ref) {\n")
                append("    return Container();\n")
                append("  }\n")
            }

            if (customCode != null) {
                append("    ${customCode.second}\n")
            }

            append("}")
        }

        newClassText = removeWidgetReferences(newClassText, fields)

        applyWriteCommandAction(
            project,
            editor,
            dartClass.textRange.startOffset,
            stateClass.textRange.endOffset,
            newClassText
        )
    }


    private fun applyWriteCommandAction(
        project: Project,
        editor: Editor,
        startOffset: Int,
        endOffset: Int,
        newClassText: String
    ) {
        WriteCommandAction.runWriteCommandAction(project) {
            val document = editor.document

            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document)

            document.replaceString(startOffset, endOffset, newClassText)

            PsiDocumentManager.getInstance(project).commitDocument(document)

            val psiFileUpdated =
                PsiDocumentManager.getInstance(project).getPsiFile(document) ?: return@runWriteCommandAction

            CodeStyleManager.getInstance(project).reformat(psiFileUpdated)
        }
    }

    /**
     * Removes `widget.` prefixes from field references and string interpolations.
     *
     * Converts:
     * - `widget.fieldName` to `fieldName`
     * - `${fieldName}` to `$fieldName`
     *
     * @param code The code string to clean.
     * @param fields The list of Dart fields to process.
     * @return The updated code string with `widget.` references removed.
     */
    private fun removeWidgetReferences(code: String, fields: List<DartComponent>): String {
        var updatedCode = code

        val fieldNames: List<String> = fields.mapNotNull { it.name }

        val widgetRegex = Regex("widget\\.(${fieldNames.joinToString("|") { it }})")
        updatedCode = updatedCode.replace(widgetRegex) { match ->
            match.value.replace("widget.", "")
        }

        val interpolationRegex = Regex("\\$\\{(${fieldNames.joinToString("|")})}")
        updatedCode = interpolationRegex.replace(updatedCode) { matchResult ->
            "\$${matchResult.groupValues[1]}"
        }

        return updatedCode
    }

    /**
     * Finds all usages of the given fields within the provided Dart class.
     *
     * Skips references inside field formal parameters (e.g., constructor parameters like `this.field`).
     * Only includes references that resolve to the original field declaration.
     *
     * @param dartClass The Dart class to search in.
     * @param fields The list of field components to track.
     * @return A map of field names to the list of their reference expressions found in the class.
     */
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

    /**
     * Rewrites references in a Dart class to use the `widget.` prefix, as required in StatefulWidgets.
     *
     * Replaces:
     * - `$fieldName` inside string literals with `${widget.fieldName}`
     * - Direct field references with `widget.fieldName`
     *
     * @param project The current IntelliJ project.
     * @param references A map of field names to their corresponding Dart reference expressions.
     */
    private fun adjustFieldReferencesForStateClass(
        project: Project, references: Map<String, List<DartReferenceExpression>>
    ) {
        val alreadyReplaced = mutableSetOf<DartStringLiteralExpression>()

        references.forEach { (originalName, expressions) ->
            expressions.forEach expression@{ expression ->
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

                    return@expression
                }

                val newText = "widget.$originalName"

                val newReference = DartElementGenerator.createReferenceFromText(project, newText)

                if (newReference is DartReferenceExpression) {
                    expression.replace(newReference)
                }
            }
        }
    }

    /**
     * Extracts custom code from a Dart class outside the build() method.
     *
     * Returns a pair of strings:
     * - The code between the constructor (or class body) and the build() method.
     * - The code after the build() method until the end of the class.
     *
     * Returns null if required elements are missing or an error occurs during extraction.
     *
     * @param classElement The Dart class to extract code from.
     * @return Pair of code snippets or null if extraction fails.
     */
    private fun extractCustomCode(classElement: DartClass): Pair<String, String>? {
        val documentText = classElement.containingFile.fileDocument.text

        val fileEnd = classElement.textRange.endOffset - 1

        val constructor = classElement.constructors.firstOrNull()
        var startOffset = constructor?.textRange?.endOffset
        if (startOffset == null) {
            val body = PsiTreeUtil.findChildOfType(classElement, DartClassBody::class.java)

            startOffset = body?.textRange?.startOffset ?: return null
            startOffset++
        }

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