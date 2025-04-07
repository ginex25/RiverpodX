package com.github.ginex25.riverpodx.intentions

import com.github.ginex25.riverpodx.utils.ConvertIntentionsUtils
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

class ConvertConsumerToConsumerStatefulIntention : PsiElementBaseIntentionAction(), IntentionAction {
    private val convertIntentionsUtils = ConvertIntentionsUtils()

    override fun getFamilyName(): String = text

    override fun getText(): String = "Convert ConsumerWidget to ConsumerStatefulWidget"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return convertIntentionsUtils.isConsumerWidget(element)
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (editor == null) return


        convertIntentionsUtils.convertConsumerToConsumerStateful(
            project,
            editor,
            element
        )
    }
}