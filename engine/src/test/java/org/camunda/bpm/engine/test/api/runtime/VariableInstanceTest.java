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
package org.camunda.bpm.engine.test.api.runtime;

import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.bpmn.scripttask.MySerializable;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class VariableInstanceTest extends PluggableProcessEngineTest {

    private final List<String> deploymentIds = new ArrayList<>();

    @After
    public void tearDown() throws Exception {
        deploymentIds.forEach(deploymentId -> repositoryService.deleteDeployment(deploymentId, true));
    }

    @Test
    @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
    public void shouldUpdateVariableStateOnVariableTypeChangeObjectToLong() {
        // given

        var processInstance = startProcessInstanceWithObjectVariable("oneTaskProcess",
                "variableA", "43");

        VariableInstanceEntity variable = (VariableInstanceEntity) runtimeService.createVariableInstanceQuery()
                .processInstanceIdIn(processInstance.getId())
                .singleResult();

        // Object variables will populate the byte array fields
        assertThat(variable.getByteArrayValue()).isNotNull();
        assertThat(variable.getByteArrayValueId()).isNotNull();

        // The other type fields will be null
        assertThat(variable.getDoubleValue()).isNull();
        assertThat(variable.getLongValue()).isNull();
        assertThat(variable.getTextValue()).isNull();
        assertThat(variable.getTextValue2()).isEqualTo("java.lang.String");

        // when the type is updated from object to integer
        runtimeService.setVariable(processInstance.getId(), "variableA", 43);

        variable = (VariableInstanceEntity) runtimeService.createVariableInstanceQuery()
                .processInstanceIdIn(processInstance.getId())
                .singleResult();

        // then the changed integer type should be reflected in the variable entity row appropriately
        assertThat(variable.getByteArrayValue()).isNull();  // byte array fields should not exist for an integer field
        assertThat(variable.getByteArrayValueId()).isNull();

        assertThat(variable.getDoubleValue()).isNull();
        assertThat(variable.getLongValue()).isEqualTo(43L);
        assertThat(variable.getTextValue()).isEqualTo("43");
        assertThat(variable.getTextValue2()).isNull();
    }

    @Test
    @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
    public void shouldUpdateVariableStateOnVariableTypeChangeDoubleToObject() {
        // given
        var processInstance = startProcessInstanceWithVariable("oneTaskProcess",
                "variableA", 43.0);

        VariableInstanceEntity variable = (VariableInstanceEntity) runtimeService.createVariableInstanceQuery()
                .processInstanceIdIn(processInstance.getId())
                .singleResult();

        // Double variable state is populated
        assertThat(variable.getDoubleValue()).isEqualTo(43.0);
        assertThat(variable.getLongValue()).isNull();
        assertThat(variable.getTextValue()).isNull();
        assertThat(variable.getTextValue2()).isNull();
        assertThat(variable.getValue()).isEqualTo(43.0);

        // The other type fields will be null
        assertThat(variable.getByteArrayValue()).isNull();
        assertThat(variable.getByteArrayValueId()).isNull();

        // when the type is updated from double to object
        setVariableWithObjectValue(processInstance.getId(), "variableA", "43.0"); // object value

        variable = (VariableInstanceEntity) runtimeService.createVariableInstanceQuery()
                .processInstanceIdIn(processInstance.getId())
                .singleResult();

        // then the changed object type should be reflected in the variable entity row appropriately
        assertThat(variable.getByteArrayValue()).isNotNull();  // byte array fields should note exist for an integer field
        assertThat(variable.getByteArrayValueId()).isNotNull();

        assertThat(variable.getDoubleValue()).isNull();
        assertThat(variable.getLongValue()).isNull();
        assertThat(variable.getTextValue()).isNull();
        assertThat(variable.getTextValue2()).isEqualTo("java.lang.String");
    }

    @Test
    @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
    public void shouldUpdateVariableStateOnVariableTypeChangeStringToObject() {
        // given
        var processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
                "variableA", Map.of("variableA", "This is a string value")
        );

        VariableInstanceEntity variable = (VariableInstanceEntity) runtimeService.createVariableInstanceQuery()
                .processInstanceIdIn(processInstance.getId())
                .singleResult();

        // Double variable state is populated
        assertThat(variable.getDoubleValue()).isNull();
        assertThat(variable.getLongValue()).isNull();
        assertThat(variable.getTextValue()).isEqualTo("This is a string value");
        assertThat(variable.getTextValue2()).isNull();
        assertThat(variable.getValue()).isEqualTo("This is a string value");

        // The other type fields will be null
        assertThat(variable.getByteArrayValue()).isNull();
        assertThat(variable.getByteArrayValueId()).isNull();

        // when the type is updated from double to object
        setVariableWithObjectValue(processInstance.getId(), "variableA", "43.0"); // object value

        variable = (VariableInstanceEntity) runtimeService.createVariableInstanceQuery()
                .processInstanceIdIn(processInstance.getId())
                .singleResult();

        // then the changed object type should be reflected in the variable entity row appropriately
        assertThat(variable.getByteArrayValue()).isNotNull();  // byte array fields should note exist for an integer field
        assertThat(variable.getByteArrayValueId()).isNotNull();

        assertThat(variable.getDoubleValue()).isNull();
        assertThat(variable.getLongValue()).isNull();
        assertThat(variable.getTextValue()).isNull();
        assertThat(variable.getTextValue2()).isEqualTo("java.lang.String");
    }

    @Test
    public void shouldNotDeleteByteArrayWhenTypeDoesNotChange() {
        // given a process with a MySerializable variable type
        deployProcess(Bpmn.createExecutableProcess("testProcess")
                .startEvent().camundaAsyncAfter()
                .scriptTask()
                .scriptFormat("groovy")
                .scriptText("println 'var ' + myVar")
                .scriptTask()
                .scriptFormat("groovy")
                .scriptText("execution.setVariable('myVar', new org.camunda.bpm.engine.test.bpmn.scripttask.MySerializable('updated value'))")
                .userTask()
                .endEvent()
                .done());

        // And a process instance with a variable with initial value
        var pi = runtimeService.startProcessInstanceByKey("testProcess", Variables.createVariables().putValue(
                "myVar", new MySerializable("initial value")
        ));

        var variable = (VariableInstanceEntity) runtimeService.createVariableInstanceQuery()
                .processInstanceIdIn(pi.getId())
                .singleResult();

        var byteArrayIdBeforeUpdate = variable.getByteArrayValueId();

        // when the script task is executed and the variable is updated with a new variable with the
        // same type (MySerializable) and different value
        String id = managementService.createJobQuery().singleResult().getId();
        managementService.executeJob(id);

        testRule.waitForJobExecutorToProcessAllJobs(5000);

        variable = (VariableInstanceEntity) runtimeService.createVariableInstanceQuery()
                .processInstanceIdIn(pi.getId())
                .singleResult();

        // then
        assertThat(variable.getTextValue2())
                .withFailMessage("The type of the variable didn't change and should be MySerializable")
                .isEqualTo("org.camunda.bpm.engine.test.bpmn.scripttask.MySerializable");

        assertThat(((MySerializable)variable.getValue()).getName())
                .withFailMessage("The variable changed value")
                .isEqualTo("updated value");

        assertThat(variable.getByteArrayValueId())
                .withFailMessage("The byte array should not be deleted (id changed) since the type did not change")
                .isEqualTo(byteArrayIdBeforeUpdate);
    }

    @Test
    public void shouldDeleteByteArrayOnNullifyOfExistingNonNullVariable() {
        // given a process with a MySerializable variable type with a null value
        deployProcess(Bpmn.createExecutableProcess("testProcess")
                .startEvent().camundaAsyncAfter()
                .scriptTask()
                .scriptFormat("groovy")
                .scriptText("println 'var ' + myVar")
                .scriptTask()
                .scriptFormat("groovy")
                .scriptText("execution.setVariable('myVar', null)") // The new value is null
                .userTask()
                .endEvent()
                .done());

        var pi = runtimeService.startProcessInstanceByKey("testProcess", Variables.createVariables().putValue(
                "myVar", new MySerializable("not null value") // non-null existing variable
        ));

        var variable = (VariableInstanceEntity) runtimeService.createVariableInstanceQuery()
                .processInstanceIdIn(pi.getId())
                .singleResult();

        var byteArrayIdBeforeUpdate = variable.getByteArrayValueId();

        assertThat(variable.getByteArrayValue()).isNotNull();

        // when variable is updated from non-null to null
        String id = managementService.createJobQuery().singleResult().getId();
        managementService.executeJob(id);

        testRule.waitForJobExecutorToProcessAllJobs(5000);

        variable = (VariableInstanceEntity) runtimeService.createVariableInstanceQuery()
                .processInstanceIdIn(pi.getId())
                .singleResult();

        // then
        assertThat(variable.getTypeName())
                .withFailMessage("The type of the variable should be null after the update")
                .isEqualTo("null");

        assertThat(((MySerializable) variable.getValue()))
                .withFailMessage("The variable changed value to null")
                .isNull();

        assertThat(variable.getByteArrayValueId())
                .withFailMessage("The byte array should be deleted (id changed) since the type changes when nullifying an existing non-null value")
                .isNotEqualTo(byteArrayIdBeforeUpdate);
    }

    @Test
    public void shouldDeleteByteArrayOnUpdateOfExistingNullVariable() {
        // given a process with a MySerializable variable type with a null value
        deployProcess(Bpmn.createExecutableProcess("testProcess")
                .startEvent().camundaAsyncAfter()
                .scriptTask()
                .scriptFormat("groovy")
                .scriptText("println 'var ' + myVar")
                .scriptTask()
                .scriptFormat("groovy")                                                     // The new value is not null
                .scriptText("execution.setVariable('myVar', new org.camunda.bpm.engine.test.bpmn.scripttask.MySerializable('non null value'))")
                .userTask()
                .endEvent()
                .done());

        var pi = runtimeService.startProcessInstanceByKey("testProcess", Variables.createVariables().putValue(
                "myVar", null // existing null variable
        ));

        var variable = (VariableInstanceEntity) runtimeService.createVariableInstanceQuery()
                .processInstanceIdIn(pi.getId())
                .singleResult();

        // the bytearray does not exist for a null value
        assertThat(variable.getByteArrayValueId()).isNull();
        assertThat(variable.getByteArrayValue()).isNull();

        // when variable is updated from null to non-null
        String id = managementService.createJobQuery().singleResult().getId();
        managementService.executeJob(id);

        testRule.waitForJobExecutorToProcessAllJobs(5000);

        variable = (VariableInstanceEntity) runtimeService.createVariableInstanceQuery()
                .processInstanceIdIn(pi.getId())
                .singleResult();

        // then
        assertThat(variable.getTextValue2())
                .withFailMessage("The type name of the variable should be set on the text value 2 field")
                .isEqualTo("org.camunda.bpm.engine.test.bpmn.scripttask.MySerializable");

        assertThat(((MySerializable)variable.getValue()).getName())
                .withFailMessage("The variable changed value")
                .isEqualTo("non null value");

        assertThat(variable.getByteArrayValue())
                .withFailMessage("The new byte array value should not be null after the update")
                .isNotNull();

        assertThat(variable.getByteArrayValueId())
                .withFailMessage("The byte array id should not be null after the update ")
                .isNotNull();
    }

    private ProcessInstance startProcessInstanceWithVariable(String processDefinitionKey, String variableName, Object variableValue) {
        VariableMap variables = Variables.createVariables()
                .putValue(variableName, variableValue);

        return runtimeService.startProcessInstanceByKey(processDefinitionKey, variables);
    }

    private ProcessInstance startProcessInstanceWithObjectVariable(String processDefinitionKey, String variableName, Object variableValue) {
        ObjectValue value = Variables.objectValue(variableValue)
                .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
                .create();

        VariableMap variables = Variables.createVariables()
                .putValue(variableName, value);

        return runtimeService.startProcessInstanceByKey(processDefinitionKey, variables);
    }

    private void setVariableWithObjectValue(String executionId, String variableName, Object variableValue) {
        ObjectValue value = Variables.objectValue(variableValue)
                .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
                .create();

        runtimeService.setVariable(executionId, variableName, value);
    }

    protected void deployProcess(BpmnModelInstance process) {
        org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeployment()
                .addModelInstance("testProcess.bpmn", process)
                .deploy();
        deploymentIds.add(deployment.getId());
    }
}