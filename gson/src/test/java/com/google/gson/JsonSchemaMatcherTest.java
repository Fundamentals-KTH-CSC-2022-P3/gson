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

import com.google.gson.stream.JsonReader;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(JUnit4.class)
public class JsonSchemaMatcherTest extends TestCase {

  private String objectSchema;

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

  /**
   * The following setup function prepares this schema:
   *   {"type":"object","properties":{"obj2":{"type":"object","properties":{"nested":{"type":"object","properties":{"veryNested1":{"type":"object"},"veryNested2":{"type":"object"}},"required":["veryNested2"]}},"required":["nested"]},"obj1":{"type":"object","properties":{"nested":{"type":"object"}}}},"required":["obj2"]}
   */
  @Before
  public void createObjectSchema() {
    JsonSchemaObject schema = new JsonSchemaObject();
    schema.addProperty(
            "obj1",
            new JsonSchemaObject().addProperty("nested", new JsonSchemaObject())
    );
    schema.addRequiredProperty(
            "obj2",
            new JsonSchemaObject().addRequiredProperty(
                    "nested",
                    new JsonSchemaObject().addProperty(
                            "veryNested1",
                            new JsonSchemaObject()
                    ).addRequiredProperty(
                            "veryNested2",
                            new JsonSchemaObject()
                    )
            )
    );
    objectSchema = schema.toJsonElement().toString();
  }

  @Test
  public void testMatcherCanSuccessfullyValidateAPassingSchema() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(objectSchema);

    String jsonToValidate = "{\n" +
            "    \"obj1\": {},\n" +
            "    \"obj2\": {\n" +
            "        \"nested\": {\n" +
            "            \"veryNested1\": {},\n" +
            "            \"veryNested2\": {}\n" +
            "        }\n" +
            "    }\n" +
            "}";

    assertTrue(matcher.matches(jsonToValidate));
  }

  @Test
  public void testMatcherCanValidateSchemaWithoutTypes() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(objectSchema);

    String jsonToValidate = "{\n" +
            "    \"obj2\": {\n" +
            "        \"nested\": {\n" +
            "            \"veryNested2\": {}\n" +
            "        }\n" +
            "    }\n" +
            "}";

    assertTrue(matcher.matches(jsonToValidate));
  }

  @Test
  public void testMatcherReturnsFalseIfProvidedStringDoesNotMatchSchema() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(objectSchema);

    // "obj1" has the wrong type of property "nested" it should be an object but is an integer.
    String json1 = "{\n" +
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
    String json2 = "{\n" +
            "    \"obj1\": {},\n" +
            "    \"obj2\": {\n" +
            "        \"nested\": {\n" +
            "            \"veryNested1\": {}\n" +
            "        }\n" +
            "    }\n" +
            "}";

    assertFalse(matcher.matches(json1));
    assertFalse(matcher.matches(json2));
  }

  @Test
  public void testArraySuccessful() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(arraySchemaString);
    assertTrue(matcher.matches(arrayInstanceSuccessfulString));
  }

  @Test
  public void testArrayUniqueFailing() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(arraySchemaString);
    assertFalse(matcher.matches(arrayInstanceUniqueFailing));
  }

  @Test
  public void testArrayMinItemsFailing() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(arraySchemaString);
    assertFalse(matcher.matches(arrayInstanceMinItemsFailing));
  }

  @Test
  public void testNumberMinMaxSuccessful() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(numberMaxMinSchemaString);
    assertTrue(matcher.matches(numberMinMaxInstanceSuccessful));
    assertTrue(matcher.matches(numberMinMaxInstanceSuccessful2));
    assertTrue(matcher.matches(numberMinMaxInstanceSuccessful3));
  }

  @Test
  public void testNumberMinMaxFailingTooSmall() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(numberMaxMinSchemaString);
    assertFalse(matcher.matches(numberMinMaxInstanceFailingTooSmall));
  }

  @Test
  public void testNumberMinMaxFailingTooLarge() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(numberMaxMinSchemaString);
    assertFalse(matcher.matches(numberMinMaxInstanceFailingTooLarge));
  }

  @Test
  public void testNumberExclusiveMinMaxSuccessful() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(numberExclusiveMinMaxSchemaString);
    assertTrue(matcher.matches(numberExclusiveMinMaxSuccessful));
  }

  @Test
  public void testNumberExclusiveMinMaxFailingTooSmall() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(numberExclusiveMinMaxSchemaString);
    assertFalse(matcher.matches(numberExclusiveMinMaxFailingTooSmall));
  }

  @Test
  public void testNumberExclusiveMinMaxFailingTooLarge() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(numberExclusiveMinMaxSchemaString);
    assertFalse(matcher.matches(numberExclusiveMinMaxFailingTooLarge));
  }

  @Test
  public void testReaderAPI() {
    Reader schemaReader = new StringReader("true");
    Reader instanceReader = new StringReader("{}");
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(schemaReader);
    assertTrue(matcher.matches(instanceReader));
  }

  @Test
  public void testJsonReaderAPI() {
    JsonReader schemaJsonReader = new JsonReader(new StringReader("true"));
    JsonReader instanceJsonReader = new JsonReader(new StringReader("{}"));
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(schemaJsonReader);
    assertTrue(matcher.matches(instanceJsonReader));
  }

  @Test
  public void testStringAPI() {
    String schemaString = "true";
    String instanceString = "{}";
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(schemaString);
    assertTrue(matcher.matches(instanceString));
  }

  @Test
  public void testJsonElementAPI() {
    JsonElement schemaJsonElement = JsonParser.parseString("true");
    JsonElement instanceJsonElement = JsonParser.parseString("{}");
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(schemaJsonElement);
    assertTrue(matcher.matches(instanceJsonElement));
  }
}

abstract class JsonSchemaElement {

  public abstract JsonElement toJsonElement();

  public String toString() {
    return toJsonElement().getAsString();
  }
}

class JsonSchemaObject extends JsonSchemaElement {
  Map<String, JsonSchemaElement> properties = new HashMap<>();
  List<String> required = new ArrayList<>();

  public JsonSchemaObject addProperty(String name, JsonSchemaElement element) {
    properties.put(name, element);
    return this;
  }

  public JsonSchemaObject addRequiredProperty(String name, JsonSchemaElement element) {
    properties.put(name, element);
    required.add(name);
    return this;
  }

  public JsonElement toJsonElement() {
    JsonObject el = new JsonObject();
    JsonObject props = new JsonObject();
    for (String key : properties.keySet()) {
      props.add(key, properties.get(key).toJsonElement());
    }
    el.addProperty("type", "object");
    if (props.size() != 0) {
      el.add("properties", props);
    }
    if (!required.isEmpty()) {
      JsonArray arr = new JsonArray();
      for (String key : required) {
        arr.add(key);
      }
      el.add("required", arr);
    }
    return el;
  }
}
