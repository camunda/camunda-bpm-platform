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
package org.camunda.bpm.engine.impl.bpmn.behavior;

import static org.camunda.bpm.engine.impl.util.ClassDelegateUtil.applyFieldDeclaration;

import java.util.List;
import java.util.concurrent.Callable;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.bpmn.delegate.ActivityBehaviorInvocation;
import org.camunda.bpm.engine.impl.bpmn.delegate.JavaDelegateInvocation;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.ProcessApplicationContextUtil;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.SignallableActivityBehavior;


/**
 * {@link ActivityBehavior} used when 'delegateExpression' is used
 * for a serviceTask.
 *
 * @author Joram Barrez
 * @author Josh Long
 * @author Slawomir Wojtasiak (Patch for ACT-1159)
 * @author Falko Menge
 */
public class ServiceTaskDelegateExpressionActivityBehavior extends TaskActivityBehavior {

  protected Expression expression;
  private final List<FieldDeclaration> fieldDeclarations;

  public ServiceTaskDelegateExpressionActivityBehavior(Expression expression, List<FieldDeclaration> fieldDeclarations) {
    this.expression = expression;
    this.fieldDeclarations = fieldDeclarations;
  }

  @Override
  public void signal(final ActivityExecution execution, final String signalName, final Object signalData) throws Exception {
    ProcessApplicationReference targetProcessApplication = ProcessApplicationContextUtil.getTargetProcessApplication((ExecutionEntity) execution);

    if (!ProcessApplicationContextUtil.requiresContextSwitch(targetProcessApplication)) {

      Object delegate = expression.getValue(execution);
      applyFieldDeclaration(fieldDeclarations, delegate);
      ActivityBehavior activityBehaviorInstance = getActivityBehaviorInstance(execution, delegate);

      if (activityBehaviorInstance instanceof SignallableActivityBehavior) {
        try {
          ((SignallableActivityBehavior) activityBehaviorInstance).signal(execution, signalName, signalData);
        }
        catch (BpmnError error) {
          propagateBpmnError(error, execution);
        }
        catch (Exception exception) {
          propagateExceptionAsError(exception, execution);
        }
      }

    } else {
      Context.executeWithinProcessApplication(new Callable<Void>() {

        public Void call() throws Exception {
          try {
            signal(execution, signalName, signalData);
          }
          catch (BpmnError error) {
            propagateBpmnError(error, execution);
          }
          catch (Exception exception) {
            propagateExceptionAsError(exception, execution);
          }
          return null;
        }

      }, targetProcessApplication);
    }
  }

	public void execute(ActivityExecution execution) throws Exception {

    try {

      // Note: we can't cache the result of the expression, because the
      // execution can change: eg. delegateExpression='${mySpringBeanFactory.randomSpringBean()}'
      Object delegate = expression.getValue(execution);
      applyFieldDeclaration(fieldDeclarations, delegate);

      if (delegate instanceof ActivityBehavior) {
        Context.getProcessEngineConfiguration()
          .getDelegateInterceptor()
          .handleInvocation(new ActivityBehaviorInvocation((ActivityBehavior) delegate, execution));

      } else if (delegate instanceof JavaDelegate) {
        Context.getProcessEngineConfiguration()
          .getDelegateInterceptor()
          .handleInvocation(new JavaDelegateInvocation((JavaDelegate) delegate, execution));
        leave(execution);

      } else {
        throw new ProcessEngineException("Delegate expression " + expression
                + " did neither resolve to an implementation of " + ActivityBehavior.class
                + " nor " + JavaDelegate.class);
      }
    } catch (Exception exc) {

      Throwable cause = exc;
      BpmnError error = null;
      while (cause != null) {
        if (cause instanceof BpmnError) {
          error = (BpmnError) cause;
          break;
        }
        cause = cause.getCause();
      }

      if (error != null) {
        propagateBpmnError(error, execution);
      } else {
        propagateExceptionAsError(exc, execution);
      }

    }
  }

  protected ActivityBehavior getActivityBehaviorInstance(ActivityExecution execution, Object delegateInstance) {

    if (delegateInstance instanceof ActivityBehavior) {
      return (ActivityBehavior) delegateInstance;
    } else if (delegateInstance instanceof JavaDelegate) {
      return new ServiceTaskJavaDelegateActivityBehavior((JavaDelegate) delegateInstance);
    } else {
      throw new ProcessEngineException(delegateInstance.getClass().getName() + " doesn't implement " + JavaDelegate.class.getName() + " nor "
          + ActivityBehavior.class.getName());
    }
  }

}
