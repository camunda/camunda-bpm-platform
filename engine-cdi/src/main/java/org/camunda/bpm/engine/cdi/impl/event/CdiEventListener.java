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
package org.camunda.bpm.engine.cdi.impl.event;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.cdi.BusinessProcessEvent;
import org.camunda.bpm.engine.cdi.BusinessProcessEventType;
import org.camunda.bpm.engine.cdi.annotation.event.*;
import org.camunda.bpm.engine.cdi.impl.util.BeanManagerLookup;
import org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.ExecutionContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;

import javax.enterprise.inject.spi.BeanManager;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Generic {@link ExecutionListener} publishing events using the cdi event
 * infrastructure.
 *
 * @author Daniel Meyer
 */
public class CdiEventListener implements TaskListener, ExecutionListener, Serializable {

  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = Logger.getLogger(CdiEventListener.class.getName());

  public void notify(DelegateExecution execution) throws Exception {
    // test whether cdi is setup correctly. (if not, just do not deliver the event)
    if (!testCdiSetup()) {
      return;
    }

    BusinessProcessEvent event = createEvent(execution);
    Annotation[] qualifiers = getQualifiers(event);
    getBeanManager().fireEvent(event, qualifiers);
  }

  public void notify(DelegateTask task) {
    // test whether cdi is setup correctly. (if not, just do not deliver the event)
    if (!testCdiSetup()) {
      return;
    }

    BusinessProcessEvent event = createEvent(task);
    Annotation[] qualifiers = getQualifiers(event);
    getBeanManager().fireEvent(event, qualifiers);
  }

  private boolean testCdiSetup() {
    try {
      ProgrammaticBeanLookup.lookup(ProcessEngine.class);
    }
    catch (Exception e) {
      LOGGER.fine("CDI was not setup correctly");
      return false;
    }
    return true;
  }

  protected BusinessProcessEvent createEvent(DelegateExecution execution) {
    ProcessDefinition processDefinition = Context.getExecutionContext().getProcessDefinition();

    // map type
    String eventName = execution.getEventName();
    BusinessProcessEventType type = null;
    if(ExecutionListener.EVENTNAME_START.equals(eventName)) {
      type = BusinessProcessEventType.START_ACTIVITY;
    } else if(ExecutionListener.EVENTNAME_END.equals(eventName)) {
      type = BusinessProcessEventType.END_ACTIVITY;
    } else if(ExecutionListener.EVENTNAME_TAKE.equals(eventName)) {
      type = BusinessProcessEventType.TAKE;
    }

    return new CdiBusinessProcessEvent(execution.getCurrentActivityId(), execution.getCurrentTransitionId(), processDefinition, execution, type, ClockUtil.getCurrentTime());
  }

  protected BusinessProcessEvent createEvent(DelegateTask task) {
    ExecutionContext executionContext = Context.getExecutionContext();
    ProcessDefinitionEntity processDefinition = null;
    if (executionContext != null) {
      processDefinition = executionContext.getProcessDefinition();
    }

    // map type
    String eventName = task.getEventName();
    BusinessProcessEventType type = null;
    if (TaskListener.EVENTNAME_CREATE.equals(eventName)) {
      type = BusinessProcessEventType.CREATE_TASK;
    }
    else if (TaskListener.EVENTNAME_ASSIGNMENT.equals(eventName)) {
      type = BusinessProcessEventType.ASSIGN_TASK;
    }
    else if (TaskListener.EVENTNAME_COMPLETE.equals(eventName)) {
      type = BusinessProcessEventType.COMPLETE_TASK;
    }
    else if (TaskListener.EVENTNAME_DELETE.equals(eventName)) {
      type = BusinessProcessEventType.DELETE_TASK;
    }

    return new CdiBusinessProcessEvent(task, processDefinition, type, ClockUtil.getCurrentTime());
  }


  protected BeanManager getBeanManager() {
    BeanManager bm = BeanManagerLookup.getBeanManager();
    if (bm == null) {
      throw new ProcessEngineException("No cdi bean manager available, cannot publish event.");
    }
    return bm;
  }

  protected Annotation[] getQualifiers(BusinessProcessEvent event) {
    ProcessDefinition processDefinition = event.getProcessDefinition();
    List<Annotation> annotations = new ArrayList<Annotation>();
    if (processDefinition != null) {
      annotations.add(new BusinessProcessDefinitionLiteral(processDefinition.getKey()));
    }

    if (event.getType() == BusinessProcessEventType.TAKE) {
      annotations.add(new TakeTransitionLiteral(event.getTransitionName()));
    }
    else if (event.getType() == BusinessProcessEventType.START_ACTIVITY) {
      annotations.add(new StartActivityLiteral(event.getActivityId()));
    }
    else if (event.getType() == BusinessProcessEventType.END_ACTIVITY) {
      annotations.add(new EndActivityLiteral(event.getActivityId()));
    }
    else if (event.getType() == BusinessProcessEventType.CREATE_TASK) {
      annotations.add(new CreateTaskLiteral(event.getTaskDefinitionKey()));
    }
    else if (event.getType() == BusinessProcessEventType.ASSIGN_TASK) {
      annotations.add(new AssignTaskLiteral(event.getTaskDefinitionKey()));
    }
    else if (event.getType() == BusinessProcessEventType.COMPLETE_TASK) {
      annotations.add(new CompleteTaskLiteral(event.getTaskDefinitionKey()));
    }
    else if (event.getType() == BusinessProcessEventType.DELETE_TASK) {
      annotations.add(new DeleteTaskLiteral(event.getTaskDefinitionKey()));
    }
    return annotations.toArray(new Annotation[annotations.size()]);
  }
}
