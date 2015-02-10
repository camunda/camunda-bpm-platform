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

import static org.camunda.bpm.engine.impl.util.ClassDelegateUtil.instantiateDelegate;

import java.util.List;
import java.util.concurrent.Callable;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.ProcessApplicationContextUtil;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.SignallableActivityBehavior;


/**
 * Helper class for bpmn constructs that allow class delegation.
 *
 * This class will lazily instantiate the referenced classes when needed at runtime.
 *
 * @author Joram Barrez
 * @author Falko Menge
 * @author Roman Smirnov
 */
public class ClassDelegateActivityBehavior extends AbstractBpmnActivityBehavior {

  protected String className;
  protected List<FieldDeclaration> fieldDeclarations;

  public ClassDelegateActivityBehavior(String className, List<FieldDeclaration> fieldDeclarations) {
    this.className = className;
    this.fieldDeclarations = fieldDeclarations;
  }

  public ClassDelegateActivityBehavior(Class<?> clazz, List<FieldDeclaration> fieldDeclarations) {
    this(clazz.getName(), fieldDeclarations);
  }

  // Activity Behavior
  public void execute(ActivityExecution execution) throws Exception {

    ActivityBehavior activityBehaviorInstance = getActivityBehaviorInstance(execution);
    try {
      activityBehaviorInstance.execute(execution);
    } catch (BpmnError error) {
      propagateBpmnError(error, execution);
    } catch (Exception ex) {
      propagateExceptionAsError(ex, execution);
    }
  }

  // Signallable activity behavior
  public void signal(final ActivityExecution execution, final String signalName, final Object signalData) throws Exception {

    ProcessApplicationReference targetProcessApplication = ProcessApplicationContextUtil.getTargetProcessApplication((ExecutionEntity) execution);

    if(!ProcessApplicationContextUtil.requiresContextSwitch(targetProcessApplication)) {
      ActivityBehavior activityBehaviorInstance = getActivityBehaviorInstance(execution);

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
      } else {
        throw new ProcessEngineException("signal() can only be called on a " + SignallableActivityBehavior.class.getName() + " instance");
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

  protected ActivityBehavior getActivityBehaviorInstance(ActivityExecution execution) {
    Object delegateInstance = instantiateDelegate(className, fieldDeclarations);

    if (delegateInstance instanceof ActivityBehavior) {
      return (ActivityBehavior) delegateInstance;
    } else if (delegateInstance instanceof JavaDelegate) {
      return new ServiceTaskJavaDelegateActivityBehavior((JavaDelegate) delegateInstance);
    } else {
      throw new ProcessEngineException(delegateInstance.getClass().getName()+" doesn't implement "+JavaDelegate.class.getName()+" nor "+ActivityBehavior.class.getName());
    }
  }

}
