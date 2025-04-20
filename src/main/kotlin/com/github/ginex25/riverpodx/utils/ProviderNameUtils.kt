package com.github.ginex25.riverpodx.utils

object ProviderNameUtils {
    private val providerRegex = Regex("\\w+Provider$")
    private val wordRegex = Regex("\\w+")

    fun toLowerCaseFirstLetter(input: String): String {
        return input.replaceFirstChar { it.lowercaseChar() }
    }

    /**
     * Generates the provider name for a given base name according to Riverpod conventions.
     *
     * Riverpod appends "Provider" to the original function name,
     * using a lowercase first letter of the base name.
     *
     * Example: `MyFunction` -> `myFunctionProvider`
     *
     * @param base The original function name.
     * @return The generated provider name.
     */
    fun toGeneratedProviderName(base: String): String {
        return "${toLowerCaseFirstLetter(base)}Provider"
    }

    /**
     * Extracts the provider name from the given text at a specific offset.
     *
     * - If the entire text matches the provider pattern, it is returned as-is.
     * - Otherwise, searches for a word around the given offset using `wordRegex`.
     * - Returns the word found, or an empty string if none is found.
     *
     * @param text The input text.
     * @param offset The character offset within the text.
     * @return The provider name at the offset, or an empty string.
     */
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