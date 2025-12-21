package com.github.ginex25.riverpodx.navigation

import com.github.ginex25.riverpodx.index.KEY
import com.github.ginex25.riverpodx.models.ProviderInfo
import com.github.ginex25.riverpodx.utils.ProviderNameUtils.getProviderNameAtOffset
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.ProjectScope
import com.intellij.util.indexing.FileBasedIndex
import java.nio.file.Paths

class CustomGotoDeclarationHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement>? {

        if (sourceElement == null || editor == null)
            return null

        val text = sourceElement.text
        val wordAtCursor = getProviderNameAtOffset(text, offset)

        val project = sourceElement.project
        val providerInfo = getProviderInfos(wordAtCursor, project)

        if (providerInfo.isEmpty())
            return null

        return providerInfo.mapNotNull { info ->
            val path = Paths.get(info.filePath)

            val file = VirtualFileManager.getInstance().findFileByNioPath(path) ?: return@mapNotNull null

            val psiFile: PsiFile = PsiManager.getInstance(project).findFile(file) ?: return@mapNotNull null

            openFileAtOffset(project, psiFile, info.offset)

            psiFile
        }.toTypedArray()
    }

    private fun openFileAtOffset(project: Project, psiFile: PsiFile, offset: Int) {
        ApplicationManager.getApplication().invokeLater {
            OpenFileDescriptor(project, psiFile.virtualFile, offset)
                .navigate(true)
        }
    }


    private fun getProviderInfos(providerName: String, project: Project): List<ProviderInfo> {
        val result = mutableListOf<ProviderInfo>()

        val fileIndex = FileBasedIndex.getInstance()

        val values = fileIndex.getValues(KEY, providerName, ProjectScope.getProjectScope(project))

        values.let {
            result.addAll(it)
        }

        return result
    }
}