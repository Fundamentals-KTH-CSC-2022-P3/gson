package com.google.gson;

import java.net.URI;
import java.util.HashMap;

public abstract class JsonSchemaValidator {
    public static void validate(JsonElement schema) throws SchemaValidationException {
        if (validateSchemaRootType(schema)) return;
        JsonObject schemaRoot = schema.getAsJsonObject();
        verifyTypeField(schemaRoot, MemberTypes.OBJECT);
        validateSchemaNode(schema);
    }

    static boolean validateSchemaRootType(JsonElement schema) {
        boolean isObject = schema.isJsonObject();
        if (!isObject){
            // if schema root is not an Object, it must be a boolean
            schema.getAsBoolean();
            return true;
        }
        return false;
    }

    static void validateSchemaNode(JsonElement schemaNode) throws SchemaValidationException {
        if(!schemaNode.isJsonObject()) {
            throw new SchemaValidationException("Schema node is not a JSON Object");
        }

        JsonObject object = schemaNode.getAsJsonObject();
        validateOptionalURIField("$schema", object);
        validateOptionalURIField("$id", object);
        validateTypeField(object);

        JsonElement types = object.get("type");
        if (types.isJsonArray()) {
            JsonArray list = types.getAsJsonArray();
            if (list.isEmpty()) {
                throw new SchemaValidationException("Type not allowed to be an empty array");
            }
            for (JsonElement type : types.getAsJsonArray()) {
                String cleanedType = type.getAsString().trim().toUpperCase();
                validateMemberType(MemberTypes.valueOf(cleanedType), object);
            }
            return;
        }
        String cleanedType = types.getAsString().trim().toUpperCase();
        validateMemberType(MemberTypes.valueOf(cleanedType), object);
    }

    static void validateMemberType(MemberTypes memberType, JsonObject schemaNode) throws SchemaValidationException{
        switch (memberType) {
            case STRING:
                break;
            case NUMBER:
                break;
            case INTEGER:
                break;
            case OBJECT:
                validateObject(schemaNode);
                break;
            case ARRAY:
                validateArray(schemaNode);
                break;
            case BOOLEAN:
                break;
            default:
                break;
        }
    }

    static void validateObject(JsonObject schemaNode) throws SchemaValidationException {
        try {
            JsonObject childNode = schemaNode.getAsJsonObject("properties");
            HashMap<String, String> propertyFields = new HashMap<>();
            for (String key : childNode.keySet()) {
                if(validateSchemaRootType(childNode.get(key)))
                    continue;
                validateSchemaNode(childNode.get(key));
                propertyFields.put(key, key);
            }
            if(schemaNode.has("required")) {
                JsonArray requiredFields = schemaNode.getAsJsonArray("required");
                for (JsonElement element : requiredFields) {
                    assert(propertyFields.containsKey(element.getAsString()));
                }
            }
        } catch (SchemaValidationException ex) {
            throw ex;
        } catch (AssertionError ex) {
            throw new SchemaValidationException("Required properties not present in schema");
        } catch (Throwable ex) {
            throw new SchemaValidationException(ex);
        }
    }

    static void validateArray(JsonObject schemaNode) throws SchemaValidationException {

    }

    static void validateTypeField(JsonObject schema) throws SchemaValidationException {
        JsonElement type = schema.get("type");
        if (type == null){
            throw new SchemaValidationException("Type not defined");
        }
        boolean isArray = type.isJsonArray();
        if (isArray){
            JsonArray array = type.getAsJsonArray();
            if (array.isEmpty())
                throw new SchemaValidationException();
            try {
                for (JsonElement member : array) {
                    //TODO Add recursive array parsing
                    MemberTypes.valueOf(member.getAsString().trim().toUpperCase());
                }
            } catch (IllegalArgumentException ex) {
                throw new SchemaValidationException("Type not allowed", ex);
            }
            return;
        }
        try {
            MemberTypes.valueOf(type.getAsString().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new SchemaValidationException("Type not allowed", ex);
        }
    }

    static void verifyTypeField(JsonObject schemaNode, MemberTypes requiredType) throws SchemaValidationException {
        JsonElement type = schemaNode.get("type");
        MemberTypes memberType;
        try {
            String objectType = type.getAsString().trim().toUpperCase();
            memberType = MemberTypes.valueOf(objectType);
        } catch (Throwable ex) {
            throw new SchemaValidationException(ex);
        }

        if (memberType != requiredType) {
            throw new SchemaValidationException();
        }
    }

    static void validateOptionalURIField(String memberName, JsonObject schemaRoot) throws SchemaValidationException {
        if (schemaRoot.has(memberName)){
            JsonPrimitive schema = schemaRoot.getAsJsonPrimitive(memberName);
            try{
                URI.create(schema.getAsString());
            } catch (IllegalArgumentException e){
                throw new SchemaValidationException(memberName + " is not a valid URI", e);
            }
        }
    }

    enum MemberTypes{
        STRING,
        NUMBER,
        INTEGER,
        OBJECT,
        ARRAY,
        BOOLEAN,
        NULL
    }

    static class SchemaValidationException extends Exception{
        public SchemaValidationException(){
            super();
        }
        public SchemaValidationException(String message) {
            super(message);
        }
        public SchemaValidationException(String message, Throwable throwable) {
            super(message, throwable);
        }
        public SchemaValidationException(Throwable ex){
            super(ex);
        }
    }
}
