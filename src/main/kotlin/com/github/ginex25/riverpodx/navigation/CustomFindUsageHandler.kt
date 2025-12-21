package com.github.ginex25.riverpodx.navigation

import com.github.ginex25.riverpodx.utils.ProviderNameUtils
import com.intellij.find.findUsages.FindUsagesHandler
import com.intellij.find.findUsages.FindUsagesOptions
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScopesCore
import com.intellij.usageView.UsageInfo
import com.intellij.util.Processor
import com.jetbrains.lang.dart.psi.DartCallExpression
import com.jetbrains.lang.dart.psi.DartRecursiveVisitor
import com.jetbrains.lang.dart.psi.DartReferenceExpression

class CustomFindUsageHandler(element: PsiElement) : FindUsagesHandler(element) {
    private val providerNameUtil = ProviderNameUtils

    override fun processElementUsages(
        element: PsiElement,
        processor: Processor<in UsageInfo>,
        options: FindUsagesOptions
    ): Boolean {
        ApplicationManager.getApplication().runReadAction {
            val elements = collectReferences()

            for (e in elements) {
                processor.process(UsageInfo(e))
            }
        }

        return true
    }

    private fun collectReferences(): Array<PsiElement> {
        val providerName = providerNameUtil.toGeneratedProviderName(psiElement.text)

        val foundElements = mutableListOf<PsiElement>()

        val psiManager = PsiManager.getInstance(project)

        val libDir =
            ProjectRootManager.getInstance(project).contentRoots.firstNotNullOfOrNull { it.findChild("lib") }

        if (libDir == null) {
            return emptyArray()
        }

        val scope = GlobalSearchScopesCore.directoryScope(project, libDir, true)

        val files = FilenameIndex.getAllFilesByExt(project, "dart", scope)

        for (virtualFile in files) {
            val psiFile = psiManager.findFile(virtualFile) ?: continue
            val text = psiFile.text
            
            if (!text.contains(providerName) && !text.contains(psiElement.text)) continue

            psiFile.accept(object : DartRecursiveVisitor() {
                override fun visitReferenceExpression(o: DartReferenceExpression) {
                    super.visitReferenceExpression(o)

                    val resolved = o.reference?.resolve()

                    if (resolved != null && resolved.text == psiElement.text) {
                        foundElements.add(o)
                    }
                }

                override fun visitCallExpression(o: DartCallExpression) {
                    super.visitCallExpression(o)

                    val callee = o.expression

                    if (callee !is DartReferenceExpression) return

                    if (callee.text.startsWith("ref.")) {
                        val argumentList = o.arguments?.argumentList ?: return

                        val expressionList = argumentList.expressionList

                        for (exp in expressionList) {
                            if (exp.text.startsWith(providerName)) {
                                foundElements.add(o.element)
                            }
                        }
                    }

                    if (callee.text.startsWith("$providerName.overrideWith")) {
                        foundElements.add(o.element)
                    }
                }
            })
        }

        return foundElements.toTypedArray()
    }
}
