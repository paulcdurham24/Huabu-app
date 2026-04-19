package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Language { KOTLIN, JAVA, PYTHON, JAVASCRIPT, TYPESCRIPT, SWIFT, GO, RUST, CPP, CSHARP, HTML, CSS, SQL, BASH, OTHER }

@Entity(tableName = "code_snippets")
data class CodeSnippet(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val code: String,
    val language: Language = Language.KOTLIN,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
