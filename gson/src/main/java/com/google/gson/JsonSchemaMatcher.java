package com.google.gson;

public class JsonSchemaMatcher {

    private final JsonObject schemaRoot;
    private final JsonElement instanceRoot;

    public JsonSchemaMatcher(String jsonSchema, String jsonInstance) {
        JsonParser parser = new JsonParser();
        schemaRoot = parser.parse(jsonSchema).getAsJsonObject();
        instanceRoot = parser.parse(jsonInstance);
    }

    public String getSchemaUri() {
        return schemaRoot.get("$schema").getAsString();
    }

    public String getIdUri() {
        return schemaRoot.get("$id").getAsString();
    }

    public String getTitle() {
        return schemaRoot.get("title").getAsString();
    }

    public String getRootDescription() {
        return schemaRoot.get("description").getAsString();
    }

    public String getRootType() {
        return schemaRoot.get("type").getAsString();
    }

    private boolean matchingTypes(JsonElement instance, JsonObject schema) {
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
                } catch(NumberFormatException e) {
                    return false;
                }
            case "string":
                if (!instance.isJsonPrimitive())
                    return false;
                return instance.getAsJsonPrimitive().isString();
        }

        return false;
    }

    private boolean objectMatches(JsonElement instance, JsonObject schema) {
        JsonObject instanceObject = instance.getAsJsonObject();

        // If the instanceObject has children then the corresponding schema must have the "properties" property.
        if (instanceObject.size() > 0 && !schema.has("properties"))
            return false;

        // Check that the instance has all the required properties.
        if (schema.has("required")) {
            JsonArray requiredArray = schema.getAsJsonArray("required");

            for (JsonElement required : requiredArray) {
                if (!instanceObject.has(required.getAsString()))
                    return false;
            }
        }

        JsonObject properties = schema.getAsJsonObject("properties");

        // Check that each property in the instance object matches against the schema file.
        for (String key : instanceObject.keySet()) {
            if (!properties.has(key))
                return false;

            // Assume properties.get(key) is an JSON object.
            if (!matches(instanceObject.get(key), properties.get(key).getAsJsonObject()))
                return false;
        }

        return true;
    }

    private boolean arrayMatches(JsonElement instance, JsonObject schema) {
        return false;
    }

    public boolean matches() {
        return matches(instanceRoot, schemaRoot);
    }

    public boolean matches(JsonElement instance, JsonObject schema) {
        if (!matchingTypes(instance, schema))
            return false;

        if (instance.isJsonObject()) {
            return objectMatches(instance, schema);
        } else if(instance.isJsonArray()) {
            return arrayMatches(instance, schema);
        } else {
            return false;
        }
    }
}
