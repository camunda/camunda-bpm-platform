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
package org.camunda.bpm.qa.upgrade.timestamp;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.util.Date;

/**
 * @author Nikola Koevski
 */
public abstract class AbstractTimestampMigrationScenario {

  protected static final long TIME = 1548082136000L;
  protected static final Date TIMESTAMP = new Date(TIME);

  protected static void deployModel(ProcessEngine processEngine, String deploymentName, String resourceName, BpmnModelInstance modelInstance) {
    processEngine.getRepositoryService()
      .createDeployment()
      .name(deploymentName)
      .addModelInstance(resourceName + ".bpmn20.xml", modelInstance)
      .deploy();
  }
}
