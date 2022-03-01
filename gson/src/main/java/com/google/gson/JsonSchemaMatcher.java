package com.google.gson;

public class JsonSchemaMatcher {

    private final JsonObject schemaRoot;

    public JsonSchemaMatcher(String jsonSchema) {
        JsonParser parser = new JsonParser();
        JsonElement parsed = parser.parse(jsonSchema);
        schemaRoot = parsed.getAsJsonObject();
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
