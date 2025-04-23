package com.github.ginex25.riverpodx.navigation

import com.intellij.find.findUsages.FindUsagesHandler
import com.intellij.find.findUsages.FindUsagesHandlerFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.dart.psi.DartComponent
import com.jetbrains.lang.dart.psi.DartMetadata
import com.jetbrains.lang.dart.psi.impl.DartComponentNameImpl

class CustomFindUsageHandlerFactory : FindUsagesHandlerFactory() {
    override fun canFindUsages(element: PsiElement): Boolean {
        if (element !is DartComponentNameImpl) return false

        val dartClass = element.parent as? DartComponent ?: return false

        val annotations = PsiTreeUtil.findChildrenOfType(dartClass, DartMetadata::class.java)
        val hasRiverpodAnnotation = annotations.any { it.text.contains("@riverpod", true) }

        return hasRiverpodAnnotation
    }

    override fun createFindUsagesHandler(element: PsiElement, forHighlightUsages: Boolean): FindUsagesHandler {
        return CustomFindUsageHandler(element)
    }
}