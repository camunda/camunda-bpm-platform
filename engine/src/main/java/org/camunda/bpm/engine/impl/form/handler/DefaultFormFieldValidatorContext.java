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
import org.camunda.bpm.engine.form.FormData;
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidatorContext;

/**
 * @author Daniel Meyer
 *
 */
public class DefaultFormFieldValidatorContext implements FormFieldValidatorContext {

  protected DelegateExecution execution;
  protected String configuration;
  protected Map<String, Object> submittedValues;

  public DefaultFormFieldValidatorContext(DelegateExecution execution, String configuration, Map<String, Object> submittedValues) {
    super();
    this.execution = execution;
    this.configuration = configuration;
    this.submittedValues = submittedValues;
  }

  public DelegateExecution getExecution() {
    return execution;
  }

  public void setExecution(DelegateExecution execution) {
    this.execution = execution;
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

  public void setSubmittedValues(Map<String, Object> submittedValues) {
    this.submittedValues = submittedValues;
  }

}
