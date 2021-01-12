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
package org.camunda.bpm.engine.impl.el;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.bpmn.behavior.ExternalTaskActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.List;


/**
 * Implementation of an {@link ELResolver} that resolves expressions
 * with the process variables of a given {@link VariableScope} as context.
 * <br>
 * Also exposes the currently logged in username to be used in expressions (if any)
 *
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class VariableScopeElResolver extends ELResolver {

  public static final String EXECUTION_KEY = "execution";
  public static final String CASE_EXECUTION_KEY = "caseExecution";
  public static final String TASK_KEY = "task";
  public static final String EXTERNAL_TASK_KEY = "externalTask";
  public static final String LOGGED_IN_USER_KEY = "authenticatedUserId";

  public Object getValue(ELContext context, Object base, Object property)  {

    Object object = context.getContext(VariableScope.class);
    if(object != null) {
      VariableScope variableScope = (VariableScope) object;
      if (base == null) {
        String variable = (String) property; // according to javadoc, can only be a String

        if( EXECUTION_KEY.equals(property) && variableScope instanceof ExecutionEntity
                || TASK_KEY.equals(property) && variableScope instanceof TaskEntity
                || variableScope instanceof CaseExecutionEntity
                && (CASE_EXECUTION_KEY.equals(property) || EXECUTION_KEY.equals(property)) ) {
          context.setPropertyResolved(true);
          return variableScope;
        } else if(EXTERNAL_TASK_KEY.equals(property)
            && variableScope instanceof ExecutionEntity
            && ((ExecutionEntity) variableScope).getActivity() != null
            && ((ExecutionEntity) variableScope).getActivity().getActivityBehavior() instanceof ExternalTaskActivityBehavior) {
          List<ExternalTaskEntity> externalTasks = ((ExecutionEntity) variableScope).getExternalTasks();
          if(externalTasks.size() != 1) {
            throw new ProcessEngineException("Could not resolve expression to single external task entity.");
          }
          context.setPropertyResolved(true);
          return externalTasks.get(0);

        } else if (EXECUTION_KEY.equals(property) && variableScope instanceof TaskEntity) {
          context.setPropertyResolved(true);
          return ((TaskEntity) variableScope).getExecution();
        } else if(LOGGED_IN_USER_KEY.equals(property)){
          context.setPropertyResolved(true);
          return Context.getCommandContext().getAuthenticatedUserId();
        } else {
          if (variableScope.hasVariable(variable)) {
            context.setPropertyResolved(true); // if not set, the next elResolver in the CompositeElResolver will be called
            return variableScope.getVariable(variable);
          }
        }
      }
    }

    // property resolution (eg. bean.value) will be done by the BeanElResolver (part of the CompositeElResolver)
    // It will use the bean resolved in this resolver as base.

    return null;
  }

  public boolean isReadOnly(ELContext context, Object base, Object property) {
    if (base == null) {
      String variable = (String) property;
      Object object = context.getContext(VariableScope.class);
      return object != null && !((VariableScope)object).hasVariable(variable);
    }
    return true;
  }

  public void setValue(ELContext context, Object base, Object property, Object value) {
    if (base == null) {
      String variable = (String) property;
      Object object = context.getContext(VariableScope.class);
      if (object != null) {
        VariableScope variableScope = (VariableScope) object;
        if (variableScope.hasVariable(variable)) {
          variableScope.setVariable(variable, value);
          context.setPropertyResolved(true);
        }
      }
    }
  }

  public Class< ? > getCommonPropertyType(ELContext arg0, Object arg1) {
    return Object.class;
  }

  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext arg0, Object arg1) {
    return null;
  }

  public Class< ? > getType(ELContext arg0, Object arg1, Object arg2) {
    return Object.class;
  }

}
