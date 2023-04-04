package pl.brightinventions.persistance.person

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.IntIdTable
import pl.brightinventions.exposed.jsonValue

object PersonTable : IntIdTable("person") {
    val name = text("name")
    val surname = text("surname")
    val age = integer("age")
    val details = jsonValue<PersonDetails>(
        "details",
        { Json.encodeToString(it as PersonDetails) },
        { Json.decodeFromString(it) as PersonDetails }
    )
}

@Serializable
data class PersonDetails(
    val nickname: String
)

object AddressTable : IntIdTable("address") {
    val personId = reference("person_id", PersonTable.id)
    val street = text("street")
    val house = text("house")
    val apartment = text("apartment")
    val city = text("city")
    val postalCode = text("postal_code")
}
