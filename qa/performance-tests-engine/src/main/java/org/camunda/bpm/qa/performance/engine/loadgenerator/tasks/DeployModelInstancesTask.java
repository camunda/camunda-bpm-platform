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
package org.camunda.bpm.qa.performance.engine.loadgenerator.tasks;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * @author Daniel Meyer
 *
 */
public class DeployModelInstancesTask implements Runnable {

  protected final List<BpmnModelInstance> modelInstances;
  protected final ProcessEngine engine;

  public DeployModelInstancesTask(ProcessEngine engine, List<BpmnModelInstance> modelInstances) {
    this.engine = engine;
    this.modelInstances = modelInstances;
  }

  public void run() {

    DeploymentBuilder deploymentbuilder = engine.getRepositoryService().createDeployment();

    for(int i = 0; i < modelInstances.size(); i++) {
      deploymentbuilder.addModelInstance("process"+i+".bpmn", modelInstances.get(i));
    }

    deploymentbuilder.deploy();
  }

}
