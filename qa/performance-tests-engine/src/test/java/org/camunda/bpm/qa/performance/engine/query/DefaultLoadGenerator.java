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
package org.camunda.bpm.qa.performance.engine.query;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.qa.performance.engine.junit.PerfTestProcessEngine;
import org.camunda.bpm.qa.performance.engine.loadgenerator.LoadGenerator;
import org.camunda.bpm.qa.performance.engine.loadgenerator.LoadGeneratorConfiguration;
import org.camunda.bpm.qa.performance.engine.loadgenerator.tasks.DeployModelInstancesTask;
import org.camunda.bpm.qa.performance.engine.loadgenerator.tasks.GenerateMetricsTask;
import org.camunda.bpm.qa.performance.engine.loadgenerator.tasks.StartProcessInstanceTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Daniel Meyer
 *
 */
public class DefaultLoadGenerator {

  /**
   * The reported ID for the metrics.
   */
  protected static final String REPORTER_ID = "REPORTER_ID";
  protected static final int NUMBER_OF_PROCESSES = 999;

  public static void main(String[] args) throws InterruptedException {

    final Properties properties = PerfTestProcessEngine.loadProperties();
    final ProcessEngine processEngine = PerfTestProcessEngine.getInstance();

    final LoadGeneratorConfiguration config = new LoadGeneratorConfiguration();
    config.setColor(Boolean.parseBoolean(properties.getProperty("loadGenerator.colorOutput", "false")));
    config.setNumberOfIterations(Integer.parseInt(properties.getProperty("loadGenerator.numberOfIterations", "10000")));

    final List<BpmnModelInstance> modelInstances = createProcesses(config.getNumberOfIterations());

    Runnable[] setupTasks = new Runnable[] {
        new DeployModelInstancesTask(processEngine, modelInstances)
    };
    config.setSetupTasks(setupTasks);

    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    processEngineConfiguration.setMetricsEnabled(true);
    processEngineConfiguration.getDbMetricsReporter().setReporterId(REPORTER_ID);
    final Runnable[] workerRunnables = new Runnable[2];
    Process process = modelInstances.get(0).getModelElementsByType(Process.class).iterator().next();
    String processDefKey = process.getId();
    workerRunnables[0] = new StartProcessInstanceTask(processEngine, processDefKey);
    workerRunnables[1] = new GenerateMetricsTask(processEngine);
    config.setWorkerTasks(workerRunnables);

    new LoadGenerator(config).execute();

    System.out.println(processEngine.getHistoryService().createHistoricProcessInstanceQuery().count()+ " Process Instances in DB");
    processEngineConfiguration.setMetricsEnabled(false);
  }

  static List<BpmnModelInstance> createProcesses(int numberOfProcesses) {

    List<BpmnModelInstance> result = new ArrayList<BpmnModelInstance>(numberOfProcesses);

    System.out.println("Number of Processes: " + numberOfProcesses);
    for(int i=0; i<NUMBER_OF_PROCESSES; i++) {
      result.add(createProcess(i));
    }
    return result;
  }

  protected static BpmnModelInstance createProcess(int id){
    return Bpmn.createExecutableProcess("process" + id)
        .camundaHistoryTimeToLive(180)
        .startEvent()
        .userTask()
        .camundaAssignee("demo")
        .endEvent()
        .done();
  }

}
