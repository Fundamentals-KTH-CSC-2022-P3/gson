package com.google.gson;

import java.net.URI;

public class JsonSchemaValidator {
    public static void validate(JsonElement schema) throws SchemaValidationException {
        validateRootObject(schema);
    }

    static void validateRootObject(JsonElement schema) throws SchemaValidationException {
        if (validateSchemaRootType(schema)) return;
        JsonObject schemaRoot = schema.getAsJsonObject();

        validateTypeFieldAsObject(schemaRoot);
        validateOptionalURIField("$schema", schemaRoot);
        validateOptionalURIField("$id", schemaRoot);
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
            case NULL:
                break;
        }
    }

    static void validateObject(JsonObject schemaNode) throws SchemaValidationException {
        try {
            JsonObject childNode = schemaNode.getAsJsonObject("properties");

            if(schemaNode.has("required")) {
                //TODO: Verify that the array contains only keys from properties
            }
            for (String key : childNode.keySet()) {
                if(validateSchemaRootType(childNode.get(key)))
                    continue;
                validateRootObject(childNode.get(key));
            }
        } catch (Throwable ex) {
            throw new SchemaValidationException(ex);
        }
    }

    static void validateArray(JsonObject schemaNode) throws SchemaValidationException {

    }

    static void validateTypeField(JsonObject schema) throws SchemaValidationException {
        JsonElement type = schema.get("type");
        boolean isArray = type.isJsonArray();
        if (isArray){
            JsonArray array = type.getAsJsonArray();
            if (array.isEmpty())
                throw new SchemaValidationException();
            try {
                for (JsonElement member : array) {
                    MemberTypes.valueOf(member.getAsString().trim().toUpperCase());
                }
            } catch (IllegalArgumentException ex) {
                throw new SchemaValidationException(ex);
            }
            return;
        }
        try {
            MemberTypes.valueOf(type.getAsString().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new SchemaValidationException(ex);
        }
    }

    static void validateTypeFieldAsObject(JsonObject schema) throws SchemaValidationException {
        JsonElement type = schema.get("type");
        MemberTypes memberType = MemberTypes.NULL;
        try {
            String objectType = type.getAsString().trim().toUpperCase();
            memberType = MemberTypes.valueOf(objectType);
        } catch (Throwable ex) {
            throw new SchemaValidationException(ex);
        }

        if (memberType != MemberTypes.OBJECT) {
            throw new SchemaValidationException();
        }
    }

    static void validateOptionalURIField(String memberName, JsonObject schemaRoot) throws SchemaValidationException {
        if (schemaRoot.has(memberName)){
            JsonPrimitive schema = schemaRoot.getAsJsonPrimitive(memberName);
            try{
                URI.create(schema.getAsString());
            } catch (IllegalArgumentException e){
                throw new SchemaValidationException(e);
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
        public SchemaValidationException(Throwable ex){
            super(ex);
        }
    }
}
