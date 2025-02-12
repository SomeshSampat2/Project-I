package com.innovatelabs3.projectI2.domain.models

data class EmailContent(
    val to: String,
    val subject: String = "",
    val body: String = "",
    val isHtml: Boolean = false
)