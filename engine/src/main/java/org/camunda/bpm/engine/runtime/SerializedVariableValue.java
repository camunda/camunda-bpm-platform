package org.camunda.bpm.engine.runtime;

import java.util.Map;

import org.camunda.bpm.engine.impl.variable.VariableType;

public interface SerializedVariableValue {

  /**
   * Returns the serialized representation of the variable.
   * For primitive types (integer, string, etc.), serialized values are the same as the regular values.
   * For object types, serialized values return the representation of these objects
   * as stored in the database.
   */
  Object getValue();

  /**
   * Returns variable configuration that is required for the serialized that provides
   * meaning to the serialized value. For example, the configuration could contain
   * the class name of the serialized object. The actual configuration depends on the
   * {@link VariableType}; These classes also provide constants for accessing the expected
   * configuration properties.
   */
  Map<String, Object> getConfig();
}
