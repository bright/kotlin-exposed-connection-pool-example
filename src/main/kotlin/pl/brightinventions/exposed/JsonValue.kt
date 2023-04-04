package pl.brightinventions.exposed

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.vendors.PostgreSQLDialect
import org.jetbrains.exposed.sql.vendors.currentDialect
import kotlin.reflect.KClass

inline fun <reified T : Any> Column<*>.jsonValue(vararg jsonPath: String): JsonValue<T> = this.jsonValue(T::class, *jsonPath)

@Suppress("CyclomaticComplexMethod")
fun <T : Any> Column<*>.jsonValue(clazz: KClass<T>, vararg jsonPath: String): JsonValue<T> {
    val columnType = when (clazz) {
        Boolean::class -> BooleanColumnType()
        Int::class -> IntegerColumnType()
        Float::class -> FloatColumnType()
        Long::class -> LongColumnType()
        String::class -> TextColumnType()
        else -> TextColumnType()
    }

    return when (currentDialect) {
        is PostgreSQLDialect -> PostgreSQLJsonValue(this, columnType, jsonPath.toList())
        else -> throw NotImplementedError()
    }
}

class PostgreSQLJsonValue<T>(
    private val expr: Expression<*>,
    override val columnType: ColumnType,
    private val jsonPath: List<String>
) : JsonValue<T>(columnType) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        currentDialect is PostgreSQLDialect
        append("(")
        append(expr)
        append("${jsonPath.joinToString { it }})::${columnType.sqlType()}")
    }
}

abstract class JsonValue<T>(
    columnType: ColumnType
) : Function<T>(columnType)
