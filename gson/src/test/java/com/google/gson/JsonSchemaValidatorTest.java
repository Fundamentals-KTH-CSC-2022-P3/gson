package com.google.gson;

import junit.framework.TestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JsonSchemaValidatorTest extends TestCase {

    @Test
    public void testValidate() {
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
        assertNotNull(exception);
        assertEquals(JsonSchemaValidator.SchemaValidationException.class, exception.getClass());
    }

    @Test
    public void testValidateOptionalURIField() {
    }
}