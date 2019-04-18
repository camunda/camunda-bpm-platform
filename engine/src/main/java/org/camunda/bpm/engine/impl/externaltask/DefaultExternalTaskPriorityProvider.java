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
package org.camunda.bpm.engine.impl.externaltask;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.DefaultPriorityProvider;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.behavior.ExternalTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * Represents the default priority provider for external tasks.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class DefaultExternalTaskPriorityProvider extends DefaultPriorityProvider<ExternalTaskActivityBehavior> {

  public static final ExternalTaskLogger LOG = ProcessEngineLogger.EXTERNAL_TASK_LOGGER;

  @Override
  protected void logNotDeterminingPriority(ExecutionEntity execution, Object value, ProcessEngineException e) {
    LOG.couldNotDeterminePriority(execution, value, e);
  }

  @Override
  public Long getSpecificPriority(ExecutionEntity execution, ExternalTaskActivityBehavior param, String jobDefinitionId) {
    ParameterValueProvider priorityProvider = param.getPriorityValueProvider();
    if (priorityProvider != null) {
      return evaluateValueProvider(priorityProvider, execution, "");
    }
    return null;
  }

  @Override
  protected Long getProcessDefinitionPriority(ExecutionEntity execution, ExternalTaskActivityBehavior param) {
    return getProcessDefinedPriority(execution.getProcessDefinition(), BpmnParse.PROPERTYNAME_TASK_PRIORITY, execution, "");
  }
}
