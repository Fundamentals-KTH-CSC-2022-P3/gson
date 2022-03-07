package com.google.gson.schema;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;

public abstract class JsonSchemaValidator {
    /**
     * provides validation of an element as a valid JSON schema.
     * @param schema the schema root
     * @throws SchemaValidationException if the schema does not fulfill the requirements specified on
     * https://datatracker.ietf.org/doc/html/draft-bhutton-json-schema-00
     */
    public static void validate(JsonElement schema) throws SchemaValidationException {
        if (validateSchemaRootType(schema)) return;
        JsonObject schemaRoot = schema.getAsJsonObject();
        verifyTypeField(schemaRoot, MemberTypes.OBJECT);
        validateSchemaNode(schema);
    }

    static boolean validateSchemaRootType(JsonElement schema) throws SchemaValidationException {
        boolean isObject = schema.isJsonObject();
        if (!isObject){
            // if schema root is not an Object, it must be a boolean
            try {
                schema.getAsBoolean();
            } catch (RuntimeException e){
                throw new SchemaValidationException("Schema is not valid");
            }
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

        verifyAndGetFieldOrDefault(object, "description", JsonElement::getAsString, "");
        verifyAndGetFieldOrDefault(object, "title", JsonElement::getAsString, "");

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
            case NUMBER:
            case INTEGER:
                validateNumberAndInteger(schemaNode);
                break;
            case STRING:
                validateString(schemaNode);
                break;
            case OBJECT:
                validateObject(schemaNode);
                break;
            case ARRAY:
                validateArray(schemaNode);
                break;
            default:
                break;
        }
    }

    static void validateString(JsonObject schemaNode) throws SchemaValidationException {
        int maxLength = verifyAndGetFieldOrDefault(schemaNode, "maxLength", JsonElement::getAsInt,  Integer.MAX_VALUE);
        if (maxLength < 0){
            throw new SchemaValidationException("\"maxLength\" must be greater than 0");
        }
        int minLength = verifyAndGetFieldOrDefault(schemaNode, "minLength", JsonElement::getAsInt,  Integer.MAX_VALUE);
        if (minLength < 0){
            throw new SchemaValidationException("\"minLength\" must be greater than 0");
        }

        verifyAndGetFieldOrDefault(schemaNode, "pattern", JsonElement::getAsString, "");
    }

    static void validateNumberAndInteger(JsonObject schemaNode) throws SchemaValidationException {
        verifyAndGetFieldOrDefault(schemaNode, "exclusiveMinimum", JsonElement::getAsDouble,  0.0);
        verifyAndGetFieldOrDefault(schemaNode, "exclusiveMaximum", JsonElement::getAsDouble,  0.0);
        verifyAndGetFieldOrDefault(schemaNode, "minimum", JsonElement::getAsDouble,  0.0);
        verifyAndGetFieldOrDefault(schemaNode, "maximum", JsonElement::getAsDouble,  0.0);

        double multipleOf = verifyAndGetFieldOrDefault(schemaNode,
                "multipleOf",
                JsonElement::getAsDouble,
                1.0);
        if (multipleOf <= 0){
            throw new SchemaValidationException("\"multipleOf\" must be strictly greater than 0");
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
        if (schemaNode.has("items")){
            validateSchemaNode(schemaNode.getAsJsonObject("items"));
        }
        if (schemaNode.has("contains")){
            validateSchemaNode(schemaNode.getAsJsonObject("contains"));
        }
        if ((!schemaNode.has("items") || schemaNode.getAsJsonObject("items").keySet().isEmpty()) &&
                !schemaNode.has("contains")){
            throw new SchemaValidationException("Empty \"items\" object requires \"contains\" keyword");
        }

        if (schemaNode.has("prefixItems")){
            JsonArray prefixItemsArray = schemaNode.getAsJsonArray("prefixItems");
            if (prefixItemsArray.isEmpty()){
                throw new SchemaValidationException("\"prefixItems\" may not be empty");
            }
            for (JsonElement el : prefixItemsArray){
                validateSchemaNode(el.getAsJsonObject());
            }
        }

        verifyAndGetFieldOrDefault(schemaNode, "minItems", JsonElement::getAsInt,  0);
        verifyAndGetFieldOrDefault(schemaNode, "uniqueItems", JsonElement::getAsBoolean, true);

        if (schemaNode.has("uniqueItems")){
            try {
                schemaNode.getAsJsonPrimitive("uniqueItems").getAsBoolean();
            } catch (Throwable throwable){
                throw new SchemaValidationException("\"uniqueItems\" must be a boolean", throwable);
            }
        }

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
                HashSet<MemberTypes> memberSet = new HashSet<>();
                for (JsonElement member : array) {
                    MemberTypes parsedType = MemberTypes.valueOf(member.getAsString().trim().toUpperCase());
                    if (memberSet.contains(parsedType)) {
                        throw new SchemaValidationException("Type array may not contain multiple instances of the same type");
                    }
                    memberSet.add(parsedType);
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

    private static <T> T verifyAndGetFieldOrDefault(JsonObject schemaNode,
                                                    String key,
                                                    Function<JsonElement, T> verificationFunction,
                                                    T defaultValue) throws SchemaValidationException {
        if (schemaNode.has(key)) {
            try{
                return verificationFunction.apply(schemaNode.getAsJsonPrimitive(key));
            } catch (RuntimeException e){
                throw new SchemaValidationException("\"" + key + "\" must be a " + defaultValue.getClass().getSimpleName(), e);
            }
        }
        return defaultValue;
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
