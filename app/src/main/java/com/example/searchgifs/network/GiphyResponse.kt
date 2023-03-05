package com.example.searchgifs

data class GiphyResponse (
    val data: List<GiphyGifs>,
    val pagination: Pagination
)

data class GiphyGifs(
    val id: String,
    val title: String,
    val import_datetime: String,
)

data class Pagination(
    val total_count: Int
)