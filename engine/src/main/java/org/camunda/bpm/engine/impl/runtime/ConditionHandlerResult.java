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
package org.camunda.bpm.engine.impl.runtime;

import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

public class ConditionHandlerResult {
  private ProcessDefinitionEntity processDefinition;
  private ActivityImpl activity;

  public ConditionHandlerResult(ProcessDefinitionEntity processDefinition, ActivityImpl activity) {
    this.setProcessDefinition(processDefinition);
    this.setActivity(activity);
  }

  public ProcessDefinitionEntity getProcessDefinition() {
    return processDefinition;
  }

  public void setProcessDefinition(ProcessDefinitionEntity processDefinition) {
    this.processDefinition = processDefinition;
  }

  public ActivityImpl getActivity() {
    return activity;
  }

  public void setActivity(ActivityImpl activity) {
    this.activity = activity;
  }

  public static ConditionHandlerResult matchedProcessDefinition(ProcessDefinitionEntity processDefinition, ActivityImpl startActivityId) {
    ConditionHandlerResult conditionHandlerResult = new ConditionHandlerResult(processDefinition, startActivityId);
    return conditionHandlerResult;
  }
}
