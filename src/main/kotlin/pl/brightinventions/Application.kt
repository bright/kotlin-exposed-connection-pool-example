package pl.brightinventions
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import pl.brightinventions.dto.CreateAddressDto
import pl.brightinventions.dto.CreatePersonDto
import pl.brightinventions.exposed.Database
import pl.brightinventions.persistance.person.AddressTable
import pl.brightinventions.persistance.person.PersonRepository
import pl.brightinventions.persistance.person.PersonTable

fun main(args: Array<String>): Unit = EngineMain.main(args)
//fun main() {
//    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
//        install(ContentNegotiation) {
//            json()
//        }
//    }
//        .start(wait = true)
//}

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
            post {
                call.respond(repository.create(call.receive()))
            }
            delete("/{id}") {
                call.respond(repository.delete(call.parameters["id"]?.toInt()!!))
            }
            put("/{id}") {
                call.respond(repository.update(call.parameters["id"]?.toInt()!!, call.receive()))
            }
        }
    }
}

fun Application.data() {
    Database.register(environment.config)
    val repository = PersonRepository()
    transaction {
        SchemaUtils.create(PersonTable)
        SchemaUtils.create(AddressTable)
        val john = repository.create(CreatePersonDto("John", "Doe", 33))
        repository.addAddress(john, CreateAddressDto(
            "ul. Jana Matejki", "12", "1", "Gdansk", "80-232"
        )
        )
        repository.addAddress(john, CreateAddressDto(
            "ul. Jana Matejki", "13", "1", "Gdansk", "80-232"
        )
        )
        repository.create(CreatePersonDto("George", "Smith", 34))
        repository.create(CreatePersonDto("Megan", "Miller", 22))
    }
}
