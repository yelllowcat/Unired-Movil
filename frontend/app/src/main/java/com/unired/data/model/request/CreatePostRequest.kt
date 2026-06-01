package com.unired.data.model.request

data class CreatePostRequest(
    val content: String,
    val removeImage: Boolean? = null
)
