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
package org.camunda.bpm.engine.impl.form.handler;

import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.form.FormFieldValidationConstraint;
import org.camunda.bpm.engine.impl.form.FormFieldValidationConstraintImpl;
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidator;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * <p>Wrapper for a validation constraint</p>
 *
 * @author Daniel Meyer
 *
 */
public class FormFieldValidationConstraintHandler {

  protected String name;
  protected String config;
  protected FormFieldValidator validator;

  public FormFieldValidationConstraint createValidationConstraint(ExecutionEntity execution) {
    return new FormFieldValidationConstraintImpl(name, config);
  }

  // submit /////////////////////////////////

  public void validate(Object submittedValue, Map<String, Object> submittedValues, FormFieldHandler formFieldHandler, ExecutionEntity execution) {
    boolean isValid = validator.validate(submittedValue, new DefaultFormFieldValidatorContext(execution, config, submittedValues));
    if(!isValid) {
      throw new ProcessEngineException("Invalid value submitted for form field '"+formFieldHandler.getId()+"': validation of "+this+" failed.");
    }
  }

  // getter / setter ////////////////////////

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setConfig(String config) {
    this.config = config;
  }

  public String getConfig() {
    return config;
  }

  public void setValidator(FormFieldValidator validator) {
    this.validator = validator;
  }

  public FormFieldValidator getValidator() {
    return validator;
  }

  public String toString() {
    return name + (config != null ? ("("+config+")") : "");
  }

}
