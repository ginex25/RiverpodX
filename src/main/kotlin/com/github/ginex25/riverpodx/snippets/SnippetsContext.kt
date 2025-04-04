package com.github.ginex25.riverpodx.snippets

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType

class SnippetsContext private constructor() : TemplateContextType("RIVERPOD_SNIPPETS", "snippets"){
    override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
        return templateActionContext.file.name.endsWith(".dart")
    }
}
