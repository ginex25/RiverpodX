package com.github.ginex25.riverpodx.utils

object ProviderNameUtils {
    private val providerRegex = Regex("\\w+Provider$")
    private val wordRegex = Regex("\\w+")

    fun toLowerCaseFirstLetter(input: String): String {
        return input.replaceFirstChar { it.lowercaseChar() }
    }

    // Riverpod automatically generates a provider name by appending "Provider"
    // to the original function name with the first letter in lowercase.
    // We index this generated name to support "go to provider" functionality.
    fun toGeneratedProviderName(base: String): String {
        return "${toLowerCaseFirstLetter(base)}Provider"
    }

    fun getProviderNameAtOffset(text: String, offset: Int): String {
        if (providerRegex.matches(text))
            return text

        if (offset < 0 || offset >= text.length)
            return ""


        var start = offset
        var end = offset

        while (start > 0 && wordRegex.matches(text[start - 1].toString())) {
            start--
        }

        while (end < text.length && wordRegex.matches(text[end].toString())) {
            end++
        }

        return text.substring(start, end)
    }
}