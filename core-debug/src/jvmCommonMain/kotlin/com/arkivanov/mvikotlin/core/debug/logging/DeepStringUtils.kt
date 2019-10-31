package com.arkivanov.mvikotlin.core.debug.logging

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.json
import java.lang.reflect.Field
import java.lang.reflect.TypeVariable
import kotlin.math.min

private val BLACK_LIST_FIELDS = hashSetOf("serialVersionUID", "INSTANCE")

internal actual fun Any.toDeepString(mode: DeepStringMode, format: Boolean): String =
    toJsonValue(mode, HashSet()).let { jsonValue ->
        when (jsonValue) {
            is JsonValue.Number -> jsonValue.value.toString()
            is JsonValue.Boolean -> jsonValue.value.toString()
            is JsonValue.String -> "\"${jsonValue.value}\""

            is JsonValue.JsonElement ->
                jsonValue.value.let {
                    if (format) Json.indented.stringify(JsonElement.serializer(), it) else it.toString()
                }
        }
    }

private fun Any.toJsonValue(mode: DeepStringMode, visitedObjects: MutableSet<Any>): JsonValue =
    if (visitedObjects.contains(this)) {
        JsonValue.String("Recursive reference")
    } else {
        visitedObjects.add(this)
        try {
            when (this) {
                is String -> JsonValue.String(if ((mode === DeepStringMode.SHORT) && (length > 64)) "${substring(0, 64)}…" else this)
                is Int -> JsonValue.Number(this)
                is Long -> JsonValue.Number(this)
                is Float -> JsonValue.Number(this.toDouble())
                is Double -> JsonValue.Number(this)
                is Boolean -> JsonValue.Boolean(this)
                is Char -> JsonValue.String(this.toString())
                is Short -> JsonValue.Number(this.toInt())
                is Byte -> JsonValue.Number(this.toInt())
                is Enum<*> -> JsonValue.String(this.name)
                else -> JsonValue.JsonElement(toJson(mode, visitedObjects))
            }
        } finally {
            visitedObjects.remove(this)
        }
    }

private fun Any.toJson(mode: DeepStringMode, visitedObjects: MutableSet<Any>): JsonObject =
    json {
        when (this@toJson) {
            is Iterable<*> -> putValues(iterator(), mode, visitedObjects)
            is Map<*, *> -> putValues(this@toJson, mode, visitedObjects)
            is Array<*> -> putValues(iterator(), mode, visitedObjects)
            is IntArray -> putValues(this@toJson, mode)
            is LongArray -> putValues(this@toJson, mode)
            is FloatArray -> putValues(this@toJson, mode)
            is DoubleArray -> putValues(this@toJson, mode)
            is BooleanArray -> putValues(this@toJson, mode)
            is CharArray -> putValues(this@toJson, mode)
            is ShortArray -> putValues(this@toJson, mode)
            is ByteArray -> putValues(this@toJson, mode)
            else -> putValues(this@toJson, mode, visitedObjects)
        }
    }

private fun JsonObjectBuilder.putValues(iterator: Iterator<*>, mode: DeepStringMode, visitedObjects: MutableSet<Any>) {
    val limit: Int? = getArrayLimit(mode)
    var index = 0
    while (((limit == null) || (index < limit)) && iterator.hasNext()) {
        putTypedValue(index.toString(), iterator.next(), null, mode, visitedObjects)
        index++
    }
}

private fun getArrayLimit(mode: DeepStringMode): Int? =
    when (mode) {
        DeepStringMode.SHORT -> 0
        DeepStringMode.MEDIUM -> 10
        DeepStringMode.FULL -> null
    }

private fun JsonObjectBuilder.putValues(map: Map<*, *>, mode: DeepStringMode, visitedObjects: MutableSet<Any>) {
    val limit: Int? = getArrayLimit(mode)
    var index = 0
    val iterator = map.entries.iterator()

    while (((limit == null) || (index < limit)) && iterator.hasNext()) {
        val (key: Any?, value: Any?) = iterator.next()
        val entryJson =
            json {
                putTypedValue("key", key, null, mode, visitedObjects)
                putTypedValue("value", value, null, mode, visitedObjects)
            }
        put("$index", entryJson)
        index++
    }
}

private fun JsonObjectBuilder.putTypedValue(
    prefix: String,
    value: Any?,
    valueClass: Class<*>?,
    mode: DeepStringMode,
    visitedObjects: MutableSet<Any>
) {
    putValue("$prefix: ${getFullTypeName(value, valueClass)}", value?.toJsonValue(mode, visitedObjects))
}

private fun JsonObjectBuilder.putValue(name: String, value: JsonValue?): JsonObjectBuilder =
    when (value) {
        is JsonValue.Number -> put(name, value.value)
        is JsonValue.Boolean -> put(name, value.value)
        is JsonValue.String -> put(name, value.value)
        is JsonValue.JsonElement -> put(name, value.value)
        null -> putNull(name)
    }

private fun JsonObjectBuilder.putValues(array: IntArray, mode: DeepStringMode) {
    for (index in 0 until (getArrayLimit(mode)?.let { min(it, array.size) } ?: array.size)) {
        put("$index", array[index])
    }
}

private fun JsonObjectBuilder.putValues(array: LongArray, mode: DeepStringMode) {
    for (index in 0 until (getArrayLimit(mode)?.let { min(it, array.size) } ?: array.size)) {
        put("$index", array[index])
    }
}

private fun JsonObjectBuilder.putValues(array: FloatArray, mode: DeepStringMode) {
    for (index in 0 until (getArrayLimit(mode)?.let { min(it, array.size) } ?: array.size)) {
        put("$index", array[index])
    }
}

private fun JsonObjectBuilder.putValues(array: DoubleArray, mode: DeepStringMode) {
    for (index in 0 until (getArrayLimit(mode)?.let { min(it, array.size) } ?: array.size)) {
        put("$index", array[index])
    }
}

private fun JsonObjectBuilder.putValues(array: BooleanArray, mode: DeepStringMode) {
    for (index in 0 until (getArrayLimit(mode)?.let { min(it, array.size) } ?: array.size)) {
        put("$index", array[index])
    }
}

private fun JsonObjectBuilder.putValues(array: CharArray, mode: DeepStringMode) {
    for (index in 0 until (getArrayLimit(mode)?.let { min(it, array.size) } ?: array.size)) {
        put("$index", array[index].toString())
    }
}

private fun JsonObjectBuilder.putValues(array: ShortArray, mode: DeepStringMode) {
    for (index in 0 until (getArrayLimit(mode)?.let { min(it, array.size) } ?: array.size)) {
        put("$index", array[index])
    }
}

private fun JsonObjectBuilder.putValues(array: ByteArray, mode: DeepStringMode) {
    for (index in 0 until (getArrayLimit(mode)?.let { min(it, array.size) } ?: array.size)) {
        put("$index", array[index])
    }
}

private val Class<*>.allFields: List<Field>
    get() {
        val list = ArrayList<Field>()
        var cls: Class<*>? = this
        while ((cls != null) && (cls != Object::class.java)) {
            list += cls.declaredFields
            cls = cls.superclass
        }

        return list
    }


private fun JsonObjectBuilder.putValues(obj: Any, mode: DeepStringMode, visitedObjects: MutableSet<Any>) {
    if (obj is Throwable) {
        putTypedValue("message", obj.message, String::class.java, mode, visitedObjects)
        putTypedValue("cause", obj.cause, Throwable::class.java, mode, visitedObjects)
        if (mode === DeepStringMode.FULL) {
            putTypedValue("stackTrace", obj.stackTrace, null, mode, visitedObjects)
        }

        return
    }

    obj.javaClass.allFields.forEach { field ->
        val isAccessible = field.isAccessible
        if (!isAccessible) {
            field.isAccessible = true
        }
        try {
            val fieldName = field.name
            if (fieldName.isAllowedFieldName()) {
                when (field.type) {
                    Int::class.javaPrimitiveType -> put("$fieldName: int", field.getInt(obj))
                    Long::class.javaPrimitiveType -> put("$fieldName: long", field.getLong(obj))
                    Float::class.javaPrimitiveType -> put("$fieldName: float", field.getFloat(obj))
                    Double::class.javaPrimitiveType -> put("$fieldName: double", field.getDouble(obj))
                    Boolean::class.javaPrimitiveType -> put("$fieldName: boolean", field.getBoolean(obj))
                    Char::class.javaPrimitiveType -> put("$fieldName: char", field.getChar(obj).toString())
                    Short::class.javaPrimitiveType -> put("$fieldName: short", field.getShort(obj))
                    Byte::class.javaPrimitiveType -> put("$fieldName: byte", field.getByte(obj))
                    else -> putTypedValue(fieldName, field.get(obj), field.type, mode, visitedObjects)
                }
            }
        } finally {
            if (!isAccessible) {
                field.isAccessible = false
            }
        }
    }
}

private fun String.isAllowedFieldName(): Boolean = !startsWith("$") && !BLACK_LIST_FIELDS.contains(this)

private fun getFullTypeName(value: Any?, valueClass: Class<*>? = null): String {
    val clazz = value?.javaClass ?: valueClass ?: return "?"

    if (value != null) {
        if (clazz.isArray) {
            return clazz.simpleName.replaceFirst("[]", "[${java.lang.reflect.Array.getLength(value)}]")
        }

        if (value is Collection<*>) {
            return "${clazz.toTypeNameWithGenerics(value)}(${value.size})"
        }
    }

    return clazz.toTypeNameWithGenerics(value)
}

private fun Class<*>.toTypeNameWithGenerics(value: Any?): String =
    (value?.javaClass ?: this).let { clazz ->
        clazz
            .typeParameters
            .takeUnless(kotlin.Array<*>::isEmpty)
            ?.joinToString(separator = ", ", prefix = "<", postfix = ">", transform = TypeVariable<*>::getName)
            ?.let { "${clazz.simpleName}$it" }
            ?: clazz.simpleName
    }

private fun JsonObjectBuilder.put(name: String, value: Boolean): JsonObjectBuilder {
    name to value

    return this
}

private fun JsonObjectBuilder.put(name: String, value: Number?): JsonObjectBuilder {
    name to value

    return this
}

private fun JsonObjectBuilder.put(name: String, value: String?): JsonObjectBuilder {
    name to value

    return this
}

private fun JsonObjectBuilder.put(name: String, value: JsonElement): JsonObjectBuilder {
    name to value

    return this
}

private fun JsonObjectBuilder.putNull(name: String): JsonObjectBuilder = put(name, null as String?)

private sealed class JsonValue {
    class Number(val value: kotlin.Number) : JsonValue()
    class Boolean(val value: kotlin.Boolean) : JsonValue()
    class String(val value: kotlin.String) : JsonValue()
    class JsonElement(val value: kotlinx.serialization.json.JsonElement) : JsonValue()
}
