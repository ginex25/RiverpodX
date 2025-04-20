package com.github.ginex25.riverpodx.intentions

import com.github.ginex25.riverpodx.utils.ConvertIntentionsUtils
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

class ConvertToConsumerIntention : PsiElementBaseIntentionAction(), IntentionAction {
    private val convertIntentionsUtils = ConvertIntentionsUtils()

    override fun getFamilyName(): String = text

    override fun getText(): String = "Convert to ConsumerStateful"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return convertIntentionsUtils.isConsumerStatefulWidget(element) || convertIntentionsUtils.isStatefulWidget(
            element
        )
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (editor == null) return


        convertIntentionsUtils.convertToConsumer(
            project, editor, element
        )
    }
}