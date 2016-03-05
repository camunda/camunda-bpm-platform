/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.qa.performance.engine.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.qa.performance.engine.junit.PerfTestProcessEngine;
import org.camunda.bpm.qa.performance.engine.loadgenerator.LoadGenerator;
import org.camunda.bpm.qa.performance.engine.loadgenerator.LoadGeneratorConfiguration;
import org.camunda.bpm.qa.performance.engine.loadgenerator.tasks.DeployModelInstancesTask;
import org.camunda.bpm.qa.performance.engine.loadgenerator.tasks.StartProcessInstanceTask;

/**
 * @author Daniel Meyer
 *
 */
public class DefaultLoadGenerator {

  public static void main(String[] args) throws InterruptedException {

    final Properties properties = PerfTestProcessEngine.loadProperties();
    final ProcessEngine processEngine = PerfTestProcessEngine.getInstance();

    final LoadGeneratorConfiguration config = new LoadGeneratorConfiguration();
    config.setColor(Boolean.parseBoolean(properties.getProperty("loadGenerator.colorOutput", "false")));
    config.setNumberOfIterations(Integer.parseInt(properties.getProperty("loadGenerator.numberOfIterations", "10000")));

    final List<BpmnModelInstance> modelInstances = createProcesses();

    Runnable[] setupTasks = new Runnable[] {
        new DeployModelInstancesTask(processEngine, modelInstances)
    };
    config.setSetupTasks(setupTasks);

    // auto start all bpmn processes
    List<String> keys = new ArrayList<String>();
    for (BpmnModelInstance instance : modelInstances) {
      Collection<Process> processes = instance.getModelElementsByType(Process.class);
      for (Process process : processes) {
        if(process.isExecutable()) {
          keys.add(process.getId());
        }
      }
    }

    final Runnable[] workerRunnables = new Runnable[keys.size()];
    for (int i = 0; i < workerRunnables.length; i++) {
      workerRunnables[i] = new StartProcessInstanceTask(processEngine, keys.get(i));
    }
    config.setWorkerTasks(workerRunnables);

    new LoadGenerator(config).execute();

    System.out.println(processEngine.getHistoryService().createHistoricProcessInstanceQuery().count()+ " Process Instances in DB");
  }

  static List<BpmnModelInstance> createProcesses() {

    List<BpmnModelInstance> result = new ArrayList<BpmnModelInstance>();

    result.add(Bpmn.createExecutableProcess("process1").startEvent().userTask().camundaAssignee("demo").endEvent().done());

    return result;
  }

}
