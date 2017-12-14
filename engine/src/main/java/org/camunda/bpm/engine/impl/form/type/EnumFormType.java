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

package org.camunda.bpm.engine.impl.form.type;

import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;


/**
 * @author Tom Baeyens
 */
public class EnumFormType extends SimpleFormFieldType {

  public final static String TYPE_NAME = "enum";

  protected Map<String, String> values;

  public EnumFormType(Map<String, String> values) {
    this.values = values;
  }

  public String getName() {
    return TYPE_NAME;
  }

  @Override
  public Object getInformation(String key) {
    if (key.equals("values")) {
      return values;
    }
    return null;
  }

  public TypedValue convertValue(TypedValue propertyValue) {
    Object value = propertyValue.getValue();
    if(value == null || String.class.isInstance(value)) {
      validateValue(value);
      return Variables.stringValue((String) value, propertyValue.isTransient());
    }
    else {
      throw new ProcessEngineException("Value '"+value+"' is not of type String.");
    }
  }

  protected void validateValue(Object value) {
    if(value != null) {
      if(values != null && !values.containsKey(value)) {
        throw new ProcessEngineException("Invalid value for enum form property: " + value);
      }
    }
  }

  public Map<String, String> getValues() {
    return values;
  }

  //////////////////// deprecated ////////////////////////////////////////

  @Override
  public Object convertFormValueToModelValue(Object propertyValue) {
    validateValue(propertyValue);
    return propertyValue;
  }

  @Override
  public String convertModelValueToFormValue(Object modelValue) {
    if(modelValue != null) {
      if(!(modelValue instanceof String)) {
        throw new ProcessEngineException("Model value should be a String");
      }
      validateValue(modelValue);
    }
    return (String) modelValue;
  }

}
