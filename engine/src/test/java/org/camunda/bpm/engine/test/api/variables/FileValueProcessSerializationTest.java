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
package org.camunda.bpm.engine.test.api.variables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.charset.Charset;
import java.util.Scanner;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;

/**
 * @author Ronny Bräunlich
 *
 */
public class FileValueProcessSerializationTest extends PluggableProcessEngineTest {

  protected static final String ONE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/variables/oneTaskProcess.bpmn20.xml";

  @Test
  public void testSerializeFileVariable() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("process").startEvent().userTask().endEvent().done();
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeployment().addModelInstance("process.bpmn", modelInstance).deploy();
    VariableMap variables = Variables.createVariables();
    String filename = "test.txt";
    String type = "text/plain";
    FileValue fileValue = Variables.fileValue(filename).file("ABC".getBytes()).encoding("UTF-8").mimeType(type).create();
    variables.put("file", fileValue);
    runtimeService.startProcessInstanceByKey("process", variables);
    Task task = taskService.createTaskQuery().singleResult();
    VariableInstance result = runtimeService.createVariableInstanceQuery().processInstanceIdIn(task.getProcessInstanceId()).singleResult();
    FileValue value = (FileValue) result.getTypedValue();

    assertThat(value.getFilename()).isEqualTo(filename);
    assertThat(value.getMimeType()).isEqualTo(type);
    assertThat(value.getEncoding()).isEqualTo("UTF-8");
    assertThat(value.getEncodingAsCharset()).isEqualTo(Charset.forName("UTF-8"));
    try (Scanner scanner = new Scanner(value.getValue())) {
      assertThat(scanner.nextLine()).isEqualTo("ABC");
    }

    // clean up
    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Test
  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSerializeNullMimeType() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("fileVar", Variables.fileValue("test.txt").file("ABC".getBytes()).encoding("UTF-8").create()));

    FileValue fileVar = runtimeService.getVariableTyped(pi.getId(), "fileVar");
    assertNull(fileVar.getMimeType());
  }

  @Test
  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSerializeNullMimeTypeAndNullEncoding() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("fileVar", Variables.fileValue("test.txt").file("ABC".getBytes()).create()));

    FileValue fileVar = runtimeService.getVariableTyped(pi.getId(), "fileVar");
    assertNull(fileVar.getMimeType());
    assertNull(fileVar.getEncoding());
  }

  @Test
  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSerializeNullEncoding() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("fileVar", Variables.fileValue("test.txt").mimeType("some mimetype").file("ABC".getBytes()).create()));

    FileValue fileVar = runtimeService.getVariableTyped(pi.getId(), "fileVar");
    assertNull(fileVar.getEncoding());
  }

  @Test
  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSerializeNullValue() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("fileVar", Variables.fileValue("test.txt").create()));

    FileValue fileVar = runtimeService.getVariableTyped(pi.getId(), "fileVar");
    assertNull(fileVar.getMimeType());
  }

  @Test
  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSerializeEmptyFileName() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("fileVar", Variables.fileValue("").create()));

    FileValue fileVar = runtimeService.getVariableTyped(pi.getId(), "fileVar");
    assertEquals("", fileVar.getFilename());
  }

}
