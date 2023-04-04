package pl.brightinventions.exposed

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject

class JsonbColumnType(
    private val serialize: (Any) -> String,
    private val deserialize: (String) -> Any
) : ColumnType() {
    override fun sqlType() = "JSON"

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        super.setParameter(
            stmt,
            index,
            value.let {
                PGobject().apply {
                    this.type = sqlType()
                    this.value = value as String?
                }
            }
        )
    }

    override fun valueFromDB(value: Any): Any {
        if (value !is PGobject) {
            return value
        }
        return deserialize(checkNotNull(value.value))
    }

    override fun valueToString(value: Any?): String = when (value) {
        is Iterable<*> -> nonNullValueToString(value)
        else -> super.valueToString(value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun notNullValueToDB(value: Any): String = serialize(value)
}

fun <T: Any> Table.jsonValue(name: String, serialize: (Any) -> String, deserialize: (String) -> Any): Column<T> =
    registerColumn(name, JsonbColumnType(serialize, deserialize))
