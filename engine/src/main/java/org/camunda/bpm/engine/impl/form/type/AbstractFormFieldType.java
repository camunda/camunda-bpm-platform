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

import org.camunda.bpm.engine.form.FormType;
import org.camunda.bpm.engine.variable.value.TypedValue;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public abstract class AbstractFormFieldType implements FormType {

  public abstract String getName();

  public abstract TypedValue convertToFormValue(TypedValue propertyValue);

  public abstract TypedValue convertToModelValue(TypedValue propertyValue);

  @Deprecated
  public abstract Object convertFormValueToModelValue(Object propertyValue);

  @Deprecated
  public abstract String convertModelValueToFormValue(Object modelValue);

  public Object getInformation(String key) {
    return null;
  }

}
