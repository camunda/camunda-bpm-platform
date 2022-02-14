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
package org.camunda.bpm.engine.impl.cmmn.behavior;

import static org.camunda.bpm.engine.impl.util.CallableElementUtil.getCaseDefinitionToCall;

import java.util.Map;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnCaseInstance;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;

/**
 * @author Roman Smirnov
 *
 */
public class CaseTaskActivityBehavior extends ProcessOrCaseTaskActivityBehavior {

  protected static final CmmnBehaviorLogger LOG = ProcessEngineLogger.CMNN_BEHAVIOR_LOGGER;

  protected void triggerCallableElement(CmmnActivityExecution execution, Map<String, Object> variables, String businessKey) {
    CaseExecutionEntity executionEntity = (CaseExecutionEntity) execution;

    CmmnCaseDefinition definition = getCaseDefinitionToCall(executionEntity, executionEntity.getCaseDefinitionTenantId(), getCallableElement());
    CmmnCaseInstance caseInstance = execution.createSubCaseInstance(definition, businessKey);
    caseInstance.create(variables);
  }

  protected String getTypeName() {
    return "case task";
  }

}
