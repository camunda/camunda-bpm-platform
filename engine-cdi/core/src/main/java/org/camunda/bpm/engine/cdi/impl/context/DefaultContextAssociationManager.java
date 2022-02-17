/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.cdi.impl.context;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Scope;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.cdi.ProcessEngineCdiException;
import org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;
import org.camunda.bpm.engine.impl.context.BpmnExecutionContext;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Default implementation of the business process association manager. Uses a
 * fallback-strategy to associate the process instance with the "broadest"
 * active scope, starting with the conversation.
 * <p />
 * Subclass in order to implement custom association schemes and association
 * with custom scopes.
 *
 * @author Daniel Meyer
 */
@SuppressWarnings("serial")
public class DefaultContextAssociationManager implements ContextAssociationManager, Serializable {

  protected final static Logger log = Logger.getLogger(DefaultContextAssociationManager.class.getName());

  @Inject private BeanManager beanManager;

  protected Class< ? extends ScopedAssociation> getBroadestActiveContext() {
    for (Class< ? extends ScopedAssociation> scopeType : getAvailableScopedAssociationClasses()) {
      Annotation scopeAnnotation = scopeType.getAnnotations().length > 0 ? scopeType.getAnnotations()[0] : null;
      if (scopeAnnotation == null || !beanManager.isScope(scopeAnnotation.annotationType())) {
        throw new ProcessEngineException("ScopedAssociation must carry exactly one annotation and it must be a @Scope annotation");
      }
      try {
        beanManager.getContext(scopeAnnotation.annotationType());
        return scopeType;
      } catch (ContextNotActiveException e) {
        log.finest("Context " + scopeAnnotation.annotationType() + " not active.");
      }
    }
    throw new ProcessEngineException("Could not determine an active context to associate the current process instance / task instance with.");
  }

  /**
   * Override to add different / additional contexts.
   *
   * @returns a list of {@link Scope}-types, which are used in the given order
   *          to resolve the broadest active context (@link
   *          #getBroadestActiveContext()})
   */
  protected List<Class< ? extends ScopedAssociation>> getAvailableScopedAssociationClasses() {
    ArrayList<Class< ? extends ScopedAssociation>> scopeTypes = new ArrayList<Class< ? extends ScopedAssociation>>();
    scopeTypes.add(ConversationScopedAssociation.class);
    scopeTypes.add(RequestScopedAssociation.class);
    return scopeTypes;
  }

  protected ScopedAssociation getScopedAssociation() {
    return ProgrammaticBeanLookup.lookup(getBroadestActiveContext(), beanManager);
  }

  @Override
  public void setExecution(Execution execution) {
    if(execution == null) {
      throw new ProcessEngineCdiException("Cannot associate with execution: null");
    }
    ensureCommandContextNotActive();

    ScopedAssociation scopedAssociation = getScopedAssociation();
    Execution associatedExecution = scopedAssociation.getExecution();
    if(associatedExecution!=null && !associatedExecution.getId().equals(execution.getId())) {
      throw new ProcessEngineCdiException("Cannot associate "+execution+", already associated with "+associatedExecution+". Disassociate first!");
    }

    if (log.isLoggable(Level.FINE)) {
      log.fine("Associating "+execution+" (@"
                + scopedAssociation.getClass().getAnnotations()[0].annotationType().getSimpleName() + ")");
    }
    scopedAssociation.setExecution(execution);
  }

  @Override
  public void disAssociate() {
    ensureCommandContextNotActive();
    ScopedAssociation scopedAssociation = getScopedAssociation();
    if (scopedAssociation.getExecution() == null) {
      throw new ProcessEngineException("Cannot dissasociate execution, no "
                + scopedAssociation.getClass().getAnnotations()[0].annotationType().getSimpleName()
                + " execution associated. ");
    }
    if (log.isLoggable(Level.FINE)) {
      log.fine("Disassociating");
    }
    scopedAssociation.setExecution(null);
    scopedAssociation.setTask(null);
  }

  @Override
  public String getExecutionId() {
    Execution execution = getExecution();
    if (execution != null) {
      return execution.getId();
    } else {
      return null;
    }
  }

  @Override
  public Execution getExecution() {
    ExecutionEntity execution = getExecutionFromContext();
    if(execution != null) {
      return execution;
    } else {
      return getScopedAssociation().getExecution();
    }
  }

  @Override
  public TypedValue getVariable(String variableName) {
    ExecutionEntity execution = getExecutionFromContext();
    if (execution != null) {
      return execution.getVariableTyped(variableName);
    } else {
      return getScopedAssociation().getVariable(variableName);
    }
  }

  @Override
  public void setVariable(String variableName, Object value) {
    ExecutionEntity execution = getExecutionFromContext();
    if(execution != null) {
      execution.setVariable(variableName, value);
      execution.getVariable(variableName);
    } else {
      getScopedAssociation().setVariable(variableName, value);
    }
  }

  @Override
  public TypedValue getVariableLocal(String variableName) {
    ExecutionEntity execution = getExecutionFromContext();
    if (execution != null) {
      return execution.getVariableLocalTyped(variableName);
    } else {
      return getScopedAssociation().getVariableLocal(variableName);
    }
  }

  @Override
  public void setVariableLocal(String variableName, Object value) {
    ExecutionEntity execution = getExecutionFromContext();
    if(execution != null) {
      execution.setVariableLocal(variableName, value);
      execution.getVariableLocal(variableName);
    } else {
      getScopedAssociation().setVariableLocal(variableName, value);
    }
  }

  protected ExecutionEntity getExecutionFromContext() {
    if(Context.getCommandContext() != null) {
      BpmnExecutionContext executionContext = Context.getBpmnExecutionContext();
      if(executionContext != null) {
        return executionContext.getExecution();
      }
    }
    return null;
  }

  public Task getTask() {
    ensureCommandContextNotActive();
    return getScopedAssociation().getTask();
  }

  public void setTask(Task task) {
    ensureCommandContextNotActive();
    getScopedAssociation().setTask(task);
  }

  public VariableMap getCachedVariables() {
    ensureCommandContextNotActive();
    return getScopedAssociation().getCachedVariables();
  }

  public VariableMap getCachedLocalVariables() {
    ensureCommandContextNotActive();
    return getScopedAssociation().getCachedVariablesLocal();
  }

  public void flushVariableCache() {
    ensureCommandContextNotActive();
    getScopedAssociation().flushVariableCache();
  }

  protected void ensureCommandContextNotActive() {
    if(Context.getCommandContext() != null) {
      throw new ProcessEngineCdiException("Cannot work with scoped associations inside command context.");
    }
  }

}
