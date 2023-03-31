package pl.brightinventions.exposed

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database

object Database {

    fun register(config: ApplicationConfig) {
        Database.connect(
            hikari(
                config.property("db.url").getString(),
                config.property("db.user").getString(),
                config.property("db.password").getString(),
                config.property("db.maximumPoolSize").getString().toInt()
            )
        )
    }

    private fun hikari(dbUrl: String, dbUser: String, dbPassword: String, maximumPoolSize: Int): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = dbUrl
        config.username = dbUser
        config.password = dbPassword
        config.maximumPoolSize = maximumPoolSize
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }
}
