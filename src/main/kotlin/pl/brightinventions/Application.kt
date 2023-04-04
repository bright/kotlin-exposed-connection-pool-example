package pl.brightinventions
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import pl.brightinventions.dto.CreateAddressDto
import pl.brightinventions.dto.CreatePersonDetailsDto
import pl.brightinventions.dto.CreatePersonDto
import pl.brightinventions.exposed.Database
import pl.brightinventions.persistance.person.AddressTable
import pl.brightinventions.persistance.person.PersonRepository
import pl.brightinventions.persistance.person.PersonTable

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.routing() {
    val repository = PersonRepository()

    routing {
        route("/person") {
            get {
                call.respond(repository.findAll())
            }
            get("/{id}") {
                val found = repository.find(call.parameters["id"]?.toInt()!!)
                found?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound)
            }
            get("/nickname/{nickname}") {
                val found = repository.findByNickname(call.parameters["nickname"]!!)
                found?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

fun Application.data() {
    Database.register(environment.config)
    val repository = PersonRepository()
    transaction {
        SchemaUtils.drop(PersonTable, AddressTable)
        SchemaUtils.create(PersonTable, AddressTable)
        val john = repository.create(CreatePersonDto("John", "Doe", 33, CreatePersonDetailsDto("johny")))
        repository.addAddress(john, CreateAddressDto(
            "ul. Jana Matejki", "12", "1", "Gdansk", "80-232"
        )
        )
        repository.addAddress(john, CreateAddressDto(
            "ul. Jana Matejki", "13", "1", "Gdansk", "80-232"
        )
        )
        repository.create(CreatePersonDto("George", "Smith", 34, CreatePersonDetailsDto("smithy")))
        repository.create(CreatePersonDto("Megan", "Miller", 22, CreatePersonDetailsDto("mgn")))
    }
}

fun Application.contentNegotiation() {
    install(ContentNegotiation) {
        json()
    }
}
