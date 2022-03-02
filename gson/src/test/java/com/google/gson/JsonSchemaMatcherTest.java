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

    private String nestedObjectSchemaString = "{\n" +
            "    \"type\": \"object\",\n" +
            "    \"properties\": {\n" +
            "        \"obj1\": {\n" +
            "            \"type\": \"object\",\n" +
            "            \"properties\": {\n" +
            "                \"nested\": {\n" +
            "                    \"type\": \"object\"\n" +
            "                }\n" +
            "            }\n" +
            "        },\n" +
            "        \"obj2\": {\n" +
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

    // "obj1" has the wrong type of property "nested" it should be an object but is an integer.
    private String nestedObjectInstanceFailingString = "{\n" +
            "    \"obj1\": {\n" +
            "        \"nested\": 2\n" +
            "    },\n" +
            "    \"obj2\": {\n" +
            "        \"nested\": {\n" +
            "            \"veryNested1\": {},\n" +
            "            \"veryNested2\": {}\n" +
            "        }\n" +
            "    }\n" +
            "}";

    // "obj2" does not contain the required property "veryNested2".
    private String nestedObjectInstanceFailingString2 = "{\n" +
            "    \"obj1\": {},\n" +
            "    \"obj2\": {\n" +
            "        \"nested\": {\n" +
            "            \"veryNested1\": {}\n" +
            "        }\n" +
            "    }\n" +
            "}";

    private String arraySchemaString = "{\n" +
            "    \"type\": \"array\",\n" +
            "    \"items\": {\n" +
            "        \"type\": \"object\"       \n" +
            "    },\n" +
            "    \"minItems\": 2,\n" +
            "    \"uniqueItems\": true\n" +
            "}";

    private String arrayInstanceSuccessfulString = "[\n" +
            "    {},\n" +
            "    { \"obj\": {}}\n" +
            "]";

    private String arrayInstanceUniqueFailing = "[\n" +
            "    {},\n" +
            "    {}\n" +
            "]";

    private String arrayInstanceMinItemsFailing = "[\n" +
            "    {}\n" +
            "]";

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
        JsonSchemaMatcher matcher = new JsonSchemaMatcher(nestedObjectSchemaString, nestedObjectInstanceFailingString2);
        assertFalse(matcher.matches());
    }

    public void testArraySuccessful() {
        JsonSchemaMatcher matcher = new JsonSchemaMatcher(arraySchemaString, arrayInstanceSuccessfulString);
        assertTrue(matcher.matches());
    }

    public void testArrayUniqueFailing() {
        JsonSchemaMatcher matcher = new JsonSchemaMatcher(arraySchemaString, arrayInstanceUniqueFailing);
        assertFalse(matcher.matches());
    }

    public void testArrayMinItemsFailing() {
        JsonSchemaMatcher matcher = new JsonSchemaMatcher(arraySchemaString, arrayInstanceMinItemsFailing);
        assertFalse(matcher.matches());
    }
}
