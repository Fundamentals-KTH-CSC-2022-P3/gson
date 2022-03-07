package com.google.gson.schema;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.schema.JsonSchemaValidator;
import org.junit.Assert;
import org.junit.Test;

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

    @Test
    public void testValidateString() throws JsonSchemaValidator.SchemaValidationException {
        JsonElement root = JsonParser.parseString(
                "{\n" +
                        "      \"description\": \"Tags for the product\",\n" +
                        "      \"type\": \"string\",\n" +
                        "      \"maxLength\": \"10\",\n" +
                        "      \"minLength\": \"5\",\n" +
                        "      \"pattern\": \"nice pattern\"\n" +
                        "}");
        JsonSchemaValidator.validateString(root.getAsJsonObject());
    }

    @Test
    public void testValidateNumberAndInteger() throws JsonSchemaValidator.SchemaValidationException {
        JsonElement root1 = JsonParser.parseString(
                "{\n" +
                        "      \"description\": \"Tags for the product\",\n" +
                        "      \"type\": \"number\",\n" +
                        "      \"multipleOf\": 10.5,\n" +
                        "      \"minimum\": 6.1,\n" +
                        "      \"maximum\": 15.5,\n" +
                        "      \"exclusiveMinimum\": 5,\n" +
                        "      \"exclusiveMaximum\": 20\n" +
                        "}");
        JsonElement root2 = JsonParser.parseString(
                "{\n" +
                        "      \"description\": \"Tags for the product\",\n" +
                        "      \"type\": \"integer\",\n" +
                        "      \"multipleOf\": 10,\n" +
                        "      \"minimum\": 6,\n" +
                        "      \"maximum\": 15,\n" +
                        "      \"exclusiveMinimum\": 5,\n" +
                        "      \"exclusiveMaximum\": 20\n" +
                        "}");
        JsonSchemaValidator.validateNumberAndInteger(root1.getAsJsonObject());
        JsonSchemaValidator.validateNumberAndInteger(root2.getAsJsonObject());
    }
}
