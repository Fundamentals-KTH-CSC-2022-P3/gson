package com.google.gson;

import java.util.HashSet;
import java.util.Set;

/**
 * The {@code JsonSchemaMatcher} class is responsible for matching a schema file against a JSON instance.
 */
public class JsonSchemaMatcher {

  // The root of the schema file can be either a JSON object or a Boolean.
  private final JsonElement schemaRoot;

  /**
   * Creates a new {@code JsonSchemaMatcher} instance from a valid schema string.
   *
   * @param jsonSchema the schema as a {@code String}.
   */
  public JsonSchemaMatcher(String jsonSchema) {
    schemaRoot = new JsonParser().parse(jsonSchema);
  }

  /**
   * Checks that the JSON instance (or sub-instance) and the schema (or sub-schema) has matching types.
   *
   * @param instance the JSON instance (or sub-instance).
   * @param schema   the schema (or sub-schema).
   * @return
   */
  private boolean matchingTypes(JsonElement instance, JsonObject schema) {
    // If the sub-schema does not have a "type" property then the instance can have any type.
    if (!schema.has("type"))
      return true;

    String type = schema.get("type").getAsString();

    switch (type) {
      case "object":
        return instance.isJsonObject();
      case "array":
        return instance.isJsonArray();
      case "null":
        return instance.isJsonNull();
      case "boolean":
        if (!instance.isJsonPrimitive())
          return false;
        return instance.getAsJsonPrimitive().isBoolean();
      case "number":
        if (!instance.isJsonPrimitive())
          return false;
        return instance.getAsJsonPrimitive().isNumber();
      case "integer":
        if (!instance.isJsonPrimitive())
          return false;
        if (!instance.getAsJsonPrimitive().isNumber())
          return false;
        return (instance.getAsJsonPrimitive().getAsDouble() % 1) == 0;
      case "string":
        if (!instance.isJsonPrimitive())
          return false;
        return instance.getAsJsonPrimitive().isString();
    }

    return false;
  }


  /**
   * Matches an instance (or sub-instance) object against a schema (or sub-schema).
   *
   * @param instanceObject the instance (or sub-instance) object.
   * @param schema         the schema (or sub-schema).
   * @return true if an instance (or sub-instance) object matches against the sub-schema, otherwise false.
   */
  private boolean objectMatches(JsonObject instanceObject, JsonObject schema) {
    // Check that the instance has all the required properties.
    if (schema.has("required")) {
      JsonArray requiredArray = schema.getAsJsonArray("required");

      for (JsonElement required : requiredArray) {
        if (!instanceObject.has(required.getAsString()))
          return false;
      }
    }

    // If the sub-schema does not have a "properties" property then the instance can have any properties it wants.
    if (!schema.has("properties"))
      return true;

    JsonObject properties = schema.getAsJsonObject("properties");

    // Check that each property in the sub-schema matches against the instance properties.
    for (String key : properties.keySet()) {

      // The instance is not required to have all the properties defined in the sub-schema.
      if (!instanceObject.has(key))
        continue;

      if (!matches(instanceObject.get(key), properties.getAsJsonObject(key)))
        return false;
    }

    return true;
  }

  /**
   * Matches an instance (or sub-instance) array against a schema (or sub-schema).
   *
   * @param instanceArray the instance (or sub-instance) array.
   * @param schema        the schema (or sub-schema).
   * @return true if an instance (or sub-instance) array matches against the sub-schema, otherwise false.
   */
  private boolean arrayMatches(JsonArray instanceArray, JsonObject schema) {
    // Check if the sub-schema has the "minItems" property and then the array must have
    // at least as many elements specified by the "minItems" property.
    if (schema.has("minItems") && instanceArray.size() < schema.get("minItems").getAsInt())
      return false;

    // Check if the sub-schema has the "uniqueItems" property, if so, all elements in the array must be unique.
    if (schema.has("uniqueItems")) {
      Set<JsonElement> found = new HashSet<>();

      for (JsonElement element : instanceArray) {
        // Check if the element has already been found, if so the element is not unique.
        if (found.contains(element))
          return false;
        found.add(element);
      }
    }

    // If the sub-schema does not have the "items" property then the array can contain any elements.
    if (!schema.has("items"))
      return true;

    JsonElement items = schema.get("items");

    // Special case if the value of the items key is set to false.
    if (items.isJsonPrimitive() && !items.getAsBoolean())
      return instanceArray.isEmpty();

    // Check that each element in the instance matches against the "items" object.
    for (JsonElement element : instanceArray) {
      if (!matches(element, items.getAsJsonObject()))
        return false;
    }

    return true;
  }

  /**
   * Matches an instance (or sub-instance) primitive against a schema (or sub-schema).
   *
   * @param instancePrimitive the instance (or sub-instance) primitive.
   * @param schema            the schema (or sub-schema).
   * @return true if an instance (or sub-instance) primitive matches against the sub-schema, otherwise false.
   */
  private boolean primitiveMatches(JsonPrimitive instancePrimitive, JsonObject schema) {
    if (instancePrimitive.isBoolean()) {
      return booleanMatches(instancePrimitive.getAsBoolean(), schema);
    } else if (instancePrimitive.isNumber()) {
      return numberMatches(instancePrimitive.getAsNumber(), schema);
    } else if (instancePrimitive.isString()) {
      return stringMatches(instancePrimitive.getAsString(), schema);
    } else if (instancePrimitive.isJsonNull()) {
      return nullMatches(instancePrimitive.getAsJsonNull(), schema);
    }
    return false;
  }

  /**
   * Matches an instance (or sub-instance) boolean against a schema (or sub-schema).
   *
   * @param instanceBool the instance (or sub-instance) boolean.
   * @param schema       the schema (or sub-schema).
   * @return true if an instance (or sub-instance) boolean matches against the sub-schema, otherwise false.
   */
  private boolean booleanMatches(boolean instanceBool, JsonObject schema) {
    // For now, we have no rules for booleans.
    return true;
  }

  /**
   * Matches an instance (or sub-instance) number against a schema (or sub-schema).
   *
   * @param instanceNumber the instance (or sub-instance) number.
   * @param schema         the schema (or sub-schema).
   * @return true if an instance (or sub-instance) number matches against the sub-schema, otherwise false.
   */
  private boolean numberMatches(Number instanceNumber, JsonObject schema) {
    if (schema.has("minimum")) {
      double minimum = schema.get("minimum").getAsDouble();
      if (instanceNumber.doubleValue() < minimum)
        return false;
    }

    if (schema.has("maximum")) {
      double maximum = schema.get("maximum").getAsDouble();
      if (instanceNumber.doubleValue() > maximum)
        return false;
    }

    if (schema.has("exclusiveMinimum")) {
      double exclusiveMinimum = schema.get("exclusiveMinimum").getAsDouble();
      if (instanceNumber.doubleValue() <= exclusiveMinimum)
        return false;
    }

    if (schema.has("exclusiveMaximum")) {
      double exclusiveMaximum = schema.get("exclusiveMaximum").getAsDouble();
      if (instanceNumber.doubleValue() >= exclusiveMaximum)
        return false;
    }

    return true;
  }

  /**
   * Matches an instance (or sub-instance) string against a schema (or sub-schema).
   *
   * @param instanceString the instance (or sub-instance) string.
   * @param schema         the schema (or sub-schema).
   * @return true if an instance (or sub-instance) string matches against the sub-schema, otherwise false.
   */
  private boolean stringMatches(String instanceString, JsonObject schema) {
    // For now, we have no rules for strings.
    return true;
  }

  /**
   * Matches an instance (or sub-instance) null against a schema (or sub-schema).
   *
   * @param instanceNull the instance (or sub-instance) null.
   * @param schema       the schema (or sub-schema).
   * @return true if the instance (or sub-instance) matches against the schema, otherwise false.
   */
  private boolean nullMatches(JsonNull instanceNull, JsonObject schema) {
    // For now, we have no rules for nulls.
    return true;
  }

  /**
   * Matches the schema against a JSON instance.
   *
   * @param jsonInstance the JSON instance as a {@code String}.
   * @return true if the instance matches against the schema, otherwise false.
   */
  public boolean matches(String jsonInstance) {
    JsonElement instanceRoot = new JsonParser().parse(jsonInstance);
    return matches(instanceRoot, schemaRoot);
  }

  /**
   * Matches an instance (or sub-instance) against a schema (or sub-schema).
   *
   * @param instance the instance (or sub-instance).
   * @param schema   the schema (or sub-schema).
   * @return true if the instance matches against the schema, otherwise false.
   */
  public boolean matches(JsonElement instance, JsonElement schema) {
    if (schema.isJsonPrimitive())
      return schema.getAsBoolean();

    // If the sub-schema is not a boolean it must be an object.
    JsonObject schemaObject = schema.getAsJsonObject();

    // Check that the instance has the correct type as defined by the sub-schema.
    if (!matchingTypes(instance, schemaObject))
      return false;

    // Depending on the type of the instance we should perform the matching algorithm differently.
    if (instance.isJsonObject()) {
      return objectMatches(instance.getAsJsonObject(), schemaObject);
    } else if (instance.isJsonArray()) {
      return arrayMatches(instance.getAsJsonArray(), schemaObject);
    } else if (instance.isJsonPrimitive()) {
      return primitiveMatches(instance.getAsJsonPrimitive(), schemaObject);
    } else {
      return false;
    }
  }
}
