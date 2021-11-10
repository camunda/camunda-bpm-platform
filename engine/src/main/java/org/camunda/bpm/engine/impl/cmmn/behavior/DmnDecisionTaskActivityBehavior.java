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

import static org.camunda.bpm.engine.impl.util.DecisionEvaluationUtil.evaluateDecision;

import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.dmn.result.DecisionResultMapper;

/**
 * @author Roman Smirnov
 *
 */
public class DmnDecisionTaskActivityBehavior extends DecisionTaskActivityBehavior {

  protected DecisionResultMapper decisionResultMapper;

  @Override
  protected void performStart(CmmnActivityExecution execution) {
    try {
      CaseExecutionEntity executionEntity = (CaseExecutionEntity) execution;

      evaluateDecision(executionEntity,
          executionEntity.getCaseDefinitionTenantId(),
          callableElement,
          resultVariable,
          decisionResultMapper);

      if (execution.isActive()) {
        execution.complete();
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw LOG.decisionDefinitionEvaluationFailed(execution, e);
    }
  }

  public DecisionResultMapper getDecisionTableResultMapper() {
    return decisionResultMapper;
  }

  public void setDecisionTableResultMapper(DecisionResultMapper decisionResultMapper) {
    this.decisionResultMapper = decisionResultMapper;
  }

}
