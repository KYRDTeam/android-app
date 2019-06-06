package com.kyberswap.android.data.api.limitorder

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class OrderListDeserializer : JsonDeserializer<List<OrderEntity>> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<OrderEntity>? {
        val entities = mutableListOf<OrderEntity>()
        val jsonObject = json.asJsonObject
        val fields = jsonObject.getAsJsonArray("fields")
        val orders = jsonObject.getAsJsonArray("orders")

        for (i in 0 until orders.size()) {
            val jsonOrder = orders.get(i).asJsonArray
            entities.add(jsonOrder.toOrderEntity(fields))

        }
        return entities
    }
}
