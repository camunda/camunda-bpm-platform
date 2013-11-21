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

import java.util.concurrent.Callable;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.ProcessApplicationContextUtil;
import org.camunda.bpm.engine.impl.pvm.runtime.InterpretableExecution;
import org.camunda.bpm.engine.impl.util.ReflectUtil;

/**
 * {@link FormFieldValidator} delegating to a custom, user-provided validator implementation.
 * The implementation is resolved either using a fully qualified classname of a Java Class
 * or using a java delegate implementation.
 *
 * @author Daniel Meyer
 */
public class DelegateFormFieldValidator implements FormFieldValidator {

  protected String clazz;
  protected Expression delegateExpression;

  public DelegateFormFieldValidator(Expression expression) {
    delegateExpression = expression;
  }

  public DelegateFormFieldValidator(String clazz) {
    this.clazz = clazz;
  }

  public DelegateFormFieldValidator() {
  }

  @Override
  public boolean validate(final Object submittedValue, final FormFieldValidatorContext validatorContext) {

    final DelegateExecution execution = validatorContext.getExecution();

    if(shouldPerformPaContextSwitch(validatorContext.getExecution())) {
      return Context.executeWithinProcessApplication(new Callable<Boolean>() {
        public Boolean call() throws Exception {
          return doValidate(submittedValue, validatorContext);
        }
      }, ProcessApplicationContextUtil.getTargetProcessApplication((InterpretableExecution) execution));

    } else {
      return doValidate(submittedValue, validatorContext);

    }

  }

  protected boolean shouldPerformPaContextSwitch(DelegateExecution execution) {
    if(execution == null) {
      return false;
    } else {
      ProcessApplicationReference targetPa = ProcessApplicationContextUtil.getTargetProcessApplication((InterpretableExecution) execution);
      return targetPa != null && !targetPa.equals(Context.getCurrentProcessApplication());
    }
  }

  protected boolean doValidate(final Object submittedValue, final FormFieldValidatorContext validatorContext) {
    if(clazz != null) {
      // resolve validator using Fully Qualified Classname
      Object validatorObject = ReflectUtil.instantiate(clazz);
      if(validatorObject instanceof FormFieldValidator) {
        FormFieldValidator validator = (FormFieldValidator) validatorObject;
        return validator.validate(submittedValue, validatorContext);

      } else {
        throw new ProcessEngineException("Validator class '"+clazz+"' is not an instance of "+ FormFieldValidator.class.getName());

      }
    } else {
      //resolve validator using expression
      Object validatorObject = delegateExpression.getValue(validatorContext.getExecution());
      if (validatorObject instanceof FormFieldValidator) {
        FormFieldValidator validator = (FormFieldValidator) validatorObject;
        return validator.validate(submittedValue, validatorContext);

      } else {
        throw new ProcessEngineException("Validator expression '"+delegateExpression+"' does not resolve to instance of "+ FormFieldValidator.class.getName());

      }
    }
  }

}
