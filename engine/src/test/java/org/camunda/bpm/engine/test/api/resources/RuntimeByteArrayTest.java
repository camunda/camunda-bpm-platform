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

package org.camunda.bpm.engine.test.api.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.repository.ResourceTypes;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class RuntimeByteArrayTest {
  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl configuration;
  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected TaskService taskService;

  protected String id;

  @Before
  public void initServices() {
    configuration = engineRule.getProcessEngineConfiguration();
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    taskService = engineRule.getTaskService();
  }

  @After
  public void tearDown() {
    if (id != null) {
      // delete task
      taskService.deleteTask(id, true);
    }
  }

  @Test
  public void testBinaryForFileValues() {
    // given
    BpmnModelInstance instance = createProcess();

    testRule.deploy(instance);
    FileValue fileValue = createFile();

    runtimeService.startProcessInstanceByKey("Process", Variables.createVariables().putValueTyped("fileVar", fileValue));

    String byteArrayValueId = ((VariableInstanceEntity)runtimeService.createVariableInstanceQuery().singleResult()).getByteArrayValueId();

    // when
    ByteArrayEntity byteArrayEntity = configuration.getCommandExecutorTxRequired()
        .execute(new GetByteArrayCommand(byteArrayValueId));

    // then
    assertNotNull(byteArrayEntity);
    assertNotNull(byteArrayEntity.getCreateTime());
    assertEquals(ResourceTypes.RUNTIME.getValue(), byteArrayEntity.getType());
  }

  @Test
  public void testDisableBinaryFetching() {
    byte[] binaryContent = "some binary content".getBytes();

    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("binaryVariable", binaryContent);
    Task task = taskService.newTask();
    taskService.saveTask(task);
    id = task.getId();
    taskService.setVariablesLocal(id, variables);

    String byteArrayValueId = ((VariableInstanceEntity)runtimeService.createVariableInstanceQuery().singleResult()).getByteArrayValueId();

    // when
    ByteArrayEntity byteArrayEntity = configuration.getCommandExecutorTxRequired()
        .execute(new GetByteArrayCommand(byteArrayValueId));

    // then
    assertNotNull(byteArrayEntity);
    assertNotNull(byteArrayEntity.getCreateTime());
    assertEquals(ResourceTypes.RUNTIME.getValue(), byteArrayEntity.getType());
  }

  protected FileValue createFile() {
    String fileName = "text.txt";
    String encoding = "crazy-encoding";
    String mimeType = "martini/dry";

    FileValue fileValue = Variables
        .fileValue(fileName)
        .file("ABC".getBytes())
        .encoding(encoding)
        .mimeType(mimeType)
        .create();
    return fileValue;
  }

  protected BpmnModelInstance createProcess() {
    return Bpmn.createExecutableProcess("Process")
      .startEvent()
      .userTask("user")
      .endEvent()
      .done();
  }

  public class GetByteArrayCommand implements Command<ByteArrayEntity> {

    protected String byteArrayId;

    public GetByteArrayCommand(String byteArrayId) {
      this.byteArrayId = byteArrayId;
    }

    public ByteArrayEntity execute(CommandContext commandContext) {
      return (ByteArrayEntity) commandContext.getDbEntityManager()
        .selectOne("selectByteArray", byteArrayId);
    }

  }
}
