package com.kyberswap.android.data.api.limitorder

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class OrderDeserializer : JsonDeserializer<LimitOrderResponseEntity> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): LimitOrderResponseEntity? {
        val jsonObject = json.asJsonObject
        val fields = jsonObject.getAsJsonArray("fields")
        val order = jsonObject.getAsJsonArray("order")
        val success = jsonObject.getAsJsonPrimitive("success").asBoolean

        val message = if (!success) {
            jsonObject.getAsJsonObject("message").toMessage()
 else mapOf()

        if (fields == null || order == null) return LimitOrderResponseEntity(
            success = success,
            message = message
        )

        order.toOrderEntity(fields)
        return LimitOrderResponseEntity(
            fields.toList().map {
                it.asString
    , order.toOrderEntity(fields),
            success, message
        )

    }
}
