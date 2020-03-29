package it.wemake.covid19Companion.data.models.response

import com.squareup.moshi.Json

/**
 * Basic Character info
 */
data class CharacterResponse(
    val name: String,
    @field:Json(name = "birth_year") val birthYear: String,
    val height: String,
    val url: String
)