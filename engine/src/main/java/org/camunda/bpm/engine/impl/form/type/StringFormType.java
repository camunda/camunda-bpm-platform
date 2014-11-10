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

import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;



/**
 * @author Tom Baeyens
 */
public class StringFormType extends AbstractFormFieldType {

  public final static String TYPE_NAME = "string";

  public String getName() {
    return TYPE_NAME;
  }

  public String getMimeType() {
    return "text/plain";
  }

  public Object convertFormValueToModelValue(Object propertyValue) {
    return propertyValue.toString();
  }

  public String convertModelValueToFormValue(Object modelValue) {
    return (String) modelValue;
  }

  public TypedValue getTypedValue(Object value) {
    return Variables.stringValue((String) value);
  }

}
