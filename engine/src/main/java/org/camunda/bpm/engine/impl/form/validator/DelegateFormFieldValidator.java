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

import org.camunda.bpm.application.InvocationContext;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.ProcessApplicationContextUtil;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
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
      ProcessApplicationReference processApplicationReference = ProcessApplicationContextUtil.getTargetProcessApplication((ExecutionEntity) execution);

      return Context.executeWithinProcessApplication(new Callable<Boolean>() {
        public Boolean call() throws Exception {
          return doValidate(submittedValue, validatorContext);
        }
      }, processApplicationReference, new InvocationContext(execution));

    } else {
      return doValidate(submittedValue, validatorContext);

    }

  }

  protected boolean shouldPerformPaContextSwitch(DelegateExecution execution) {
    if(execution == null) {
      return false;
    } else {
      ProcessApplicationReference targetPa = ProcessApplicationContextUtil.getTargetProcessApplication((ExecutionEntity) execution);
      return targetPa != null && !targetPa.equals(Context.getCurrentProcessApplication());
    }
  }

  protected boolean doValidate(Object submittedValue, FormFieldValidatorContext validatorContext) {
    FormFieldValidator validator;

    if(clazz != null) {
      // resolve validator using Fully Qualified Classname
      Object validatorObject = ReflectUtil.instantiate(clazz);
      if(validatorObject instanceof FormFieldValidator) {
        validator = (FormFieldValidator) validatorObject;

      } else {
        throw new ProcessEngineException("Validator class '"+clazz+"' is not an instance of "+ FormFieldValidator.class.getName());

      }
    } else {
      //resolve validator using expression
      Object validatorObject = delegateExpression.getValue(validatorContext.getExecution());
      if (validatorObject instanceof FormFieldValidator) {
        validator = (FormFieldValidator) validatorObject;

      } else {
        throw new ProcessEngineException("Validator expression '"+delegateExpression+"' does not resolve to instance of "+ FormFieldValidator.class.getName());

      }
    }

    FormFieldValidatorInvocation invocation = new FormFieldValidatorInvocation(validator, submittedValue, validatorContext);
    try {
      Context
        .getProcessEngineConfiguration()
        .getDelegateInterceptor()
        .handleInvocation(invocation);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new ProcessEngineException(e);
    }

    return invocation.getInvocationResult();
  }

}
