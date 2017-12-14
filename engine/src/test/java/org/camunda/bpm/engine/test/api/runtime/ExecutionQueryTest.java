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

package org.camunda.bpm.engine.test.api.runtime;

import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.executionByProcessDefinitionId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.executionByProcessDefinitionKey;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.executionByProcessInstanceId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.hierarchical;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.inverted;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.verifySorting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Assert;


/**
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class ExecutionQueryTest extends PluggableProcessEngineTestCase {

  private static String CONCURRENT_PROCESS_KEY = "concurrent";
  private static String SEQUENTIAL_PROCESS_KEY = "oneTaskProcess";

  private List<String> concurrentProcessInstanceIds;
  private List<String> sequentialProcessInstanceIds;

  protected void setUp() throws Exception {
    super.setUp();
    repositoryService.createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
      .addClasspathResource("org/camunda/bpm/engine/test/api/runtime/concurrentExecution.bpmn20.xml")
      .deploy();

    concurrentProcessInstanceIds = new ArrayList<String>();
    sequentialProcessInstanceIds = new ArrayList<String>();

    for (int i = 0; i < 4; i++) {
      concurrentProcessInstanceIds.add(runtimeService.startProcessInstanceByKey(CONCURRENT_PROCESS_KEY, "BUSINESS-KEY-" + i).getId());
    }
    sequentialProcessInstanceIds.add(runtimeService.startProcessInstanceByKey(SEQUENTIAL_PROCESS_KEY).getId());
  }

  protected void tearDown() throws Exception {
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
    super.tearDown();
  }

  public void testQueryByProcessDefinitionKey() {
    // Concurrent process with 3 executions for each process instance
    assertEquals(12, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).list().size());
    assertEquals(1, runtimeService.createExecutionQuery().processDefinitionKey(SEQUENTIAL_PROCESS_KEY).list().size());
  }

  public void testQueryByInvalidProcessDefinitionKey() {
    ExecutionQuery query = runtimeService.createExecutionQuery().processDefinitionKey("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }

  public void testQueryByProcessInstanceId() {
    for (String processInstanceId : concurrentProcessInstanceIds) {
      ExecutionQuery query =  runtimeService.createExecutionQuery().processInstanceId(processInstanceId);
      assertEquals(3, query.list().size());
      assertEquals(3, query.count());
    }
    assertEquals(1, runtimeService.createExecutionQuery().processInstanceId(sequentialProcessInstanceIds.get(0)).list().size());
  }

  public void testQueryByInvalidProcessInstanceId() {
    ExecutionQuery query = runtimeService.createExecutionQuery().processInstanceId("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }

  public void testQueryExecutionId() {
    Execution execution = runtimeService.createExecutionQuery().processDefinitionKey(SEQUENTIAL_PROCESS_KEY).singleResult();
    assertNotNull(runtimeService.createExecutionQuery().executionId(execution.getId()));
  }

  public void testQueryByInvalidExecutionId() {
    ExecutionQuery query = runtimeService.createExecutionQuery().executionId("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }

  public void testQueryByActivityId() {
    ExecutionQuery query = runtimeService.createExecutionQuery().activityId("receivePayment");
    assertEquals(4, query.list().size());
    assertEquals(4, query.count());

    try {
      assertNull(query.singleResult());
      fail();
    } catch (ProcessEngineException e) { }
  }

  public void testQueryByInvalidActivityId() {
    ExecutionQuery query = runtimeService.createExecutionQuery().activityId("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }

  public void testQueryPaging() {
    assertEquals(13, runtimeService.createExecutionQuery().count());
    assertEquals(4, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).listPage(0, 4).size());
    assertEquals(1, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).listPage(2, 1).size());
    assertEquals(10, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).listPage(1, 10).size());
    assertEquals(12, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).listPage(0, 20).size());
  }

  @SuppressWarnings("unchecked")
  public void testQuerySorting() {

    // 13 executions: 3 for each concurrent, 1 for the sequential
    List<Execution> executions = runtimeService.createExecutionQuery().orderByProcessInstanceId().asc().list();
    assertEquals(13, executions.size());
    verifySorting(executions, executionByProcessInstanceId());

    executions = runtimeService.createExecutionQuery().orderByProcessDefinitionId().asc().list();
    assertEquals(13, executions.size());
    verifySorting(executions, executionByProcessDefinitionId());

    executions = runtimeService.createExecutionQuery().orderByProcessDefinitionKey().asc().list();
    assertEquals(13, executions.size());
    verifySorting(executions, executionByProcessDefinitionKey(processEngine));

    executions = runtimeService.createExecutionQuery().orderByProcessInstanceId().desc().list();
    assertEquals(13, executions.size());
    verifySorting(executions, inverted(executionByProcessInstanceId()));

    executions = runtimeService.createExecutionQuery().orderByProcessDefinitionId().desc().list();
    assertEquals(13, executions.size());
    verifySorting(executions, inverted(executionByProcessDefinitionId()));

    executions = runtimeService.createExecutionQuery().orderByProcessDefinitionKey().desc().list();
    assertEquals(13, executions.size());
    verifySorting(executions, inverted(executionByProcessDefinitionKey(processEngine)));

    executions = runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).orderByProcessDefinitionId().asc().list();
    assertEquals(12, executions.size());
    verifySorting(executions, executionByProcessDefinitionId());

    executions = runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).orderByProcessDefinitionId().desc().list();
    assertEquals(12, executions.size());
    verifySorting(executions, executionByProcessDefinitionId());

    executions = runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).orderByProcessDefinitionKey().asc()
        .orderByProcessInstanceId().desc().list();
    assertEquals(12, executions.size());
    verifySorting(executions, hierarchical(executionByProcessDefinitionKey(processEngine), inverted(executionByProcessInstanceId())));
  }

  public void testQueryInvalidSorting() {
    try {
      runtimeService.createExecutionQuery().orderByProcessDefinitionKey().list();
      fail();
    } catch (ProcessEngineException e) {

    }
  }

  public void testQueryByBusinessKey() {
    assertEquals(3, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).processInstanceBusinessKey("BUSINESS-KEY-1").list().size());
    assertEquals(3, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).processInstanceBusinessKey("BUSINESS-KEY-2").list().size());
    assertEquals(0, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).processInstanceBusinessKey("NON-EXISTING").list().size());
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryStringVariable() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    vars.put("stringVar2", "ghijkl");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("stringVar", "azerty");
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // Test EQUAL on single string variable, should result in 2 matches
    ExecutionQuery query = runtimeService.createExecutionQuery().variableValueEquals("stringVar", "abcdef");
    List<Execution> executions = query.list();
    Assert.assertNotNull(executions);
    Assert.assertEquals(2, executions.size());

    // Test EQUAL on two string variables, should result in single match
    query = runtimeService.createExecutionQuery().variableValueEquals("stringVar", "abcdef").variableValueEquals("stringVar2", "ghijkl");
    Execution execution = query.singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance2.getId(), execution.getId());

    // Test NOT_EQUAL, should return only 1 execution
    execution = runtimeService.createExecutionQuery().variableValueNotEquals("stringVar", "abcdef").singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    // Test GREATER_THAN, should return only matching 'azerty'
    execution = runtimeService.createExecutionQuery().variableValueGreaterThan("stringVar", "abcdef").singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    execution = runtimeService.createExecutionQuery().variableValueGreaterThan("stringVar", "z").singleResult();
    Assert.assertNull(execution);

    // Test GREATER_THAN_OR_EQUAL, should return 3 results
    assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("stringVar", "abcdef").count());
    assertEquals(0, runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("stringVar", "z").count());

    // Test LESS_THAN, should return 2 results
    executions = runtimeService.createExecutionQuery().variableValueLessThan("stringVar", "abcdeg").list();
    Assert.assertEquals(2, executions.size());
    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(executions.get(0).getId(), executions.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());

    assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThan("stringVar", "abcdef").count());
    assertEquals(3, runtimeService.createExecutionQuery().variableValueLessThanOrEqual("stringVar", "z").count());

    // Test LESS_THAN_OR_EQUAL
    executions = runtimeService.createExecutionQuery().variableValueLessThanOrEqual("stringVar", "abcdef").list();
    Assert.assertEquals(2, executions.size());
    expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(Arrays.asList(executions.get(0).getId(), executions.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());

    assertEquals(3, runtimeService.createExecutionQuery().variableValueLessThanOrEqual("stringVar", "z").count());
    assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThanOrEqual("stringVar", "aa").count());

    // Test LIKE
    execution = runtimeService.createExecutionQuery().variableValueLike("stringVar", "azert%").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());

    execution = runtimeService.createExecutionQuery().variableValueLike("stringVar", "%y").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());

    execution = runtimeService.createExecutionQuery().variableValueLike("stringVar", "%zer%").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());

    assertEquals(3, runtimeService.createExecutionQuery().variableValueLike("stringVar", "a%").count());
    assertEquals(0, runtimeService.createExecutionQuery().variableValueLike("stringVar", "%x%").count());

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryLongVariable() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("longVar", 12345L);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("longVar", 12345L);
    vars.put("longVar2", 67890L);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("longVar", 55555L);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // Query on single long variable, should result in 2 matches
    ExecutionQuery query = runtimeService.createExecutionQuery().variableValueEquals("longVar", 12345L);
    List<Execution> executions = query.list();
    Assert.assertNotNull(executions);
    Assert.assertEquals(2, executions.size());

    // Query on two long variables, should result in single match
    query = runtimeService.createExecutionQuery().variableValueEquals("longVar", 12345L).variableValueEquals("longVar2", 67890L);
    Execution execution = query.singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance2.getId(), execution.getId());

    // Query with unexisting variable value
    execution = runtimeService.createExecutionQuery().variableValueEquals("longVar", 999L).singleResult();
    Assert.assertNull(execution);

    // Test NOT_EQUALS
    execution = runtimeService.createExecutionQuery().variableValueNotEquals("longVar", 12345L).singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    // Test GREATER_THAN
    execution = runtimeService.createExecutionQuery().variableValueGreaterThan("longVar", 44444L).singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    Assert.assertEquals(0, runtimeService.createExecutionQuery().variableValueGreaterThan("longVar", 55555L).count());
    Assert.assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThan("longVar",1L).count());

    // Test GREATER_THAN_OR_EQUAL
    execution = runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("longVar", 44444L).singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    execution = runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("longVar", 55555L).singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    Assert.assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("longVar",1L).count());

    // Test LESS_THAN
    executions = runtimeService.createExecutionQuery().variableValueLessThan("longVar", 55555L).list();
    Assert.assertEquals(2, executions.size());

    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(executions.get(0).getId(), executions.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());

    Assert.assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThan("longVar", 12345L).count());
    Assert.assertEquals(3, runtimeService.createExecutionQuery().variableValueLessThan("longVar",66666L).count());

    // Test LESS_THAN_OR_EQUAL
    executions = runtimeService.createExecutionQuery().variableValueLessThanOrEqual("longVar", 55555L).list();
    Assert.assertEquals(3, executions.size());

    Assert.assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThanOrEqual("longVar", 12344L).count());

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryDoubleVariable() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("doubleVar", 12345.6789);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("doubleVar", 12345.6789);
    vars.put("doubleVar2", 9876.54321);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("doubleVar", 55555.5555);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // Query on single double variable, should result in 2 matches
    ExecutionQuery query = runtimeService.createExecutionQuery().variableValueEquals("doubleVar", 12345.6789);
    List<Execution> executions = query.list();
    Assert.assertNotNull(executions);
    Assert.assertEquals(2, executions.size());

    // Query on two double variables, should result in single value
    query = runtimeService.createExecutionQuery().variableValueEquals("doubleVar", 12345.6789).variableValueEquals("doubleVar2", 9876.54321);
    Execution execution = query.singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance2.getId(), execution.getId());

    // Query with unexisting variable value
    execution = runtimeService.createExecutionQuery().variableValueEquals("doubleVar", 9999.99).singleResult();
    Assert.assertNull(execution);

    // Test NOT_EQUALS
    execution = runtimeService.createExecutionQuery().variableValueNotEquals("doubleVar", 12345.6789).singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    // Test GREATER_THAN
    execution = runtimeService.createExecutionQuery().variableValueGreaterThan("doubleVar", 44444.4444).singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    Assert.assertEquals(0, runtimeService.createExecutionQuery().variableValueGreaterThan("doubleVar", 55555.5555).count());
    Assert.assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThan("doubleVar",1.234).count());

    // Test GREATER_THAN_OR_EQUAL
    execution = runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("doubleVar", 44444.4444).singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    execution = runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("doubleVar", 55555.5555).singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    Assert.assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("doubleVar",1.234).count());

    // Test LESS_THAN
    executions = runtimeService.createExecutionQuery().variableValueLessThan("doubleVar", 55555.5555).list();
    Assert.assertEquals(2, executions.size());

    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(executions.get(0).getId(), executions.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());

    Assert.assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThan("doubleVar", 12345.6789).count());
    Assert.assertEquals(3, runtimeService.createExecutionQuery().variableValueLessThan("doubleVar",66666.6666).count());

    // Test LESS_THAN_OR_EQUAL
    executions = runtimeService.createExecutionQuery().variableValueLessThanOrEqual("doubleVar", 55555.5555).list();
    Assert.assertEquals(3, executions.size());

    Assert.assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThanOrEqual("doubleVar", 12344.6789).count());

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryIntegerVariable() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("integerVar", 12345);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("integerVar", 12345);
    vars.put("integerVar2", 67890);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("integerVar", 55555);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // Query on single integer variable, should result in 2 matches
    ExecutionQuery query = runtimeService.createExecutionQuery().variableValueEquals("integerVar", 12345);
    List<Execution> executions = query.list();
    Assert.assertNotNull(executions);
    Assert.assertEquals(2, executions.size());

    // Query on two integer variables, should result in single value
    query = runtimeService.createExecutionQuery().variableValueEquals("integerVar", 12345).variableValueEquals("integerVar2", 67890);
    Execution execution = query.singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance2.getId(), execution.getId());

    // Query with unexisting variable value
    execution = runtimeService.createExecutionQuery().variableValueEquals("integerVar", 9999).singleResult();
    Assert.assertNull(execution);

    // Test NOT_EQUALS
    execution = runtimeService.createExecutionQuery().variableValueNotEquals("integerVar", 12345).singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    // Test GREATER_THAN
    execution = runtimeService.createExecutionQuery().variableValueGreaterThan("integerVar", 44444).singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    Assert.assertEquals(0, runtimeService.createExecutionQuery().variableValueGreaterThan("integerVar", 55555).count());
    Assert.assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThan("integerVar",1).count());

    // Test GREATER_THAN_OR_EQUAL
    execution = runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("integerVar", 44444).singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    execution = runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("integerVar", 55555).singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    Assert.assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("integerVar",1).count());

    // Test LESS_THAN
    executions = runtimeService.createExecutionQuery().variableValueLessThan("integerVar", 55555).list();
    Assert.assertEquals(2, executions.size());

    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(executions.get(0).getId(), executions.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());

    Assert.assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThan("integerVar", 12345).count());
    Assert.assertEquals(3, runtimeService.createExecutionQuery().variableValueLessThan("integerVar",66666).count());

    // Test LESS_THAN_OR_EQUAL
    executions = runtimeService.createExecutionQuery().variableValueLessThanOrEqual("integerVar", 55555).list();
    Assert.assertEquals(3, executions.size());

    Assert.assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThanOrEqual("integerVar", 12344).count());

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryShortVariable() {
    Map<String, Object> vars = new HashMap<String, Object>();
    short shortVar = 1234;
    vars.put("shortVar", shortVar);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    short shortVar2 = 6789;
    vars = new HashMap<String, Object>();
    vars.put("shortVar", shortVar);
    vars.put("shortVar2", shortVar2);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("shortVar", (short)5555);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // Query on single short variable, should result in 2 matches
    ExecutionQuery query = runtimeService.createExecutionQuery().variableValueEquals("shortVar", shortVar);
    List<Execution> executions = query.list();
    Assert.assertNotNull(executions);
    Assert.assertEquals(2, executions.size());

    // Query on two short variables, should result in single value
    query = runtimeService.createExecutionQuery().variableValueEquals("shortVar", shortVar).variableValueEquals("shortVar2", shortVar2);
    Execution execution = query.singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance2.getId(), execution.getId());

    // Query with unexisting variable value
    short unexistingValue = (short)9999;
    execution = runtimeService.createExecutionQuery().variableValueEquals("shortVar", unexistingValue).singleResult();
    Assert.assertNull(execution);

    // Test NOT_EQUALS
    execution = runtimeService.createExecutionQuery().variableValueNotEquals("shortVar", (short)1234).singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    // Test GREATER_THAN
    execution = runtimeService.createExecutionQuery().variableValueGreaterThan("shortVar", (short)4444).singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    Assert.assertEquals(0, runtimeService.createExecutionQuery().variableValueGreaterThan("shortVar", (short)5555).count());
    Assert.assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThan("shortVar",(short)1).count());

    // Test GREATER_THAN_OR_EQUAL
    execution = runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("shortVar", (short)4444).singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    execution = runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("shortVar", (short)5555).singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    Assert.assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("shortVar",(short)1).count());

    // Test LESS_THAN
    executions = runtimeService.createExecutionQuery().variableValueLessThan("shortVar", (short)5555).list();
    Assert.assertEquals(2, executions.size());

    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(executions.get(0).getId(), executions.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());

    Assert.assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThan("shortVar", (short)1234).count());
    Assert.assertEquals(3, runtimeService.createExecutionQuery().variableValueLessThan("shortVar",(short)6666).count());

    // Test LESS_THAN_OR_EQUAL
    executions = runtimeService.createExecutionQuery().variableValueLessThanOrEqual("shortVar", (short)5555).list();
    Assert.assertEquals(3, executions.size());

    Assert.assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThanOrEqual("shortVar", (short)1233).count());

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryDateVariable() throws Exception {
    Map<String, Object> vars = new HashMap<String, Object>();
    Date date1 = Calendar.getInstance().getTime();
    vars.put("dateVar", date1);

    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    Date date2 = Calendar.getInstance().getTime();
    vars = new HashMap<String, Object>();
    vars.put("dateVar", date1);
    vars.put("dateVar2", date2);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    Calendar nextYear = Calendar.getInstance();
    nextYear.add(Calendar.YEAR, 1);
    vars = new HashMap<String, Object>();
    vars.put("dateVar",nextYear.getTime());
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    Calendar nextMonth = Calendar.getInstance();
    nextMonth.add(Calendar.MONTH, 1);

    Calendar twoYearsLater = Calendar.getInstance();
    twoYearsLater.add(Calendar.YEAR, 2);

    Calendar oneYearAgo = Calendar.getInstance();
    oneYearAgo.add(Calendar.YEAR, -1);

    // Query on single short variable, should result in 2 matches
    ExecutionQuery query = runtimeService.createExecutionQuery().variableValueEquals("dateVar", date1);
    List<Execution> executions = query.list();
    Assert.assertNotNull(executions);
    Assert.assertEquals(2, executions.size());

    // Query on two short variables, should result in single value
    query = runtimeService.createExecutionQuery().variableValueEquals("dateVar", date1).variableValueEquals("dateVar2", date2);
    Execution execution = query.singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance2.getId(), execution.getId());

    // Query with unexisting variable value
    Date unexistingDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/01/1989 12:00:00");
    execution = runtimeService.createExecutionQuery().variableValueEquals("dateVar", unexistingDate).singleResult();
    Assert.assertNull(execution);

    // Test NOT_EQUALS
    execution = runtimeService.createExecutionQuery().variableValueNotEquals("dateVar", date1).singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    // Test GREATER_THAN
    execution = runtimeService.createExecutionQuery().variableValueGreaterThan("dateVar", nextMonth.getTime()).singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    Assert.assertEquals(0, runtimeService.createExecutionQuery().variableValueGreaterThan("dateVar", nextYear.getTime()).count());
    Assert.assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThan("dateVar", oneYearAgo.getTime()).count());

    // Test GREATER_THAN_OR_EQUAL
    execution = runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("dateVar", nextMonth.getTime()).singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    execution = runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("dateVar", nextYear.getTime()).singleResult();
    Assert.assertNotNull(execution);
    Assert.assertEquals(processInstance3.getId(), execution.getId());

    Assert.assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("dateVar",oneYearAgo.getTime()).count());

    // Test LESS_THAN
    executions = runtimeService.createExecutionQuery().variableValueLessThan("dateVar", nextYear.getTime()).list();
    Assert.assertEquals(2, executions.size());

    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(executions.get(0).getId(), executions.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());

    Assert.assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThan("dateVar", date1).count());
    Assert.assertEquals(3, runtimeService.createExecutionQuery().variableValueLessThan("dateVar", twoYearsLater.getTime()).count());

    // Test LESS_THAN_OR_EQUAL
    executions = runtimeService.createExecutionQuery().variableValueLessThanOrEqual("dateVar", nextYear.getTime()).list();
    Assert.assertEquals(3, executions.size());

    Assert.assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThanOrEqual("dateVar", oneYearAgo.getTime()).count());

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testBooleanVariable() throws Exception {

    // TEST EQUALS
    HashMap<String, Object> vars = new HashMap<String, Object>();
    vars.put("booleanVar", true);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("booleanVar", false);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().variableValueEquals("booleanVar", true).list();

    assertNotNull(instances);
    assertEquals(1, instances.size());
    assertEquals(processInstance1.getId(), instances.get(0).getId());

    instances = runtimeService.createProcessInstanceQuery().variableValueEquals("booleanVar", false).list();

    assertNotNull(instances);
    assertEquals(1, instances.size());
    assertEquals(processInstance2.getId(), instances.get(0).getId());

    // TEST NOT_EQUALS
    instances = runtimeService.createProcessInstanceQuery().variableValueNotEquals("booleanVar", true).list();

    assertNotNull(instances);
    assertEquals(1, instances.size());
    assertEquals(processInstance2.getId(), instances.get(0).getId());

    instances = runtimeService.createProcessInstanceQuery().variableValueNotEquals("booleanVar", false).list();

    assertNotNull(instances);
    assertEquals(1, instances.size());
    assertEquals(processInstance1.getId(), instances.get(0).getId());

    // Test unsupported operations
    try {
      runtimeService.createProcessInstanceQuery().variableValueGreaterThan("booleanVar", true);
      fail("Excetion expected");
    } catch(ProcessEngineException ae) {
      assertTextPresent("Booleans and null cannot be used in 'greater than' condition", ae.getMessage());
    }

    try {
      runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("booleanVar", true);
      fail("Excetion expected");
    } catch(ProcessEngineException ae) {
      assertTextPresent("Booleans and null cannot be used in 'greater than or equal' condition", ae.getMessage());
    }

    try {
      runtimeService.createProcessInstanceQuery().variableValueLessThan("booleanVar", true);
      fail("Excetion expected");
    } catch(ProcessEngineException ae) {
      assertTextPresent("Booleans and null cannot be used in 'less than' condition", ae.getMessage());
    }

    try {
      runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("booleanVar", true);
      fail("Excetion expected");
    } catch(ProcessEngineException ae) {
      assertTextPresent("Booleans and null cannot be used in 'less than or equal' condition", ae.getMessage());
    }

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryVariablesUpdatedToNullValue() {
    // Start process instance with different types of variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 928374L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "coca-cola");
    variables.put("booleanVar", true);
    variables.put("dateVar", new Date());
    variables.put("nullVar", null);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    ExecutionQuery query = runtimeService.createExecutionQuery()
      .variableValueEquals("longVar", null)
      .variableValueEquals("shortVar", null)
      .variableValueEquals("integerVar", null)
      .variableValueEquals("stringVar", null)
      .variableValueEquals("booleanVar", null)
      .variableValueEquals("dateVar", null);

    ExecutionQuery notQuery = runtimeService.createExecutionQuery()
    .variableValueNotEquals("longVar", null)
    .variableValueNotEquals("shortVar", null)
    .variableValueNotEquals("integerVar", null)
    .variableValueNotEquals("stringVar", null)
    .variableValueNotEquals("booleanVar", null)
    .variableValueNotEquals("dateVar", null);

    assertNull(query.singleResult());
    assertNotNull(notQuery.singleResult());

    // Set all existing variables values to null
    runtimeService.setVariable(processInstance.getId(), "longVar", null);
    runtimeService.setVariable(processInstance.getId(), "shortVar", null);
    runtimeService.setVariable(processInstance.getId(), "integerVar", null);
    runtimeService.setVariable(processInstance.getId(), "stringVar", null);
    runtimeService.setVariable(processInstance.getId(), "booleanVar", null);
    runtimeService.setVariable(processInstance.getId(), "dateVar", null);
    runtimeService.setVariable(processInstance.getId(), "nullVar", null);

    Execution queryResult = query.singleResult();
    assertNotNull(queryResult);
    assertEquals(processInstance.getId(), queryResult.getId());
    assertNull(notQuery.singleResult());
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryNullVariable() throws Exception {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("nullVar", null);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("nullVar", "notnull");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("nullVarLong", "notnull");
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("nullVarDouble", "notnull");
    ProcessInstance processInstance4 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("nullVarByte", "testbytes".getBytes());
    ProcessInstance processInstance5 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // Query on null value, should return one value
    ExecutionQuery query = runtimeService.createExecutionQuery().variableValueEquals("nullVar", null);
    List<Execution> executions = query.list();
    Assert.assertNotNull(executions);
    Assert.assertEquals(1, executions.size());
    Assert.assertEquals(processInstance1.getId(), executions.get(0).getId());

    // Test NOT_EQUALS null
    Assert.assertEquals(1, runtimeService.createExecutionQuery().variableValueNotEquals("nullVar", null).count());
    Assert.assertEquals(1, runtimeService.createExecutionQuery().variableValueNotEquals("nullVarLong", null).count());
    Assert.assertEquals(1, runtimeService.createExecutionQuery().variableValueNotEquals("nullVarDouble", null).count());
    // When a byte-array refrence is present, the variable is not considered null
    Assert.assertEquals(1, runtimeService.createExecutionQuery().variableValueNotEquals("nullVarByte", null).count());

    // All other variable queries with null should throw exception
    try {
      runtimeService.createExecutionQuery().variableValueGreaterThan("nullVar", null);
      fail("Excetion expected");
    } catch(ProcessEngineException ae) {
      assertTextPresent("Booleans and null cannot be used in 'greater than' condition", ae.getMessage());
    }

    try {
      runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("nullVar", null);
      fail("Excetion expected");
    } catch(ProcessEngineException ae) {
      assertTextPresent("Booleans and null cannot be used in 'greater than or equal' condition", ae.getMessage());
    }

    try {
      runtimeService.createExecutionQuery().variableValueLessThan("nullVar", null);
      fail("Excetion expected");
    } catch(ProcessEngineException ae) {
      assertTextPresent("Booleans and null cannot be used in 'less than' condition", ae.getMessage());
    }

    try {
      runtimeService.createExecutionQuery().variableValueLessThanOrEqual("nullVar", null);
      fail("Excetion expected");
    } catch(ProcessEngineException ae) {
      assertTextPresent("Booleans and null cannot be used in 'less than or equal' condition", ae.getMessage());
    }

    try {
      runtimeService.createExecutionQuery().variableValueLike("nullVar", null);
      fail("Excetion expected");
    } catch(ProcessEngineException ae) {
      assertTextPresent("Booleans and null cannot be used in 'like' condition", ae.getMessage());
    }

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance4.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance5.getId(), "test");
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryInvalidTypes() throws Exception {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("bytesVar", "test".getBytes());
    vars.put("serializableVar",new DummySerializable());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    try {
      runtimeService.createExecutionQuery()
        .variableValueEquals("bytesVar", "test".getBytes())
        .list();
      fail("Expected exception");
    } catch(ProcessEngineException ae) {
      assertTextPresent("Variables of type ByteArray cannot be used to query", ae.getMessage());
    }

    try {
      runtimeService.createExecutionQuery()
        .variableValueEquals("serializableVar", new DummySerializable())
        .list();
      fail("Expected exception");
    } catch(ProcessEngineException ae) {
      assertTextPresent("Object values cannot be used to query", ae.getMessage());
    }

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
  }

  public void testQueryVariablesNullNameArgument() {
    try {
      runtimeService.createExecutionQuery().variableValueEquals(null, "value");
      fail("Expected exception");
    } catch(ProcessEngineException ae) {
      assertTextPresent("name is null", ae.getMessage());
    }
    try {
      runtimeService.createExecutionQuery().variableValueNotEquals(null, "value");
      fail("Expected exception");
    } catch(ProcessEngineException ae) {
      assertTextPresent("name is null", ae.getMessage());
    }
    try {
      runtimeService.createExecutionQuery().variableValueGreaterThan(null, "value");
      fail("Expected exception");
    } catch(ProcessEngineException ae) {
      assertTextPresent("name is null", ae.getMessage());
    }
    try {
      runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual(null, "value");
      fail("Expected exception");
    } catch(ProcessEngineException ae) {
      assertTextPresent("name is null", ae.getMessage());
    }
    try {
      runtimeService.createExecutionQuery().variableValueLessThan(null, "value");
      fail("Expected exception");
    } catch(ProcessEngineException ae) {
      assertTextPresent("name is null", ae.getMessage());
    }
    try {
      runtimeService.createExecutionQuery().variableValueLessThanOrEqual(null, "value");
      fail("Expected exception");
    } catch(ProcessEngineException ae) {
      assertTextPresent("name is null", ae.getMessage());
    }
    try {
      runtimeService.createExecutionQuery().variableValueLike(null, "value");
      fail("Expected exception");
    } catch(ProcessEngineException ae) {
      assertTextPresent("name is null", ae.getMessage());
    }
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryAllVariableTypes() throws Exception {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("nullVar", null);
    vars.put("stringVar", "string");
    vars.put("longVar", 10L);
    vars.put("doubleVar", 1.2);
    vars.put("integerVar", 1234);
    vars.put("booleanVar", true);
    vars.put("shortVar", (short) 123);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    ExecutionQuery query = runtimeService.createExecutionQuery()
      .variableValueEquals("nullVar", null)
      .variableValueEquals("stringVar", "string")
      .variableValueEquals("longVar", 10L)
      .variableValueEquals("doubleVar", 1.2)
      .variableValueEquals("integerVar", 1234)
      .variableValueEquals("booleanVar", true)
      .variableValueEquals("shortVar", (short) 123);

    List<Execution> executions = query.list();
    Assert.assertNotNull(executions);
    Assert.assertEquals(1, executions.size());
    Assert.assertEquals(processInstance.getId(), executions.get(0).getId());

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testClashingValues() throws Exception {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 1234L);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    Map<String, Object> vars2 = new HashMap<String, Object>();
    vars2.put("var", 1234);

    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars2);

    List<Execution> executions = runtimeService.createExecutionQuery()
    .processDefinitionKey("oneTaskProcess")
    .variableValueEquals("var", 1234L)
    .list();

    assertEquals(1, executions.size());
    assertEquals(processInstance.getId(), executions.get(0).getProcessInstanceId());

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
}

  @Deployment
  public void testQueryBySignalSubscriptionName() {
    runtimeService.startProcessInstanceByKey("catchSignal");

    // it finds subscribed instances
    Execution execution = runtimeService.createExecutionQuery()
      .signalEventSubscription("alert")
      .singleResult();
    assertNotNull(execution);

    // test query for nonexisting subscription
    execution = runtimeService.createExecutionQuery()
            .signalEventSubscription("nonExisitng")
            .singleResult();
    assertNull(execution);

    // it finds more than one
    runtimeService.startProcessInstanceByKey("catchSignal");
    assertEquals(2, runtimeService.createExecutionQuery().signalEventSubscription("alert").count());
  }

  @Deployment
  public void testQueryBySignalSubscriptionNameBoundary() {
    runtimeService.startProcessInstanceByKey("signalProces");

    // it finds subscribed instances
    Execution execution = runtimeService.createExecutionQuery()
      .signalEventSubscription("Test signal")
      .singleResult();
    assertNotNull(execution);

    // test query for nonexisting subscription
    execution = runtimeService.createExecutionQuery()
            .signalEventSubscription("nonExisitng")
            .singleResult();
    assertNull(execution);

    // it finds more than one
    runtimeService.startProcessInstanceByKey("signalProces");
    assertEquals(2, runtimeService.createExecutionQuery().signalEventSubscription("Test signal").count());
  }

  public void testNativeQuery() {
    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();
    // just test that the query will be constructed and executed, details are tested in the TaskQueryTest
    assertEquals(tablePrefix + "ACT_RU_EXECUTION", managementService.getTableName(Execution.class));

    long executionCount = runtimeService.createExecutionQuery().count();

    assertEquals(executionCount, runtimeService.createNativeExecutionQuery().sql("SELECT * FROM " + managementService.getTableName(Execution.class)).list().size());
    assertEquals(executionCount, runtimeService.createNativeExecutionQuery().sql("SELECT count(*) FROM " + managementService.getTableName(Execution.class)).count());
  }

  public void testNativeQueryPaging() {
    assertEquals(5, runtimeService.createNativeExecutionQuery().sql("SELECT * FROM " + managementService.getTableName(Execution.class)).listPage(1, 5).size());
    assertEquals(1, runtimeService.createNativeExecutionQuery().sql("SELECT * FROM " + managementService.getTableName(Execution.class)).listPage(2, 1).size());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/concurrentExecution.bpmn20.xml"})
  public void testExecutionQueryWithProcessVariable() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("x", "parent");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("concurrent", variables);

    List<Execution> concurrentExecutions = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).list();
    assertEquals(3, concurrentExecutions.size());
    for (Execution execution : concurrentExecutions) {
      if (!((ExecutionEntity)execution).isProcessInstanceExecution()) {
        // only the concurrent executions, not the root one, would be cooler to query that directly, see http://jira.codehaus.org/browse/ACT-1373
        runtimeService.setVariableLocal(execution.getId(), "x", "child");
      }
    }

    assertEquals(2, runtimeService.createExecutionQuery().processInstanceId(pi.getId()).variableValueEquals("x", "child").count());
    assertEquals(1, runtimeService.createExecutionQuery().processInstanceId(pi.getId()).variableValueEquals("x", "parent").count());

    assertEquals(3, runtimeService.createExecutionQuery().processInstanceId(pi.getId()).processVariableValueEquals("x", "parent").count());
    assertEquals(3, runtimeService.createExecutionQuery().processInstanceId(pi.getId()).processVariableValueNotEquals("x", "xxx").count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/concurrentExecution.bpmn20.xml"})
  public void testExecutionQueryForSuspendedExecutions() {
    List<Execution> suspendedExecutions = runtimeService.createExecutionQuery().suspended().list();
    assertEquals(suspendedExecutions.size(), 0);

    for (String instanceId : concurrentProcessInstanceIds) {
      runtimeService.suspendProcessInstanceById(instanceId);
    }

    suspendedExecutions = runtimeService.createExecutionQuery().suspended().list();
    assertEquals(12, suspendedExecutions.size());

    List<Execution> activeExecutions = runtimeService.createExecutionQuery().active().list();
    assertEquals(1, activeExecutions.size());

    for (Execution activeExecution : activeExecutions) {
      assertEquals(activeExecution.getProcessInstanceId(), sequentialProcessInstanceIds.get(0));
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/failingProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByIncidentId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();

    List<Execution> executionList = runtimeService
        .createExecutionQuery()
        .incidentId(incident.getId()).list();

    assertEquals(1, executionList.size());
  }

  public void testQueryByInvalidIncidentId() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    assertEquals(0, query.incidentId("invalid").count());

    try {
      query.incidentId(null);
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/failingProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByIncidentType() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();

    List<Execution> executionList = runtimeService
        .createExecutionQuery()
        .incidentType(incident.getIncidentType()).list();

    assertEquals(1, executionList.size());
  }

  public void testQueryByInvalidIncidentType() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    assertEquals(0, query.incidentType("invalid").count());

    try {
      query.incidentType(null);
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/failingProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByIncidentMessage() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();

    List<Execution> executionList = runtimeService
        .createExecutionQuery()
        .incidentMessage(incident.getIncidentMessage()).list();

    assertEquals(1, executionList.size());
  }

  public void testQueryByInvalidIncidentMessage() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    assertEquals(0, query.incidentMessage("invalid").count());

    try {
      query.incidentMessage(null);
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/failingProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByIncidentMessageLike() {
    runtimeService.startProcessInstanceByKey("failingProcess");

    executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    List<Execution> executionList = runtimeService
        .createExecutionQuery()
        .incidentMessageLike("%\\_exception%").list();

    assertEquals(1, executionList.size());
  }

  public void testQueryByInvalidIncidentMessageLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    assertEquals(0, query.incidentMessageLike("invalid").count());

    try {
      query.incidentMessageLike(null);
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/failingSubProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByIncidentIdSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingSubProcess");

    executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();

    List<Execution> executionList = runtimeService
        .createExecutionQuery()
        .incidentId(incident.getId()).list();

    assertEquals(1, executionList.size());
    // execution id of subprocess != process instance id
    assertNotSame(processInstance.getId(), executionList.get(0).getId());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/failingSubProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByIncidentTypeInSubprocess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingSubProcess");

    executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();

    List<Execution> executionList = runtimeService
        .createExecutionQuery()
        .incidentType(incident.getIncidentType()).list();

    assertEquals(1, executionList.size());
    // execution id of subprocess != process instance id
    assertNotSame(processInstance.getId(), executionList.get(0).getId());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/failingSubProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByIncidentMessageInSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingSubProcess");

    executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();

    List<Execution> executionList = runtimeService
        .createExecutionQuery()
        .incidentMessage(incident.getIncidentMessage()).list();

    assertEquals(1, executionList.size());
    // execution id of subprocess != process instance id
    assertNotSame(processInstance.getId(), executionList.get(0).getId());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/failingSubProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByIncidentMessageLikeSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingSubProcess");

    executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();

    List<Execution> executionList = runtimeService
        .createExecutionQuery()
        .incidentMessageLike("%exception%").list();

    assertEquals(1, executionList.size());
    // execution id of subprocess != process instance id
    assertNotSame(processInstance.getId(), executionList.get(0).getId());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/runtime/oneMessageCatchProcess.bpmn20.xml"})
  public void testQueryForExecutionsWithMessageEventSubscriptions() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    ProcessInstance instance1 = runtimeService.startProcessInstanceByKey("oneMessageCatchProcess");
    ProcessInstance instance2 = runtimeService.startProcessInstanceByKey("oneMessageCatchProcess");

    List<Execution> executions = runtimeService.createExecutionQuery()
        .messageEventSubscription().orderByProcessInstanceId().asc().list();

    assertEquals(2, executions.size());
    if (instance1.getId().compareTo(instance2.getId()) < 0) {
      assertEquals(instance1.getId(), executions.get(0).getProcessInstanceId());
      assertEquals(instance2.getId(), executions.get(1).getProcessInstanceId());
    } else {
      assertEquals(instance2.getId(), executions.get(0).getProcessInstanceId());
      assertEquals(instance1.getId(), executions.get(1).getProcessInstanceId());
    }

  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/runtime/oneMessageCatchProcess.bpmn20.xml")
  public void testQueryForExecutionsWithMessageEventSubscriptionsOverlappingFilters() {

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneMessageCatchProcess");

    Execution execution = runtimeService
      .createExecutionQuery()
      .messageEventSubscriptionName("newInvoiceMessage")
      .messageEventSubscription()
      .singleResult();

    assertNotNull(execution);
    assertEquals(instance.getId(), execution.getProcessInstanceId());

    runtimeService
      .createExecutionQuery()
      .messageEventSubscription()
      .messageEventSubscriptionName("newInvoiceMessage")
      .list();

    assertNotNull(execution);
    assertEquals(instance.getId(), execution.getProcessInstanceId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/twoBoundaryEventSubscriptions.bpmn20.xml")
  public void testQueryForExecutionsWithMultipleSubscriptions() {
    // given two message event subscriptions
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process");

    List<EventSubscription> subscriptions =
        runtimeService.createEventSubscriptionQuery().processInstanceId(instance.getId()).list();
    assertEquals(2, subscriptions.size());
    assertEquals(subscriptions.get(0).getExecutionId(), subscriptions.get(1).getExecutionId());

    // should return the execution once (not twice)
    Execution execution = runtimeService
      .createExecutionQuery()
      .messageEventSubscription()
      .singleResult();

    assertNotNull(execution);
    assertEquals(instance.getId(), execution.getProcessInstanceId());

    // should return the execution once
    execution = runtimeService
      .createExecutionQuery()
      .messageEventSubscriptionName("messageName_1")
      .singleResult();

    assertNotNull(execution);
    assertEquals(instance.getId(), execution.getProcessInstanceId());

    // should return the execution once
    execution = runtimeService
      .createExecutionQuery()
      .messageEventSubscriptionName("messageName_2")
      .singleResult();

    assertNotNull(execution);
    assertEquals(instance.getId(), execution.getProcessInstanceId());

    // should return the execution once
    execution = runtimeService
      .createExecutionQuery()
      .messageEventSubscriptionName("messageName_1")
      .messageEventSubscriptionName("messageName_2")
      .singleResult();

    assertNotNull(execution);
    assertEquals(instance.getId(), execution.getProcessInstanceId());

    // should not return the execution
    execution = runtimeService
      .createExecutionQuery()
      .messageEventSubscriptionName("messageName_1")
      .messageEventSubscriptionName("messageName_2")
      .messageEventSubscriptionName("another")
      .singleResult();

    assertNull(execution);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessVariableValueEqualsNumber() throws Exception {
    // long
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123L));

    // non-matching long
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 12345L));

    // short
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", (short) 123));

    // double
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123.0d));

    // integer
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123));

    // untyped null (should not match)
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", null));

    // typed null (should not match)
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", Variables.longValue(null)));

    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "123"));

    assertEquals(4, runtimeService.createExecutionQuery().processVariableValueEquals("var", Variables.numberValue(123)).count());
    assertEquals(4, runtimeService.createExecutionQuery().processVariableValueEquals("var", Variables.numberValue(123L)).count());
    assertEquals(4, runtimeService.createExecutionQuery().processVariableValueEquals("var", Variables.numberValue(123.0d)).count());
    assertEquals(4, runtimeService.createExecutionQuery().processVariableValueEquals("var", Variables.numberValue((short) 123)).count());

    assertEquals(1, runtimeService.createExecutionQuery().processVariableValueEquals("var", Variables.numberValue(null)).count());

    assertEquals(4, runtimeService.createExecutionQuery().variableValueEquals("var", Variables.numberValue(123)).count());
    assertEquals(4, runtimeService.createExecutionQuery().variableValueEquals("var", Variables.numberValue(123L)).count());
    assertEquals(4, runtimeService.createExecutionQuery().variableValueEquals("var", Variables.numberValue(123.0d)).count());
    assertEquals(4, runtimeService.createExecutionQuery().variableValueEquals("var", Variables.numberValue((short) 123)).count());

    assertEquals(1, runtimeService.createExecutionQuery().variableValueEquals("var", Variables.numberValue(null)).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessVariableValueNumberComparison() throws Exception {
    // long
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123L));

    // non-matching long
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 12345L));

    // short
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", (short) 123));

    // double
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123.0d));

    // integer
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123));

    // untyped null
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", null));

    // typed null
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", Variables.longValue(null)));

    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "123"));

    assertEquals(4, runtimeService.createExecutionQuery().processVariableValueNotEquals("var", Variables.numberValue(123)).count());
  }

  public void testNullBusinessKeyForChildExecutions() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CONCURRENT_PROCESS_KEY, "76545");
    List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    for (Execution e : executions) {
      if (((ExecutionEntity) e).isProcessInstanceExecution()) {
        assertEquals("76545", ((ExecutionEntity) e).getBusinessKeyWithoutCascade());
      } else {
        assertNull(((ExecutionEntity) e).getBusinessKeyWithoutCascade());
      }
    }
  }

}
