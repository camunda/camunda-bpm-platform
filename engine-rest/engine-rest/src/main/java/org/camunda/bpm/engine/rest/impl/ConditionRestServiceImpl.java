/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.ConditionRestService;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.condition.EvaluationConditionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.runtime.ConditionEvaluationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ConditionRestServiceImpl extends AbstractRestProcessEngineAware implements ConditionRestService {

  public ConditionRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public List<ProcessInstanceDto> evaluateCondition(EvaluationConditionDto conditionDto) {
    if (conditionDto.getTenantId() != null && conditionDto.isWithoutTenantId()) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Parameter 'tenantId' cannot be used together with parameter 'withoutTenantId'.");
    }
    ConditionEvaluationBuilder builder = createConditionEvaluationBuilder(conditionDto);
    List<ProcessInstance> processInstances = builder.evaluateStartConditions();

    List<ProcessInstanceDto> result = new ArrayList<ProcessInstanceDto>();
    for (ProcessInstance processInstance : processInstances) {
      result.add(ProcessInstanceDto.fromProcessInstance(processInstance));
    }
    return result;
  }

  protected ConditionEvaluationBuilder createConditionEvaluationBuilder(EvaluationConditionDto conditionDto) {
    RuntimeService runtimeService = processEngine.getRuntimeService();

    ObjectMapper objectMapper = getObjectMapper();

    VariableMap variables = VariableValueDto.toMap(conditionDto.getVariables(), processEngine, objectMapper);

    ConditionEvaluationBuilder builder = runtimeService.createConditionEvaluation();

    if (variables != null && !variables.isEmpty()) {
      builder.setVariables(variables);
    }

    if (conditionDto.getBusinessKey() != null) {
      builder.processInstanceBusinessKey(conditionDto.getBusinessKey());
    }

    if (conditionDto.getProcessDefinitionId() != null) {
      builder.processDefinitionId(conditionDto.getProcessDefinitionId());
    }

    if (conditionDto.getTenantId() != null) {
      builder.tenantId(conditionDto.getTenantId());
    } else if (conditionDto.isWithoutTenantId()) {
      builder.withoutTenantId();
    }

    return builder;
  }

}
