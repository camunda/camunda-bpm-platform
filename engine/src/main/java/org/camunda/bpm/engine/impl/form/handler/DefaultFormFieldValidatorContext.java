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

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidatorContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * @author Daniel Meyer
 *
 */
public class DefaultFormFieldValidatorContext implements FormFieldValidatorContext {

  protected VariableScope variableScope;
  protected String configuration;
  protected VariableMap submittedValues;
  protected FormFieldHandler formFieldHandler;

  public DefaultFormFieldValidatorContext(VariableScope variableScope, String configuration, VariableMap submittedValues,
    FormFieldHandler formFieldHandler) {
    super();
    this.variableScope = variableScope;
    this.configuration = configuration;
    this.submittedValues = submittedValues;
    this.formFieldHandler = formFieldHandler;
  }

  public FormFieldHandler getFormFieldHandler() {
    return formFieldHandler;
  }

  public DelegateExecution getExecution() {
    if(variableScope instanceof DelegateExecution) {
      return (DelegateExecution) variableScope;
    }
    else if(variableScope instanceof TaskEntity){
      return ((TaskEntity) variableScope).getExecution();
    }
    else {
      return null;
    }
  }

  public VariableScope getVariableScope() {
    return variableScope;
  }

  public String getConfiguration() {
    return configuration;
  }

  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  public Map<String, Object> getSubmittedValues() {
    return submittedValues;
  }

}
