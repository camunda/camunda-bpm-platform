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

import org.camunda.bpm.engine.impl.delegate.DelegateInvocation;

/**
 * @author Roman Smirnov
 *
 */
public class FormFieldValidatorInvocation extends DelegateInvocation {

  protected FormFieldValidator formFieldValidator;
  protected Object submittedValue;
  protected FormFieldValidatorContext validatorContext;

  public FormFieldValidatorInvocation(FormFieldValidator formFieldValidator, Object submittedValue, FormFieldValidatorContext validatorContext) {
    super(null, null);
    this.formFieldValidator = formFieldValidator;
    this.submittedValue = submittedValue;
    this.validatorContext = validatorContext;
  }

  protected void invoke() throws Exception {
    invocationResult = formFieldValidator.validate(submittedValue, validatorContext);
  }

  public Boolean getInvocationResult() {
    return (Boolean) super.getInvocationResult();
  }

}
