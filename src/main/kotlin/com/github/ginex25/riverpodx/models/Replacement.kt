package com.github.ginex25.riverpodx.models

data class Replacement(
    val startOffset: Int,
    val endOffset: Int,
    val name: String,
    val originalText: String,
)