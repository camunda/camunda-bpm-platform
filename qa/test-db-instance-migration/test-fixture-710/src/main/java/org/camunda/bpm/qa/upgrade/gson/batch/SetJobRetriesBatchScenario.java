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
package org.camunda.bpm.qa.upgrade.gson.batch;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

/**
 * @author Tassilo Weidner
 */
public class SetJobRetriesBatchScenario {

  @Deployment
  public static String deploy() {
    return "org/camunda/bpm/qa/upgrade/gson/oneTaskProcessAsync.bpmn20.xml";
  }

  @DescribesScenario("initSetJobRetriesBatch")
  public static ScenarioSetup initSetJobRetriesBatch() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        for (int i = 0; i < 10; i++) {
          engine.getRuntimeService().startProcessInstanceByKey("oneTaskProcessAsync_710");
        }

        List<String> jobIds = new ArrayList<>();

        List<Job> jobs = engine.getManagementService().createJobQuery()
          .processDefinitionKey("oneTaskProcessAsync_710")
          .list();

        for (Job job : jobs) {
          jobIds.add(job.getId());
        }

        Batch batch = engine.getManagementService().setJobRetriesAsync(jobIds, 22);
        engine.getManagementService().setProperty("SetJobRetriesBatchScenario.retries.batchId", batch.getId());
      }
    };
  }
}
