package pl.brightinventions.persistance.person

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import pl.brightinventions.dto.*
import pl.brightinventions.exposed.jsonValue

class PersonRepository {

    fun findAll(): List<FoundPersonDto> = transaction {
        PersonEntity
            .all()
            .orderBy(PersonTable.details.jsonValue<String>("->>'nickname'") to SortOrder.DESC)
            .with(PersonEntity::addresses)
            .map { foundPerson ->
                FoundPersonDto(
                    foundPerson.id.value,
                    foundPerson.name,
                    foundPerson.surname,
                    foundPerson.age,
                    FoundPersonDetailsDto(foundPerson.details.nickname)
                )
            }
    }

    fun find(id: PersonId): FoundPersonWithAddressDto? = transaction {
        PersonEntity
                .findById(id)
                ?.load(PersonEntity::addresses)?.toFoundPersonWithAddressDto()
    }

    fun findByNickname(nickname: String): FoundPersonWithAddressDto? = transaction {
        PersonEntity
                .find { PersonTable.details.jsonValue<String>("->>'nickname'") eq nickname }
                .firstOrNull()
                ?.load(PersonEntity::addresses)?.toFoundPersonWithAddressDto()
    }

    fun create(person: CreatePersonDto): PersonId = transaction {
        PersonEntity.new {
            name = person.name
            surname = person.surname
            age = person.age
            details = PersonDetails(person.details.nickname)
        }.id.value
    }

    fun addAddress(personId: PersonId, address: CreateAddressDto) {
        transaction {
            PersonEntity.findById(personId)?.let {
                SizedCollection(
                    it.addresses + address.let {
                        AddressEntity.new {
                            city = address.city
                            house = address.house
                            street = address.street
                            postalCode = address.postalCode
                            apartment = address.apartment
                            this.personId = EntityID(personId, PersonTable)
                        }
                    }
                )
            }
        }
    }
}

fun PersonEntity.toFoundPersonWithAddressDto() = FoundPersonWithAddressDto(
    this.id.value,
    this.name,
    this.surname,
    this.age,
    FoundPersonDetailsDto(this.details.nickname),
    this.addresses.map { address ->
        FoundPersonAddressDto(
            address.street, address.house, address.apartment, address.city, address.postalCode
        )
    }
)
