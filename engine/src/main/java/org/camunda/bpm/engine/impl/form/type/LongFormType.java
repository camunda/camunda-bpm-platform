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
import org.camunda.bpm.engine.variable.value.LongValue;
import org.camunda.bpm.engine.variable.value.TypedValue;



/**
 * @author Tom Baeyens
 */
public class LongFormType extends SimpleFormFieldType {

  public final static String TYPE_NAME = "long";

  public String getName() {
    return TYPE_NAME;
  }

  public TypedValue convertValue(TypedValue propertyValue) {
    if(propertyValue instanceof LongValue) {
      return propertyValue;
    }
    else {
      Object value = propertyValue.getValue();
      if(value == null) {
        return Variables.longValue(null, propertyValue.isTransient());
      }
      else if((value instanceof Number) || (value instanceof String)) {
        return Variables.longValue(new Long(value.toString()), propertyValue.isTransient());
      }
      else {
        throw new ProcessEngineException("Value '"+value+"' is not of type Long.");
      }
    }
  }

  // deprecated ////////////////////////////////////////////

  public Object convertFormValueToModelValue(Object propertyValue) {
    if (propertyValue==null || "".equals(propertyValue)) {
      return null;
    }
    return new Long(propertyValue.toString());
  }

  public String convertModelValueToFormValue(Object modelValue) {
    if (modelValue==null) {
      return null;
    }
    return modelValue.toString();
  }


}
