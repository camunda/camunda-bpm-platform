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
package org.camunda.bpm.engine.test.api.runtime.migration.models.builder;

import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class DefaultExternalTaskModelBuilder {

  public static final String DEFAULT_PROCESS_KEY = "Process";
  public static final String DEFAULT_EXTERNAL_TASK_NAME = "externalTask";
  public static final String DEFAULT_EXTERNAL_TASK_TYPE = "external";
  public static final String DEFAULT_TOPIC = "foo";
  public static final Integer DEFAULT_PRIORITY = 1;

  protected String processKey = DEFAULT_PROCESS_KEY;
  protected String externalTaskName = DEFAULT_EXTERNAL_TASK_NAME;
  protected String externalTaskType = DEFAULT_EXTERNAL_TASK_TYPE;
  protected String topic = DEFAULT_TOPIC;
  protected Integer priority = DEFAULT_PRIORITY;

  public static DefaultExternalTaskModelBuilder createDefaultExternalTaskModel() {
    return new DefaultExternalTaskModelBuilder();
  }

  public DefaultExternalTaskModelBuilder processKey(String processKey) {
    this.processKey = processKey;
    return this;
  }

  public DefaultExternalTaskModelBuilder externalTaskName(String externalTaskName) {
    this.externalTaskName = externalTaskName;
    return this;
  }

  public DefaultExternalTaskModelBuilder externalTaskType(String externalTaskType) {
    this.externalTaskType = externalTaskType;
    return this;
  }

  public DefaultExternalTaskModelBuilder topic(String topic){
    this.topic = topic;
    return this;
  }

  public DefaultExternalTaskModelBuilder priority(Integer priority) {
    this.priority = priority;
    return this;
  }

  public BpmnModelInstance build() {
    return ProcessModels.newModel(processKey)
      .startEvent()
      .serviceTask(externalTaskName)
      .camundaType(externalTaskType)
      .camundaTopic(topic)
      .camundaTaskPriority(priority.toString())
      .endEvent()
      .done();
  }

}
