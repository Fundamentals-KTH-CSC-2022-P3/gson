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
            "  \"type\": \"object\"\n" +
            "}";

    private String nestedObjectSchemaString = "{\n" +
            "    \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\n" +
            "    \"$id\": \"https://example.com/product.schema.json\",\n" +
            "    \"title\": \"Nested objects\",\n" +
            "    \"description\": \"A nested objects JSON file\",\n" +
            "    \"type\": \"object\",\n" +
            "    \"properties\": {\n" +
            "        \"obj1\": {\n" +
            "            \"description\": \"First object\",\n" +
            "            \"type\": \"object\",\n" +
            "            \"properties\": {\n" +
            "                \"nested\": {\n" +
            "                    \"type\": \"object\"\n" +
            "                }\n" +
            "            }\n" +
            "        },\n" +
            "        \"obj2\": {\n" +
            "            \"description\": \"Second object\",\n" +
            "            \"type\": \"object\",\n" +
            "            \"properties\": {\n" +
            "                \"nested\": {\n" +
            "                    \"type\": \"object\",\n" +
            "                    \"properties\": {\n" +
            "                        \"veryNested1\": {\n" +
            "                            \"type\": \"object\"\n" +
            "                        },\n" +
            "                        \"veryNested2\": {\n" +
            "                            \"type\": \"object\"\n" +
            "                        }\n" +
            "                    },\n" +
            "                    \"required\": [\n" +
            "                        \"veryNested2\"\n" +
            "                    ]\n" +
            "                }\n" +
            "            },\n" +
            "            \"required\": [\n" +
            "                \"nested\"\n" +
            "            ]\n" +
            "        }\n" +
            "    },\n" +
            "    \"required\": [\n" +
            "        \"obj2\"\n" +
            "    ]\n" +
            "}";

    private String nestedObjectInstanceSuccessfulString = "{\n" +
            "    \"obj1\": {},\n" +
            "    \"obj2\": {\n" +
            "        \"nested\": {\n" +
            "            \"veryNested1\": {},\n" +
            "            \"veryNested2\": {}\n" +
            "        }\n" +
            "    }\n" +
            "}";

    private String nestedObjectInstanceSuccessfulString2 = "{\n" +
            "    \"obj2\": {\n" +
            "        \"nested\": {\n" +
            "            \"veryNested2\": {}\n" +
            "        }\n" +
            "    }\n" +
            "}";

    // "obj1" contains property "hello" which is not defined in the schema.
    private String nestedObjectInstanceFailingString = "{\n" +
            "    \"obj1\": {\n" +
            "        \"hello\": {}\n" +
            "    },\n" +
            "    \"obj2\": {\n" +
            "        \"nested\": {\n" +
            "            \"veryNested1\": {},\n" +
            "            \"veryNested2\": {}\n" +
            "        }\n" +
            "    }\n" +
            "}";

    // "obj2" does not contain the required property "veryNested2".
    private String getNestedObjectInstanceFailingString2 = "{\n" +
            "    \"obj1\": {},\n" +
            "    \"obj2\": {\n" +
            "        \"nested\": {\n" +
            "            \"veryNested1\": {}\n" +
            "        }\n" +
            "    }\n" +
            "}";

    public void testValidatorCanParseSchema() {
        JsonSchemaMatcher matcher = new JsonSchemaMatcher(schemaString, "");
        assertEquals("https://json-schema.org/draft/2020-12/schema", matcher.getSchemaUri());
    }

    public void testValidatorCanParseId() {
        JsonSchemaMatcher matcher = new JsonSchemaMatcher(schemaString, "");
        assertEquals("https://example.com/product.schema.json", matcher.getIdUri());
    }

    public void testValidatorCanParseTitle() {
        JsonSchemaMatcher matcher = new JsonSchemaMatcher(schemaString, "");
        assertEquals("Product", matcher.getTitle());
    }

    public void testValidatorCanParseDescription() {
        JsonSchemaMatcher matcher = new JsonSchemaMatcher(schemaString,"");
        assertEquals("A product from Acme's catalog", matcher.getRootDescription());
    }

    public void testValidatorCanParseRootType() {
        JsonSchemaMatcher matcher = new JsonSchemaMatcher(schemaString,"");
        assertEquals("object", matcher.getRootType());
    }

    public void testNestedObjectSuccessful() {
        JsonSchemaMatcher matcher = new JsonSchemaMatcher(nestedObjectSchemaString, nestedObjectInstanceSuccessfulString);
        assertTrue(matcher.matches());
    }

    public void testNestedObjectSuccessful2() {
        JsonSchemaMatcher matcher = new JsonSchemaMatcher(nestedObjectSchemaString, nestedObjectInstanceSuccessfulString2);
        assertTrue(matcher.matches());
    }

    public void testNestedObjectFailing() {
        JsonSchemaMatcher matcher = new JsonSchemaMatcher(nestedObjectSchemaString, nestedObjectInstanceFailingString);
        assertFalse(matcher.matches());
    }

    public void testNestedObjectFailing2() {
        JsonSchemaMatcher matcher = new JsonSchemaMatcher(nestedObjectSchemaString, getNestedObjectInstanceFailingString2);
        assertFalse(matcher.matches());
    }
}
