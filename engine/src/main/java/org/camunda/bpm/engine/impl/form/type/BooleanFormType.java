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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.BooleanValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Frederik Heremans
 */
public class BooleanFormType extends SimpleFormFieldType {

  public final static String TYPE_NAME = "boolean";

  public String getName() {
    return TYPE_NAME;
  }

  public TypedValue convertValue(TypedValue propertyValue) {
    if(propertyValue instanceof BooleanValue) {
      return propertyValue;
    }
    else {
      Object value = propertyValue.getValue();
      if(value == null) {
        return Variables.booleanValue(null, propertyValue.isTransient());
      }
      else if((value instanceof Boolean) || (value instanceof String)) {
        return Variables.booleanValue(new Boolean(value.toString()), propertyValue.isTransient());
      }
      else {
        throw new ProcessEngineException("Value '"+value+"' is not of type Boolean.");
      }
    }
  }
  // deprecated /////////////////////////////////////////////////

  public Object convertFormValueToModelValue(Object propertyValue) {
    if (propertyValue==null || "".equals(propertyValue)) {
      return null;
    }
    return Boolean.valueOf(propertyValue.toString());
  }

  public String convertModelValueToFormValue(Object modelValue) {

    if (modelValue==null) {
      return null;
    }

    if(Boolean.class.isAssignableFrom(modelValue.getClass())
            || boolean.class.isAssignableFrom(modelValue.getClass())) {
      return modelValue.toString();
    }
    throw new ProcessEngineException("Model value is not of type boolean, but of type " + modelValue.getClass().getName());
  }

}
