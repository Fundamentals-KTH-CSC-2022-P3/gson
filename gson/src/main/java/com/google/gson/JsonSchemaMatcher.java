package com.google.gson;

public class JsonSchemaMatcher {

    private final JsonObject schemaRoot;
    private final JsonElement instance;

    public JsonSchemaMatcher(String jsonSchema, String jsonInstance) {
        JsonParser parser = new JsonParser();
        schemaRoot = parser.parse(jsonSchema).getAsJsonObject();
        instance = parser.parse(jsonInstance);
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
}
