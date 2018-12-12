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
package org.camunda.bpm.engine.impl.cmd;


import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.ConditionEvaluationBuilderImpl;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.runtime.ConditionHandler;
import org.camunda.bpm.engine.impl.runtime.ConditionHandlerResult;
import org.camunda.bpm.engine.impl.runtime.ConditionSet;
import org.camunda.bpm.engine.runtime.ProcessInstance;

/**
 * Evaluates the conditions to start processes by conditional start events
 *
 * @author Yana Vasileva
 *
 */
public class EvaluateStartConditionCmd implements Command<List<ProcessInstance>> {

  protected ConditionEvaluationBuilderImpl builder;

  public EvaluateStartConditionCmd(ConditionEvaluationBuilderImpl builder) {
    this.builder = builder;
  }

  @Override
  public List<ProcessInstance> execute(final CommandContext commandContext) {
    final ConditionHandler conditionHandler = commandContext.getProcessEngineConfiguration().getConditionHandler();
    final ConditionSet conditionSet = new ConditionSet(builder);

    List<ConditionHandlerResult> results = conditionHandler.evaluateStartCondition(commandContext, conditionSet);

    for (ConditionHandlerResult ConditionHandlerResult : results) {
      checkAuthorization(commandContext, ConditionHandlerResult);
    }

    List<ProcessInstance> processInstances = new ArrayList<ProcessInstance>();
    for (ConditionHandlerResult ConditionHandlerResult : results) {
      processInstances.add(instantiateProcess(commandContext, ConditionHandlerResult));
    }

    return processInstances;
  }

  protected void checkAuthorization(CommandContext commandContext, ConditionHandlerResult result) {
    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      ProcessDefinitionEntity definition = result.getProcessDefinition();
      checker.checkCreateProcessInstance(definition);
    }
  }

  protected ProcessInstance instantiateProcess(CommandContext commandContext, ConditionHandlerResult result) {
    ProcessDefinitionEntity processDefinitionEntity = result.getProcessDefinition();

    ActivityImpl startEvent = processDefinitionEntity.findActivity(result.getActivity().getActivityId());
    ExecutionEntity processInstance = processDefinitionEntity.createProcessInstance(builder.getBusinessKey(), startEvent);
    processInstance.start(builder.getVariables());

    return processInstance;
  }

}
