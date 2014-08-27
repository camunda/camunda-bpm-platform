/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.variable;

import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.ProcessEngineVariableType;
import org.camunda.bpm.engine.delegate.SerializedVariableValue;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.variable.SerializedVariableValueImpl;


/**
 * Variable type capable of storing reference to JPA-entities. Only JPA-Entities which
 * are configured by annotations are supported. Use of compound primary keys is not supported.
 *
 * @author Frederik Heremans
 */
public class JPAEntityVariableType implements VariableType {

  public static final String CONFIG_CLASS_NAME = ProcessEngineVariableType.JPA_TYPE_CONFIG_CLASS_NAME;
  public static final String CONFIG_ENTITY_ID_STRING = ProcessEngineVariableType.JPA_TYPE_CONFIG_ENTITY_ID;

  private JPAEntityMappings mappings;

  public JPAEntityVariableType() {
    mappings = new JPAEntityMappings();
  }

  public String getTypeName() {
    return ProcessEngineVariableType.JPA.getName();
  }

  public boolean isCachable() {
    return false;
  }

  public boolean isAbleToStore(Object value) {
    if(value == null) {
      return true;
    }
    return mappings.isJPAEntity(value);
  }

  public void setValue(Object value, ValueFields valueFields) {
    EntityManagerSession entityManagerSession = Context
      .getCommandContext()
      .getSession(EntityManagerSession.class);
    if (entityManagerSession == null) {
      throw new ProcessEngineException("Cannot set JPA variable: " + EntityManagerSession.class + " not configured");
    } else {
      // Before we set the value we must flush all pending changes from the entitymanager
      // If we don't do this, in some cases the primary key will not yet be set in the object
      // which will cause exceptions down the road.
      entityManagerSession.flush();
    }

    if(value != null) {
      String className = mappings.getJPAClassString(value);
      String idString = mappings.getJPAIdString(value);
      valueFields.setTextValue(className);
      valueFields.setTextValue2(idString);
    } else {
      valueFields.setTextValue(null);
      valueFields.setTextValue2(null);
    }
  }

  public Object getValue(ValueFields valueFields) {
    if(valueFields.getTextValue() != null && valueFields.getTextValue2() != null) {
      return mappings.getJPAEntity(valueFields.getTextValue(), valueFields.getTextValue2());
    }
    return null;
  }

  public String getTypeNameForValue(ValueFields valueFields) {
    return Object.class.getSimpleName();
  }

  public SerializedVariableValue getSerializedValue(ValueFields valueFields) {
    SerializedVariableValueImpl result = new SerializedVariableValueImpl();
    result.setConfigValue(CONFIG_CLASS_NAME, valueFields.getTextValue());
    result.setConfigValue(CONFIG_ENTITY_ID_STRING, valueFields.getTextValue2());

    return result;
  }

  public void setValueFromSerialized(Object serializedValue, Map<String, Object> configuration, ValueFields valueFields) {
    valueFields.setTextValue((String) configuration.get(CONFIG_CLASS_NAME));
    valueFields.setTextValue2((String) configuration.get(CONFIG_ENTITY_ID_STRING));
  }

  public boolean isAbleToStoreSerializedValue(Object value, Map<String, Object> configuration) {
    return value == null
        && configuration != null
        && configuration.get(CONFIG_CLASS_NAME) instanceof String
        && configuration.get(CONFIG_ENTITY_ID_STRING) instanceof String;
  }

  public boolean storesCustomObjects() {
    return true;
  }

}
