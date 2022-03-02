package com.google.gson;

import java.util.HashSet;
import java.util.Set;

public class JsonSchemaMatcher {

    private final JsonElement schemaRoot;
    private final JsonElement instanceRoot;

    public JsonSchemaMatcher(String jsonSchema, String jsonInstance) {
        JsonParser parser = new JsonParser();
        schemaRoot = parser.parse(jsonSchema);
        instanceRoot = parser.parse(jsonInstance);
    }

    private boolean matchingTypes(JsonElement instance, JsonObject schema) {
        // If the sub-schema does not have a "type" property then the instance can have any type.
        if (!schema.has("type"))
            return true;

        String type = schema.get("type").getAsString();

        switch (type) {
            case "object":
                return instance.isJsonObject();
            case "array":
                return instance.isJsonArray();
            case "null":
                return instance.isJsonNull();
            case "boolean":
                if (!instance.isJsonPrimitive())
                    return false;
                return instance.getAsJsonPrimitive().isBoolean();
            case "number":
                if (!instance.isJsonPrimitive())
                    return false;
                return instance.getAsJsonPrimitive().isNumber();
            case "integer":
                if (!instance.isJsonPrimitive())
                    return false;
                if (!instance.getAsJsonPrimitive().isNumber())
                    return false;
                try {
                    Integer.parseInt(instance.getAsJsonPrimitive().getAsString());
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "string":
                if (!instance.isJsonPrimitive())
                    return false;
                return instance.getAsJsonPrimitive().isString();
        }

        return false;
    }

    private boolean objectMatches(JsonObject instanceObject, JsonObject schema) {
        // Check that the instance has all the required properties.
        if (schema.has("required")) {
            JsonArray requiredArray = schema.getAsJsonArray("required");

            for (JsonElement required : requiredArray) {
                if (!instanceObject.has(required.getAsString()))
                    return false;
            }
        }

        // If the sub-schema does not have a "properties" property then the instance can have any properties it wants.
        if (!schema.has("properties"))
            return true;

        JsonObject properties = schema.getAsJsonObject("properties");

        // Check that each property in the sub-schema matches against the instance properties.
        for (String key : properties.keySet()) {

            // The instance is not required to have all the properties defined in the sub-schema.
            if (!instanceObject.has(key))
                continue;

            if (!matches(instanceObject.get(key), properties.getAsJsonObject(key)))
                return false;
        }

        return true;
    }

    private boolean arrayMatches(JsonArray instanceArray, JsonObject schema) {
        // Check if the sub-schema has the "minItems" property and then the array must have
        // at least as many elements specified by the "minItems" property.
        if (schema.has("minItems") && instanceArray.size() < schema.get("minItems").getAsInt())
            return false;

        // Check if the sub-schema has the "uniqueItems" property, if so, all elements in the array must be unique.
        if (schema.has("uniqueItems")) {
            Set<JsonElement> found = new HashSet<>();

            for (JsonElement element : instanceArray) {
                // Check if the element has already been found, if so the element is not unique.
                if (found.contains(element))
                    return false;
                found.add(element);
            }
        }

        // If the sub-schema does not have the "items" property then the array can contain any elements.
        if (!schema.has("items"))
            return true;

        JsonElement items = schema.get("items");

        // Special case if the value of the items key is set to false.
        if (items.isJsonPrimitive() && !items.getAsBoolean())
            return instanceArray.isEmpty();

        // Check that each element in the instance matches against the "items" object.
        for (JsonElement element : instanceArray) {
            if (!matches(element, items.getAsJsonObject()))
                return false;
        }

        return true;
    }

    private boolean primitiveMatches(JsonPrimitive instancePrimitive, JsonObject schema) {
        return false;
    }

    public boolean matches() {
        return matches(instanceRoot, schemaRoot);
    }

    public boolean matches(JsonElement instance, JsonElement schema) {
        if (schema.isJsonPrimitive())
            return schema.getAsBoolean();

        // If the sub-schema is not a boolean it must be an object.
        JsonObject schemaObject = schema.getAsJsonObject();

        // Check that the instance has the correct type as defined by the sub-schema.
        if (!matchingTypes(instance, schemaObject))
            return false;

        // Depending on the type of the instance we should perform the matching algorithm differently.
        if (instance.isJsonObject()) {
            return objectMatches(instance.getAsJsonObject(), schemaObject);
        } else if (instance.isJsonArray()) {
            return arrayMatches(instance.getAsJsonArray(), schemaObject);
        } else if (instance.isJsonPrimitive()) {
            return primitiveMatches(instance.getAsJsonPrimitive(), schemaObject);
        } else {
            return false;
        }
    }
}
