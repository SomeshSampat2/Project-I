package com.innovatelabs3.projectI2.domain.models

data class ProductSearchContent(
    val query: String,
    val platform: String = "flipkart" // "flipkart" or "amazon"
)