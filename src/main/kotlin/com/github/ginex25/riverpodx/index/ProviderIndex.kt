package com.github.ginex25.riverpodx.index

import com.github.ginex25.riverpodx.models.ProviderInfo
import com.github.ginex25.riverpodx.utils.ProviderNameUtils.toGeneratedProviderName
import com.github.ginex25.riverpodx.utils.ProviderNameUtils.toLowerCaseFirstLetter
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import java.io.DataInput
import java.io.DataOutput


class ProviderIndex : FileBasedIndexExtension<String, ProviderInfo>() {


    private val providerNameRegex = Regex("@riverpod\\s+[A-Za-z<>\\d]+\\s+(\\w+)", RegexOption.MULTILINE)

    override fun getName(): ID<String, ProviderInfo> {
        return KEY
    }

    override fun getIndexer(): DataIndexer<String, ProviderInfo, FileContent> {
        return DataIndexer { inputData ->
            val result = mutableMapOf<String, ProviderInfo>()

            val content = inputData.contentAsText.toString()

            providerNameRegex.findAll(content).forEach { match ->
                val providerMatch = match.groups[1] ?: return@forEach

                val originalName = providerMatch.value

                val lowerCaseOriginalName = toLowerCaseFirstLetter(originalName)

                val generatedName = toGeneratedProviderName(lowerCaseOriginalName)

                val lineNumber = inputData.psiFile.fileDocument.getLineNumber(providerMatch.range.last) + 1

                result[generatedName] = ProviderInfo(inputData.file.path, lineNumber)
            }

            result
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): DataExternalizer<ProviderInfo> {
        return object : DataExternalizer<ProviderInfo> {
            override fun save(out: DataOutput, value: ProviderInfo) {
                out.writeUTF(value.filePath)
                out.writeInt(value.lineNumber)
            }

            override fun read(`in`: DataInput): ProviderInfo {
                return ProviderInfo(`in`.readUTF(), `in`.readInt())
            }
        }
    }


    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { file -> file.extension == "dart" }
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    override fun getVersion(): Int {
        return 1
    }
}

val KEY: ID<String, ProviderInfo> = ID.create("com.github.ginex25.riverpodx.index")