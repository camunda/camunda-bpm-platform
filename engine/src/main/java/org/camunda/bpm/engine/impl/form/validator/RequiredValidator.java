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
package org.camunda.bpm.engine.impl.form.validator;

import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Daniel Meyer
 *
 */
public class RequiredValidator implements FormFieldValidator {

  public boolean validate(Object submittedValue, FormFieldValidatorContext validatorContext) {
    if(submittedValue == null) {
      TypedValue value = validatorContext.getVariableScope().getVariableTyped(validatorContext.getFormFieldHandler().getId());
      return (value != null && value.getValue() != null);
    } else {
      if (submittedValue instanceof String) {
        return submittedValue != null && !((String)submittedValue).isEmpty();
      } else {
        return submittedValue != null;
      }
    }
  }

}
