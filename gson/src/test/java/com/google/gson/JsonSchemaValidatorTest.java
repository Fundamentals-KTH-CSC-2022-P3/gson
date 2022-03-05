package com.google.gson;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JsonSchemaValidatorTest {

    @Test
    public void testValidate() throws JsonSchemaValidator.SchemaValidationException {
        JsonElement root = JsonParser.parseString(
                "{\n" +
                        "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\n" +
                        "  \"$id\": \"https://example.com/product.schema.json\",\n" +
                        "  \"title\": \"Product\",\n" +
                        "  \"description\": \"A product from Acme's catalog\",\n" +
                        "  \"type\": \"object\",\n" +
                        "  \"properties\": {\n" +
                        "    \"productId\": {\n" +
                        "      \"description\": \"The unique identifier for a product\",\n" +
                        "      \"type\": \"integer\"\n" +
                        "    },\n" +
                        "    \"productName\": {\n" +
                        "      \"description\": \"Name of the product\",\n" +
                        "      \"type\": \"string\"\n" +
                        "    },\n" +
                        "    \"price\": {\n" +
                        "      \"description\": \"The price of the product\",\n" +
                        "      \"type\": \"number\",\n" +
                        "      \"exclusiveMinimum\": 0\n" +
                        "    },\n" +
                        "    \"tags\": {\n" +
                        "      \"description\": \"Tags for the product\",\n" +
                        "      \"type\": \"array\",\n" +
                        "      \"items\": {\n" +
                        "        \"type\": \"string\"\n" +
                        "      },\n" +
                        "      \"minItems\": 1,\n" +
                        "      \"uniqueItems\": true\n" +
                        "    },\n" +
                        "    \"dimensions\": {\n" +
                        "      \"type\": \"object\",\n" +
                        "      \"properties\": {\n" +
                        "        \"length\": {\n" +
                        "          \"type\": \"number\"\n" +
                        "        },\n" +
                        "        \"width\": {\n" +
                        "          \"type\": \"number\"\n" +
                        "        },\n" +
                        "        \"height\": {\n" +
                        "          \"type\": \"number\"\n" +
                        "        }\n" +
                        "      },\n" +
                        "      \"required\": [ \"length\", \"width\", \"height\" ]\n" +
                        "    },\n" +
                        "    \"warehouseLocation\": {\n" +
                        "      \"description\": \"Coordinates of the warehouse where the product is located.\",\n" +
                        "      \"$ref\": \"https://example.com/geographical-location.schema.json\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"required\": [ \"productId\", \"productName\", \"price\" ]\n" +
                        "}");
//        JsonSchemaValidator.validate(root);
    }

    @Test
    public void testValidateTypeField() throws JsonSchemaValidator.SchemaValidationException {
        JsonElement root = JsonParser.parseString("{ \"type\": [\"number\", \"bl\"] }");
        Exception exception = null;
        try {
            JsonSchemaValidator.validateTypeField(root.getAsJsonObject());
        } catch (Exception ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);
        Assert.assertEquals(JsonSchemaValidator.SchemaValidationException.class, exception.getClass());
    }

    @Test
    public void testValidateOptionalURIField() throws JsonSchemaValidator.SchemaValidationException {
        JsonElement root = JsonParser.parseString(
                "{\n" +
                        "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\n" +
                        "  \"$id\": \"https://example.com/product.schema.json\",\n" +
                        "  \"title\": \"Product\",\n" +
                        "  \"description\": \"A product from Acme's catalog\",\n" +
                        "  \"type\": \"object\",\n" +
                        "  \"properties\": {\n" +
                        "    \"productId\": {\n" +
                        "      \"description\": \"The unique identifier for a product\",\n" +
                        "      \"type\": \"integer\"\n" +
                        "    }" +
                        "  }," +
                        "  \"required\": [\"productId\"]" +
                        "}");
        JsonSchemaValidator.validateOptionalURIField("$schema", root.getAsJsonObject());
    }


    @Test
    public void testValidateObject() throws JsonSchemaValidator.SchemaValidationException {
        JsonElement root = JsonParser.parseString(
                "{\n" +
                        "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\n" +
                        "  \"$id\": \"https://example.com/product.schema.json\",\n" +
                        "  \"title\": \"Product\",\n" +
                        "  \"description\": \"A product from Acme's catalog\",\n" +
                        "  \"type\": \"object\",\n" +
                        "  \"properties\": {\n" +
                        "    \"productId\": {\n" +
                        "      \"description\": \"The unique identifier for a product\",\n" +
                        "      \"type\": \"integer\"\n" +
                        "    }" +
                        "  }," +
                        "  \"required\": [\"productId\"]" +
                        "}");
        JsonSchemaValidator.validateObject(root.getAsJsonObject());
    }

    @Test
    public void validateTypeField() {
    }

    @Test
    public void testValidateArray() throws JsonSchemaValidator.SchemaValidationException {
        JsonElement root = JsonParser.parseString(
                "{\n" +
                        "      \"description\": \"Tags for the product\",\n" +
                        "      \"type\": \"array\",\n" +
                        "      \"items\": {\n" +
                        "        \"type\": \"string\"\n" +
                        "      },\n" +
                        "      \"minItems\": 1,\n" +
                        "      \"uniqueItems\": true\n" +
                        "}");
        JsonSchemaValidator.validateArray(root.getAsJsonObject());
    }
}
