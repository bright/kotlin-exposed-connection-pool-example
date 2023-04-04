package pl.brightinventions.dto

data class CreatePersonDto(
    val name: String,
    val surname: String,
    val age: Int,
    val details: CreatePersonDetailsDto
)

data class CreatePersonDetailsDto(
    val nickname: String
)
