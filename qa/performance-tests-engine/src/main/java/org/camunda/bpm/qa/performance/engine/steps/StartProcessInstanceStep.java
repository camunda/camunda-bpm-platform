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
package org.camunda.bpm.qa.performance.engine.steps;

import static org.camunda.bpm.qa.performance.engine.steps.PerfTestConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestRunContext;

/**
 * @author Daniel Meyer
 *
 */
public class StartProcessInstanceStep extends ProcessEngineAwareStep {

  protected String processDefinitionKey;
  protected Map<String, Object> processVariables;

  public StartProcessInstanceStep(ProcessEngine processEngine, String processDefinitionKey) {
    this(processEngine, processDefinitionKey, null);
  }

  public StartProcessInstanceStep(ProcessEngine processEngine, String processDefinitionKey, Map<String, Object> processVariables) {
    super(processEngine);
    this.processDefinitionKey = processDefinitionKey;
    this.processVariables = processVariables;
  }

  @Override
  public void execute(PerfTestRunContext context) {
    Map<String, Object> variables = new HashMap<String, Object>();
    if (processVariables != null) {
      variables.putAll(processVariables);
    }
    // unique run id as variable
    variables.put(RUN_ID, context.getVariable(RUN_ID));

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, variables);
    context.setVariable(PROCESS_INSTANCE_ID, processInstance.getId());
  }

}
