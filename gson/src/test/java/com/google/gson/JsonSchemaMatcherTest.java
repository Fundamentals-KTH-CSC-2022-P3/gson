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

  private String numberMaxMinSchemaString = "{\n" +
          "    \"type\": \"number\",\n" +
          "    \"minimum\": 1,\n" +
          "    \"maximum\": 3\n" +
          "}";

  private String numberMinMaxInstanceSuccessful = "1";

  private String numberMinMaxInstanceSuccessful2 = "2";

  private String numberMinMaxInstanceSuccessful3 = "3";

  private String numberMinMaxInstanceFailingTooSmall = "0";

  private String numberMinMaxInstanceFailingTooLarge = "4";

  private String numberExclusiveMinMaxSchemaString = "{\n" +
          "    \"type\": \"number\",\n" +
          "    \"exclusiveMinimum\": 1,\n" +
          "    \"exclusiveMaximum\": 3\n" +
          "}";

  private String numberExclusiveMinMaxSuccessful = "2";

  private String numberExclusiveMinMaxFailingTooSmall = "1";

  private String numberExclusiveMinMaxFailingTooLarge = "3";

  public void testNestedObjectSuccessful() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(nestedObjectSchemaString);
    assertTrue(matcher.matches(nestedObjectInstanceSuccessfulString));
  }

  public void testNestedObjectSuccessful2() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(nestedObjectSchemaString);
    assertTrue(matcher.matches(nestedObjectInstanceSuccessfulString2));
  }

  public void testNestedObjectFailing() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(nestedObjectSchemaString);
    assertFalse(matcher.matches(nestedObjectInstanceFailingString));
  }

  public void testNestedObjectFailing2() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(nestedObjectSchemaString);
    assertFalse(matcher.matches(nestedObjectInstanceFailingString2));
  }

  public void testArraySuccessful() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(arraySchemaString);
    assertTrue(matcher.matches(arrayInstanceSuccessfulString));
  }

  public void testArrayUniqueFailing() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(arraySchemaString);
    assertFalse(matcher.matches(arrayInstanceUniqueFailing));
  }

  public void testArrayMinItemsFailing() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(arraySchemaString);
    assertFalse(matcher.matches(arrayInstanceMinItemsFailing));
  }

  public void testNumberMinMaxSuccessful() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(numberMaxMinSchemaString);
    assertTrue(matcher.matches(numberMinMaxInstanceSuccessful));
    assertTrue(matcher.matches(numberMinMaxInstanceSuccessful2));
    assertTrue(matcher.matches(numberMinMaxInstanceSuccessful3));
  }

  public void testNumberMinMaxFailingTooSmall() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(numberMaxMinSchemaString);
    assertFalse(matcher.matches(numberMinMaxInstanceFailingTooSmall));
  }

  public void testNumberMinMaxFailingTooLarge() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(numberMaxMinSchemaString);
    assertFalse(matcher.matches(numberMinMaxInstanceFailingTooLarge));
  }

  public void testNumberExclusiveMinMaxSuccessful() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(numberExclusiveMinMaxSchemaString);
    assertTrue(matcher.matches(numberExclusiveMinMaxSuccessful));
  }

  public void testNumberExclusiveMinMaxFailingTooSmall() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(numberExclusiveMinMaxSchemaString);
    assertFalse(matcher.matches(numberExclusiveMinMaxFailingTooSmall));
  }

  public void testNumberExclusiveMinMaxFailingTooLarge() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(numberExclusiveMinMaxSchemaString);
    assertFalse(matcher.matches(numberExclusiveMinMaxFailingTooLarge));
  }
}
