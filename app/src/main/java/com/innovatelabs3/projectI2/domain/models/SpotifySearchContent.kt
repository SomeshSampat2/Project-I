package com.innovatelabs3.projectI2.domain.models

data class SpotifySearchContent(
    val query: String,
    val type: String = "track" // "track" or "artist"
)