package com.google.gson;

public class JsonSchemaMatcher {

    private String schemaUri;
    private String idUri;
    private String title = "";
    private String description = "";
    private String rootType = "";

    public JsonSchemaMatcher(String jsonSchema) {
        JsonParser parser = new JsonParser();
        JsonElement parsed = parser.parse(jsonSchema);
        JsonObject object = parsed.getAsJsonObject();

        setSchemaUri(object.get("$schema").getAsString());
        setIdUri(object.get("$id").getAsString());
        setTitle(object.get("title").getAsString());
        setDescription(object.get("description").getAsString());
        setRootType(object.get("type").getAsString());
    }

    public String getSchemaUri() {
        return schemaUri;
    }

    public void setSchemaUri(String schemaUri) {
        this.schemaUri = schemaUri;
    }

    public String getIdUri() {
        return idUri;
    }

    public void setIdUri(String idUri) {
        this.idUri = idUri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRootType() {
        return rootType;
    }

    public void setRootType(String rootType) {
        this.rootType = rootType;
    }
}
