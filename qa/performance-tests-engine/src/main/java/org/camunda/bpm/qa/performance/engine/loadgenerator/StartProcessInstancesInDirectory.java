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
package org.camunda.bpm.qa.performance.engine.loadgenerator;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.qa.performance.engine.junit.PerfTestProcessEngine;
import org.camunda.bpm.qa.performance.engine.loadgenerator.tasks.DeployFileTask;
import org.camunda.bpm.qa.performance.engine.loadgenerator.tasks.StartProcessInstanceTask;

/**
 * @author Daniel Meyer
 *
 */
public class StartProcessInstancesInDirectory {

  public final static String[] DEPLOYABLE_FILE_EXTENSIONS = new String[] {
      ".bpmn",
      ".bpmn20.xml"
  };

  public static void main(String[] args) throws InterruptedException {

    ProcessEngine processEngine = PerfTestProcessEngine.getInstance();

    final LoadGeneratorConfiguration config = new LoadGeneratorConfiguration();

    final List<String> deployableFiles = findDeployableFiles(new File("."));
    final Runnable[] setupTasks = new Runnable[deployableFiles.size()];
    for (int i = 0; i < deployableFiles.size(); i++) {
      setupTasks[i] = new DeployFileTask(processEngine, deployableFiles.get(i));
    }
    config.setSetupTasks(setupTasks);


    final List<String> processKeys = extractProcessDefinitionKeys(deployableFiles);
    final Runnable[] workerRunnables = new Runnable[processKeys.size()];
    for (int i = 0; i < processKeys.size(); i++) {
      workerRunnables[i] = new StartProcessInstanceTask(processEngine, processKeys.get(i));
    }
    config.setWorkerTasks(workerRunnables);


    final LoadGenerator loadGenerator = new LoadGenerator(config);
    loadGenerator.execute();

  }

  private static List<String> extractProcessDefinitionKeys(List<String> deployableFileNames) {
    ArrayList<String> keys = new ArrayList<String>();
    for (String file : deployableFileNames) {
      if(file.endsWith(".bpmn") || file.endsWith(".bpmn20.xml")) {
        BpmnModelInstance modelInstance = Bpmn.readModelFromFile(new File(file));
        Collection<Process> processes = modelInstance.getModelElementsByType(Process.class);
        for (Process process : processes) {
          if(process.isExecutable()) {
            keys.add(process.getId());
          }
        }
      }
    }
    return keys;
  }

  private static List<String> findDeployableFiles(File dir) {

    final List<String> result = new ArrayList<String>();

    String[] localNames = dir.list(new FilenameFilter() {

      public boolean accept(File dir, String name) {
        for (String extension : DEPLOYABLE_FILE_EXTENSIONS) {
          if(name.endsWith(extension)) {
            return true;
          }
        }
        return false;
      }

    });

    if(localNames != null) {
      for (String file : localNames) {
        result.add(dir.getAbsolutePath() + File.separator + file);
      }
    }

    return result;
  }

}
