/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson;

import junit.framework.TestCase;

public class JsonSchemaMatcherTest extends TestCase {

    private String schemaString = "{\n" +
            "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\n" +
            "  \"$id\": \"https://example.com/product.schema.json\",\n" +
            "  \"title\": \"Product\",\n" +
            "  \"description\": \"A product from Acme's catalog\",\n" +
            "  \"type\": \"object\",\n" +
            "}";

    public void testValidatorCanParseSchema() {
        JsonSchemaMatcher validator = new JsonSchemaMatcher(schemaString);
        assertEquals("https://json-schema.org/draft/2020-12/schema", validator.getSchemaUri());
    }

    public void testValidatorCanParseId() {
        JsonSchemaMatcher validator = new JsonSchemaMatcher(schemaString);
        assertEquals("https://example.com/product.schema.json", validator.getIdUri());
    }

    public void testValidatorCanParseTitle() {
        JsonSchemaMatcher validator = new JsonSchemaMatcher(schemaString);
        assertEquals("Product", validator.getTitle());
    }

    public void testValidatorCanParseDescription() {
        JsonSchemaMatcher validator = new JsonSchemaMatcher(schemaString);
        assertEquals("A product from Acme's catalog", validator.getDescription());
    }

    public void testValidatorCanParseRootType() {
        JsonSchemaMatcher validator = new JsonSchemaMatcher(schemaString);
        assertEquals("object", validator.getRootType());
    }
}
