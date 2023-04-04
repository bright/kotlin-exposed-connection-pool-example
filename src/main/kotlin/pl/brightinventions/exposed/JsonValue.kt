package pl.brightinventions.exposed

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.vendors.PostgreSQLDialect
import org.jetbrains.exposed.sql.vendors.currentDialect
import kotlin.reflect.KClass

inline fun <reified T : Any> Column<*>.jsonValue(vararg jsonPath: String): Function<T> = this.jsonValue(T::class, *jsonPath)

fun <T : Any> Column<*>.jsonValue(clazz: KClass<T>, vararg jsonPath: String): Function<T> {
    if (this.columnType !is JsonColumnType) {
        throw IllegalArgumentException("Cannot perform jsonValue call on the column which is not related to JsonbColumnType")
    }

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
) : Function<T>(columnType) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        append("(")
        append(expr)
        append("${jsonPath.joinToString { it }})::${columnType.sqlType()}")
    }
}
