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
package org.camunda.bpm.engine.test.api.authorization;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_TASK;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_TASK;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.TASK;

import java.io.InputStream;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * @author Roman Smirnov
 *
 */
public class FormAuthorizationTest extends AuthorizationTest {

  protected static final String FORM_PROCESS_KEY = "FormsProcess";
  protected static final String RENDERED_FORM_PROCESS_KEY = "renderedFormProcess";
  protected static final String CASE_KEY = "oneTaskCase";

  protected String deploymentId;

  public void setUp() throws Exception {
    deploymentId = createDeployment(null,
        "org/camunda/bpm/engine/test/api/form/DeployedFormsProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/form/start.form",
        "org/camunda/bpm/engine/test/api/form/task.form",
        "org/camunda/bpm/engine/test/api/authorization/renderedFormProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/oneTaskCase.cmmn").getId();
    super.setUp();
  }

  public void tearDown() {
    super.tearDown();
    deleteDeployment(deploymentId);
  }

  // get start form data ///////////////////////////////////////////

  public void testGetStartFormDataWithoutAuthorizations() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(FORM_PROCESS_KEY).getId();

    try {
      // when
      formService.getStartFormData(processDefinitionId);
      fail("Exception expected: It should not be possible to get start form data");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(FORM_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testGetStartFormData() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(FORM_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, FORM_PROCESS_KEY, userId, READ);

    // when
    StartFormData startFormData = formService.getStartFormData(processDefinitionId);

    // then
    assertNotNull(startFormData);
    assertEquals("deployment:org/camunda/bpm/engine/test/api/form/start.form", startFormData.getFormKey());
  }

  // get rendered start form /////////////////////////////////////

  public void testGetRenderedStartFormWithoutAuthorization() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(RENDERED_FORM_PROCESS_KEY).getId();

    try {
      // when
      formService.getRenderedStartForm(processDefinitionId);
      fail("Exception expected: It should not be possible to get start form data");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(RENDERED_FORM_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testGetRenderedStartForm() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(RENDERED_FORM_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, RENDERED_FORM_PROCESS_KEY, userId, READ);

    // when
    Object renderedStartForm = formService.getRenderedStartForm(processDefinitionId);

    // then
    assertNotNull(renderedStartForm);
  }

  // get start form variables //////////////////////////////////

  public void testGetStartFormVariablesWithoutAuthorization() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(RENDERED_FORM_PROCESS_KEY).getId();

    try {
      // when
      formService.getStartFormVariables(processDefinitionId);
      fail("Exception expected: It should not be possible to get start form data");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(RENDERED_FORM_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testGetStartFormVariables() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(RENDERED_FORM_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, RENDERED_FORM_PROCESS_KEY, userId, READ);

    // when
    VariableMap variables = formService.getStartFormVariables(processDefinitionId);

    // then
    assertNotNull(variables);
    assertEquals(1, variables.size());
  }

  // submit start form /////////////////////////////////////////

  public void testSubmitStartFormWithoutAuthorization() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(FORM_PROCESS_KEY).getId();

    try {
      // when
      formService.submitStartForm(processDefinitionId, null);
      fail("Exception expected: It should not possible to submit a start form");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(CREATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
    }
  }

  public void testSubmitStartFormWithCreatePermissionOnProcessInstance() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(FORM_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    try {
      // when
      formService.submitStartForm(processDefinitionId, null);
      fail("Exception expected: It should not possible to submit a start form");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(CREATE_INSTANCE.getName(), message);
      assertTextPresent(FORM_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSubmitStartFormWithCreateInstancePermissionOnProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(FORM_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, FORM_PROCESS_KEY, userId, CREATE_INSTANCE);

    try {
      // when
      formService.submitStartForm(processDefinitionId, null);
      fail("Exception expected: It should not possible to submit a start form");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(CREATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
    }
  }

  public void testSubmitStartForm() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(FORM_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, FORM_PROCESS_KEY, userId, CREATE_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    // when
    ProcessInstance instance = formService.submitStartForm(processDefinitionId, null);

    // then
    assertNotNull(instance);
  }

  // get task form data (standalone task) /////////////////////////////////

  public void testStandaloneTaskGetTaskFormDataWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    try {
      // when
      formService.getTaskFormData(taskId);
      fail("Exception expected: It should not possible to get task form data");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
    }

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskGetTaskFormData() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, userId, READ);

    // when
    TaskFormData taskFormData = formService.getTaskFormData(taskId);

    // then
    // Standalone task, no TaskFormData available
    assertNull(taskFormData);

    deleteTask(taskId, true);
  }

  // get task form data (process task) /////////////////////////////////

  public void testProcessTaskGetTaskFormDataWithoutAuthorization() {
    // given
    startProcessInstanceByKey(FORM_PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    try {
      // when
      formService.getTaskFormData(taskId);
      fail("Exception expected: It should not possible to get task form data");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(READ_TASK.getName(), message);
      assertTextPresent(FORM_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testProcessTaskGetTaskFormDataWithReadPermissionOnTask() {
    // given
    startProcessInstanceByKey(FORM_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, READ);

    // when
    TaskFormData taskFormData = formService.getTaskFormData(taskId);

    // then
    assertNotNull(taskFormData);
  }

  public void testProcessTaskGetTaskFormDataWithReadTaskPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(FORM_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(PROCESS_DEFINITION, FORM_PROCESS_KEY, userId, READ_TASK);

    // when
    TaskFormData taskFormData = formService.getTaskFormData(taskId);

    // then
    assertNotNull(taskFormData);
  }

  public void testProcessTaskGetTaskFormData() {
    // given
    startProcessInstanceByKey(FORM_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, READ);
    createGrantAuthorization(PROCESS_DEFINITION, FORM_PROCESS_KEY, userId, READ_TASK);

    // when
    TaskFormData taskFormData = formService.getTaskFormData(taskId);

    // then
    assertNotNull(taskFormData);
  }

  // get task form data (case task) /////////////////////////////////

  public void testCaseTaskGetTaskFormData() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();

    // when
    TaskFormData taskFormData = formService.getTaskFormData(taskId);

    // then
    assertNotNull(taskFormData);
  }

  // get rendered task form (standalone task) //////////////////

  public void testStandaloneTaskGetTaskRenderedFormWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    try {
      // when
      formService.getRenderedTaskForm(taskId);
      fail("Exception expected: It should not possible to get rendered task form");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
    }

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskGetTaskRenderedForm() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, userId, READ);

    try {
      // when
      // Standalone task, no TaskFormData available
      formService.getRenderedTaskForm(taskId);
    } catch (NullValueException e) {}

    deleteTask(taskId, true);
  }

  // get rendered task form (process task) /////////////////////////////////

  public void testProcessTaskGetRenderedTaskFormWithoutAuthorization() {
    // given
    startProcessInstanceByKey(RENDERED_FORM_PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    try {
      // when
      formService.getRenderedTaskForm(taskId);
      fail("Exception expected: It should not possible to get rendered task form");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(READ_TASK.getName(), message);
      assertTextPresent(RENDERED_FORM_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testProcessTaskGetRenderedTaskFormWithReadPermissionOnTask() {
    // given
    startProcessInstanceByKey(RENDERED_FORM_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, READ);

    // when
    Object taskForm = formService.getRenderedTaskForm(taskId);

    // then
    assertNotNull(taskForm);
  }

  public void testProcessTaskGetRenderedTaskFormWithReadTaskPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(RENDERED_FORM_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(PROCESS_DEFINITION, RENDERED_FORM_PROCESS_KEY, userId, READ_TASK);

    // when
    Object taskForm = formService.getRenderedTaskForm(taskId);

    // then
    assertNotNull(taskForm);
  }

  public void testProcessTaskGetRenderedTaskForm() {
    // given
    startProcessInstanceByKey(RENDERED_FORM_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, READ);
    createGrantAuthorization(PROCESS_DEFINITION, RENDERED_FORM_PROCESS_KEY, userId, READ_TASK);

    // when
    Object taskForm = formService.getRenderedTaskForm(taskId);

    // then
    assertNotNull(taskForm);
  }

  // get rendered task form (case task) /////////////////////////////////

  public void testCaseTaskGetRenderedTaskForm() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();

    // when
    Object taskForm = formService.getRenderedTaskForm(taskId);

    // then
    assertNull(taskForm);
  }

  // get task form variables (standalone task) ////////////////////////

  public void testStandaloneTaskGetTaskFormVariablesWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    try {
      // when
      formService.getTaskFormVariables(taskId);
      fail("Exception expected: It should not possible to get task form variables");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
    }

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskGetTaskFormVariables() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, userId, READ);

    // when
    VariableMap variables = formService.getTaskFormVariables(taskId);

    // then
    assertNotNull(variables);

    deleteTask(taskId, true);
  }

  // get task form variables (process task) /////////////////////////////////

  public void testProcessTaskGetTaskFormVariablesWithoutAuthorization() {
    // given
    startProcessInstanceByKey(RENDERED_FORM_PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    try {
      // when
      formService.getTaskFormVariables(taskId);
      fail("Exception expected: It should not possible to get task form variables");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(READ_TASK.getName(), message);
      assertTextPresent(RENDERED_FORM_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testProcessTaskGetTaskFormVariablesWithReadPermissionOnTask() {
    // given
    startProcessInstanceByKey(RENDERED_FORM_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, READ);

    // when
    VariableMap variables = formService.getTaskFormVariables(taskId);

    // then
    assertNotNull(variables);
    assertEquals(1, variables.size());
  }

  public void testProcessTaskGetTaskFormVariablesWithReadTaskPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(RENDERED_FORM_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(PROCESS_DEFINITION, RENDERED_FORM_PROCESS_KEY, userId, READ_TASK);

    // when
    VariableMap variables = formService.getTaskFormVariables(taskId);

    // then
    assertNotNull(variables);
    assertEquals(1, variables.size());
  }

  public void testProcessTaskGetTaskFormVariables() {
    // given
    startProcessInstanceByKey(RENDERED_FORM_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, READ);
    createGrantAuthorization(PROCESS_DEFINITION, RENDERED_FORM_PROCESS_KEY, userId, READ_TASK);

    // when
    VariableMap variables = formService.getTaskFormVariables(taskId);

    // then
    assertNotNull(variables);
    assertEquals(1, variables.size());
  }

  // get task form variables (case task) /////////////////////////////////

  public void testCaseTaskGetTaskFormVariables() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();

    // when
    VariableMap variables = formService.getTaskFormVariables(taskId);

    // then
    assertNotNull(variables);
    assertEquals(0, variables.size());
  }

  // submit task form (standalone task) ////////////////////////////////

  public void testStandaloneTaskSubmitTaskFormWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    try {
      // when
      formService.submitTaskForm(taskId, null);
      fail("Exception expected: It should not possible to submit a task form");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
    }

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskSubmitTaskForm() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    formService.submitTaskForm(taskId, null);

    // then
    Task task = selectSingleTask();
    assertNull(task);

    deleteTask(taskId, true);
  }

  // submit task form (process task) ////////////////////////////////

  public void testProcessTaskSubmitTaskFormWithoutAuthorization() {
    // given
    startProcessInstanceByKey(FORM_PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    try {
      // when
      formService.submitTaskForm(taskId, null);
      fail("Exception expected: It should not possible to submit a task form");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(UPDATE_TASK.getName(), message);
      assertTextPresent(FORM_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testProcessTaskSubmitTaskFormWithUpdatePermissionOnTask() {
    // given
    startProcessInstanceByKey(FORM_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, FORM_PROCESS_KEY, userId, UPDATE_TASK);

    // when
    formService.submitTaskForm(taskId, null);

    // then
    Task task = selectSingleTask();
    assertNull(task);
  }

  public void testProcessTaskSubmitTaskFormWithUpdateTaskPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(FORM_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, FORM_PROCESS_KEY, userId, UPDATE_TASK);

    // when
    formService.submitTaskForm(taskId, null);

    // then
    Task task = selectSingleTask();
    assertNull(task);
  }

  public void testProcessTaskSubmitTaskForm() {
    // given
    startProcessInstanceByKey(FORM_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, FORM_PROCESS_KEY, userId, UPDATE_TASK);

    // when
    formService.submitTaskForm(taskId, null);

    // then
    Task task = selectSingleTask();
    assertNull(task);
  }

  // submit task form (case task) ////////////////////////////////

  public void testCaseTaskSubmitTaskForm() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();

    // when
    formService.submitTaskForm(taskId, null);

    // then
    Task task = selectSingleTask();
    assertNull(task);
  }

  // get start form key ////////////////////////////////////////

  public void testGetStartFormKeyWithoutAuthorizations() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(FORM_PROCESS_KEY).getId();

    try {
      // when
      formService.getStartFormKey(processDefinitionId);
      fail("Exception expected: It should not possible to get a start form key");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(FORM_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testGetStartFormKey() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(FORM_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, FORM_PROCESS_KEY, userId, READ);

    // when
    String formKey = formService.getStartFormKey(processDefinitionId);

    // then
    assertEquals("deployment:org/camunda/bpm/engine/test/api/form/start.form", formKey);
  }

  // get task form key ////////////////////////////////////////

  public void testGetTaskFormKeyWithoutAuthorizations() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(FORM_PROCESS_KEY).getId();

    try {
      // when
      formService.getTaskFormKey(processDefinitionId, "task");
      fail("Exception expected: It should not possible to get a task form key");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(FORM_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testGetTaskFormKey() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(FORM_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, FORM_PROCESS_KEY, userId, READ);

    // when
    String formKey = formService.getTaskFormKey(processDefinitionId, "task");

    // then
    assertEquals("deployment:org/camunda/bpm/engine/test/api/form/task.form", formKey);
  }

  // get deployed start form////////////////////////////////////////

  public void testGetDeployedStartForm() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(FORM_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, FORM_PROCESS_KEY, userId, READ);

    // when
    InputStream inputStream = formService.getDeployedStartForm(processDefinitionId);
    assertNotNull(inputStream);
  }

  public void testGetDeployedStartFormWithoutAuthorization() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(FORM_PROCESS_KEY).getId();

    try {
      // when
      formService.getDeployedStartForm(processDefinitionId);
      fail("Exception expected: It should not possible to get a deployed start form");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(FORM_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  // get deployed task form////////////////////////////////////////

  public void testGetDeployedTaskForm() {
    // given
    startProcessInstanceByKey(FORM_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, READ);

    // when
    InputStream inputStream = formService.getDeployedTaskForm(taskId);
    assertNotNull(inputStream);
  }

  public void testGetDeployedTaskFormWithoutAuthorization() {
    // given
    startProcessInstanceByKey(FORM_PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    try {
      // when
      formService.getDeployedTaskForm(taskId);
      fail("Exception expected: It should not possible to get a deployed task form");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(FORM_PROCESS_KEY, message);
      assertTextPresent(TASK.resourceName(), message);
    }
  }

}
