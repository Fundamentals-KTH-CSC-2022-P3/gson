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

  private String arraySchema;

  private String numberSchema;

  private String exclusiveNumberSchema;

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

  /**
   * The following setup function prepares this schema:
   *    {"type":"array","items":{"type":"object"},"minItems":2,"uniqueItems":true}
   */
  @Before
  public void createArraySchema() {
    JsonSchemaArray schema = new JsonSchemaArray();
    schema.setMinItems(2);
    schema.setUniqueItems(true);
    schema.setItemType("object");
    arraySchema = schema.toJsonElement().toString();
  }

  /**
   * The following setup function prepares this schema:
   *    {"type":"number","minimum":1,"maximum":3}
   */
  @Before
  public void createNumberSchema() {
    JsonSchemaNumber schema = new JsonSchemaNumber();
    schema.setMinimum(1);
    schema.setMaximum(3);
    numberSchema = schema.toJsonElement().toString();
  }

  /**
   * The following setup function prepares this schema:
   *    {"type":"number","exclusiveMinimum":1,"exclusiveMaximum":3}
   */
  @Before
  public void createExclusiveNumberSchema() {
    JsonSchemaNumber schema = new JsonSchemaNumber();
    schema.setExclusiveMinimum(1);
    schema.setExclusiveMaximum(3);
    exclusiveNumberSchema = schema.toJsonElement().toString();
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
  public void testMatcherCanSuccessfullyMatchAValidArraySchema() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(arraySchema);

    String json = "[\n" +
            "    {},\n" +
            "    { \"obj\": {}}\n" +
            "]";

    assertTrue(matcher.matches(json));
  }

  @Test
  public void testJsonDoesNotPassIfNotItemsAreNotUniqueAndUniqueIsARequirement() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(arraySchema);

    String json = "[\n" +
            "    {},\n" +
            "    {}\n" +
            "]";

    assertFalse(matcher.matches(json));
  }

  @Test
  public void testArrayMinItemsFailing() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(arraySchema);

    // Not enough items
    String json = "[\n" +
            "    {}\n" +
            "]";

    assertFalse(matcher.matches(json));
  }

  @Test
  public void testNumberMinMax() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(numberSchema);
    assertTrue(matcher.matches("1"));
    assertTrue(matcher.matches("2"));
    assertTrue(matcher.matches("3"));
  }

  @Test
  public void testNumberMinMaxFailingTooSmall() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(numberSchema);
    assertFalse(matcher.matches("0"));
  }

  @Test
  public void testNumberMinMaxFailingTooLarge() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(numberSchema);
    assertFalse(matcher.matches("4"));
  }

  @Test
  public void testNumberExclusiveMinMaxSuccessful() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(exclusiveNumberSchema);
    assertTrue(matcher.matches("2"));
  }

  @Test
  public void testNumberExclusiveMinMaxFailingTooSmall() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(exclusiveNumberSchema);
    assertFalse(matcher.matches("1"));
  }

  @Test
  public void testNumberExclusiveMinMaxFailingTooLarge() {
    JsonSchemaMatcher matcher = new JsonSchemaMatcher(exclusiveNumberSchema);
    assertFalse(matcher.matches("3"));
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

class JsonSchemaArray extends JsonSchemaElement {
  private String itemType;
  private Integer minItems = null;
  private Boolean uniqueItems = null;

  public void setItemType(String itemType) {
    this.itemType = itemType;
  }

  public void setMinItems(Integer minItems) {
    this.minItems = minItems;
  }

  public void setUniqueItems(Boolean uniqueItems) {
    this.uniqueItems = uniqueItems;
  }

  public JsonElement toJsonElement() {
    JsonObject el = new JsonObject();
    el.addProperty("type", "array");

    JsonObject items = new JsonObject();
    items.addProperty("type", itemType);

    el.add("items", items);

    if (minItems != null) {
      el.addProperty("minItems", minItems);
    }
    if (uniqueItems != null) {
      el.addProperty("uniqueItems", uniqueItems);
    }
    return el;
  }
}

class JsonSchemaNumber extends JsonSchemaElement {
  private Integer minimum = null;
  private Integer maximum = null;
  private Integer exclusiveMinimum = null;
  private Integer exclusiveMaximum = null;

  public void setMinimum(Integer minimum) {
    this.minimum = minimum;
  }

  public void setMaximum(Integer maximum) {
    this.maximum = maximum;
  }

  public void setExclusiveMinimum(Integer exclusiveMinimum) {
    this.exclusiveMinimum = exclusiveMinimum;
  }

  public void setExclusiveMaximum(Integer exclusiveMaximum) {
    this.exclusiveMaximum = exclusiveMaximum;
  }

  public JsonElement toJsonElement() {
    JsonObject el = new JsonObject();
    el.addProperty("type", "number");

    if (minimum != null) {
      el.addProperty("minimum", minimum);
    }
    if (maximum != null) {
      el.addProperty("maximum", maximum);
    }
    if (exclusiveMinimum != null) {
      el.addProperty("exclusiveMinimum", exclusiveMinimum);
    }
    if (exclusiveMaximum != null) {
      el.addProperty("exclusiveMaximum", exclusiveMaximum);
    }
    return el;
  }
}
