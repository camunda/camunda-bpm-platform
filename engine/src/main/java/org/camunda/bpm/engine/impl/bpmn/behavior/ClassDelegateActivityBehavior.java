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

import org.camunda.bpm.application.InvocationContext;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
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

  protected static final BpmnBehaviorLogger LOG = ProcessEngineLogger.BPMN_BEHAVIOR_LOGGER;

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
  @Override
  public void execute(final ActivityExecution execution) throws Exception {
    this.executeWithErrorPropagation(execution, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        getActivityBehaviorInstance(execution).execute(execution);
        return null;
      }
    });
  }

  // Signallable activity behavior
  @Override
  public void signal(final ActivityExecution execution, final String signalName, final Object signalData) throws Exception {
    ProcessApplicationReference targetProcessApplication = ProcessApplicationContextUtil.getTargetProcessApplication((ExecutionEntity) execution);
    if(ProcessApplicationContextUtil.requiresContextSwitch(targetProcessApplication)) {
      Context.executeWithinProcessApplication(new Callable<Void>() {
        public Void call() throws Exception {
          signal(execution, signalName, signalData);
          return null;
        }
      }, targetProcessApplication, new InvocationContext(execution));
    }
    else {
      doSignal(execution, signalName, signalData);
    }
  }

  protected void doSignal(final ActivityExecution execution, final String signalName, final Object signalData) throws Exception {
    final ActivityBehavior activityBehaviorInstance = getActivityBehaviorInstance(execution);

    if (activityBehaviorInstance instanceof CustomActivityBehavior) {
      CustomActivityBehavior behavior = (CustomActivityBehavior) activityBehaviorInstance;
      ActivityBehavior delegate = behavior.getDelegateActivityBehavior();

      if (!(delegate instanceof SignallableActivityBehavior)) {
        throw LOG.incorrectlyUsedSignalException(SignallableActivityBehavior.class.getName() );
      }
    }
    executeWithErrorPropagation(execution, new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        ((SignallableActivityBehavior) activityBehaviorInstance).signal(execution, signalName, signalData);
        return null;
      }
    });
  }

  protected ActivityBehavior getActivityBehaviorInstance(ActivityExecution execution) {
    Object delegateInstance = instantiateDelegate(className, fieldDeclarations);

    if (delegateInstance instanceof ActivityBehavior) {
      return new CustomActivityBehavior((ActivityBehavior) delegateInstance);
    } else if (delegateInstance instanceof JavaDelegate) {
      return new ServiceTaskJavaDelegateActivityBehavior((JavaDelegate) delegateInstance);
    } else {
      throw LOG.missingDelegateParentClassException(
        delegateInstance.getClass().getName(),
        JavaDelegate.class.getName(),
        ActivityBehavior.class.getName()
      );
    }
  }

}
