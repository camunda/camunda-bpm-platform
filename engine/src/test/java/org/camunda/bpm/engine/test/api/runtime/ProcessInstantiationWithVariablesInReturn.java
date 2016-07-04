/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

import java.util.Arrays;
import java.util.List;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Rule;
import org.junit.rules.RuleChain;

/**
 * Represents the test class for the process instantiation on which
 * the process instance is returned with variables.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class ProcessInstantiationWithVariablesInReturn {

  protected static final String SUBPROCESS_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.subprocess.bpmn20.xml";
  protected static final String SET_VARIABLE_IN_DELEGATE_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstantiationWithVariablesInReturn.setVariableInDelegate.bpmn20.xml";
  protected static final String SET_VARIABLE_IN_DELEGATE_WITH_WAIT_STATE_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstantiationWithVariablesInReturn.setVariableInDelegateWithWaitState.bpmn20.xml";
  protected static final String SIMPLE_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstantiationWithVariablesInReturn.simpleProcess.bpmn20.xml";



  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(testHelper);


  private void checkVariables(VariableMap map) {

    List<VariableInstance> variables = engineRule.getRuntimeService().createVariableInstanceQuery()
            .orderByVariableName().asc().list();
    assertEquals(variables.size(), map.size());
    for (VariableInstance instance : variables) {
      assertTrue(map.containsKey(instance.getName()));
      Object instanceValue = instance.getTypedValue().getValue();
      Object mapValue = map.getValueTyped(instance.getName()).getValue();
      if (instanceValue == null) {
        assertNull(mapValue);
      } else if (instanceValue instanceof byte[]) {
        assertTrue(Arrays.equals((byte[]) instanceValue, (byte[]) mapValue));
      } else {
        assertEquals(instanceValue, mapValue);
      }
    }
  }

  @Deployment(resources = SIMPLE_PROCESS)
  public void testReturnVariablesFromStart() {
    //given execute process with variables
    ProcessInstanceWithVariables procInstance = engineRule.getRuntimeService()
            .createProcessInstanceByKey("simpleProcess")
            .setVariable("aVariable1", "aValue1")
            .setVariableLocal("aVariable2", "aValue2")
            .setVariables(Variables.createVariables().putValue("aVariable3", "aValue3"))
            .setVariablesLocal(Variables.createVariables().putValue("aVariable4", new byte[]{127, 34, 64}))
            .executeWithVariablesInReturn(false, false);

    //when returned instance contains variables
    VariableMap map = procInstance.getVariables();
    assertNotNull(map);

    // then variables equal to variables which are accessible via query
    checkVariables(map);
  }

  @Deployment(resources = SUBPROCESS_PROCESS)
  public void testReturnVariablesFromStartWithWaitstate() {
    //given execute process with variables and wait state
    ProcessInstanceWithVariables procInstance = engineRule.getRuntimeService()
            .createProcessInstanceByKey("subprocess")
            .setVariable("aVariable1", "aValue1")
            .setVariableLocal("aVariable2", "aValue2")
            .setVariables(Variables.createVariables().putValue("aVariable3", "aValue3"))
            .setVariablesLocal(Variables.createVariables().putValue("aVariable4", new byte[]{127, 34, 64}))
            .executeWithVariablesInReturn(false, false);

    //when returned instance contains variables
    VariableMap map = procInstance.getVariables();
    assertNotNull(map);

    // then variables equal to variables which are accessible via query
    checkVariables(map);
  }

  @Deployment(resources = SUBPROCESS_PROCESS)
  public void testReturnVariablesFromStartWithWaitstateStartInSubProcess() {
    //given execute process with variables and wait state in sub process
    ProcessInstanceWithVariables procInstance = engineRule.getRuntimeService()
            .createProcessInstanceByKey("subprocess")
            .setVariable("aVariable1", "aValue1")
            .setVariableLocal("aVariable2", "aValue2")
            .setVariables(Variables.createVariables().putValue("aVariable3", "aValue3"))
            .setVariablesLocal(Variables.createVariables().putValue("aVariable4", new byte[]{127, 34, 64}))
            .startBeforeActivity("innerTask")
            .executeWithVariablesInReturn(true, true);

    //when returned instance contains variables
    VariableMap map = procInstance.getVariables();
    assertNotNull(map);

    // then variables equal to variables which are accessible via query
    checkVariables(map);
  }

  @Deployment(resources = SET_VARIABLE_IN_DELEGATE_PROCESS)
  public void testReturnVariablesFromExecution() {

    //given executed process which sets variables in java delegate
    ProcessInstanceWithVariables procInstance = engineRule.getRuntimeService().createProcessInstanceByKey("variableProcess")
            .executeWithVariablesInReturn(false, false);
    //when returned instance contains variables
    VariableMap map = procInstance.getVariables();
    assertNotNull(map);

    // then variables equal to variables which are accessible via query
    checkVariables(map);
  }

  @Deployment(resources = SET_VARIABLE_IN_DELEGATE_WITH_WAIT_STATE_PROCESS)
  public void testReturnVariablesFromExecutionWithWaitstate() {

    //given executed process which sets variables in java delegate
    ProcessInstanceWithVariables procInstance = engineRule.getRuntimeService().createProcessInstanceByKey("variableProcess")
            .executeWithVariablesInReturn(false, false);
    //when returned instance contains variables
    VariableMap map = procInstance.getVariables();
    assertNotNull(map);

    // then variables equal to variables which are accessible via query
    checkVariables(map);
  }

  @Deployment(resources = SET_VARIABLE_IN_DELEGATE_PROCESS)
  public void testReturnVariablesFromStartAndExecution() {

    //given executed process which sets variables in java delegate
    ProcessInstanceWithVariables procInstance = engineRule.getRuntimeService().createProcessInstanceByKey("variableProcess")
            .setVariable("aVariable1", "aValue1")
            .setVariableLocal("aVariable2", "aValue2")
            .setVariables(Variables.createVariables().putValue("aVariable3", "aValue3"))
            .setVariablesLocal(Variables.createVariables().putValue("aVariable4", new byte[]{127, 34, 64}))
            .executeWithVariablesInReturn(false, false);
    //when returned instance contains variables
    VariableMap map = procInstance.getVariables();
    assertNotNull(map);

    // then variables equal to variables which are accessible via query
    checkVariables(map);
  }

  @Deployment(resources = SET_VARIABLE_IN_DELEGATE_WITH_WAIT_STATE_PROCESS)
  public void testReturnVariablesFromStartAndExecutionWithWaitstate() {

    //given executed process which sets variables in java delegate
    ProcessInstanceWithVariables procInstance = engineRule.getRuntimeService().createProcessInstanceByKey("variableProcess")
            .setVariable("aVariable1", "aValue1")
            .setVariableLocal("aVariable2", "aValue2")
            .setVariables(Variables.createVariables().putValue("aVariable3", "aValue3"))
            .setVariablesLocal(Variables.createVariables().putValue("aVariable4", new byte[]{127, 34, 64}))
            .executeWithVariablesInReturn(false, false);
    //when returned instance contains variables
    VariableMap map = procInstance.getVariables();
    assertNotNull(map);

    // then variables equal to variables which are accessible via query
    checkVariables(map);
  }

}
