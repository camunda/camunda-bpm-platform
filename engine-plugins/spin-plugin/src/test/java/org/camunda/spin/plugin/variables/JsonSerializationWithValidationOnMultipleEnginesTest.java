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
package org.camunda.spin.plugin.variables;

import static org.camunda.bpm.engine.variable.Variables.objectValue;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.DeserializationTypeValidator;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.spin.DataFormats;
import org.camunda.spin.json.SpinJsonException;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test cases for multiple engines defining different validators that do not
 * override each other although Spin makes heavy use of the {@link DataFormats}
 * class that holds static references regardless of the number of Spin plugins
 * (data formats are overridden for example because they are held once in the
 * DataFormats class)
 */
public class JsonSerializationWithValidationOnMultipleEnginesTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRulePositive = new ProcessEngineBootstrapRule() {
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      DeserializationTypeValidator validatorMock = mock(DeserializationTypeValidator.class);
      when(validatorMock.validate(anyString())).thenReturn(true);
      configuration.setDeserializationTypeValidator(validatorMock);
      configuration.setDeserializationTypeValidationEnabled(true);
      return configuration;
    };
  };

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRuleNegative = new ProcessEngineBootstrapRule() {
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      DeserializationTypeValidator validatorMock = mock(DeserializationTypeValidator.class);
      when(validatorMock.validate(anyString())).thenReturn(false);
      configuration.setDeserializationTypeValidator(validatorMock);
      configuration.setDeserializationTypeValidationEnabled(true);
      return configuration;
    };
  };

  @Rule
  public ProcessEngineRule engineRulePositive = new ProvidedProcessEngineRule(bootstrapRulePositive);

  @Rule
  public ProcessEngineRule engineRuleNegative = new ProvidedProcessEngineRule(bootstrapRuleNegative);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void shouldUsePositiveValidator() {
    // given
    Deployment deployment = engineRulePositive.getRepositoryService().createDeployment()
        .addModelInstance("foo.bpmn", getOneTaskModel())
        .deploy();
    try {
      ProcessInstance instance = engineRulePositive.getRuntimeService().startProcessInstanceByKey("oneTaskProcess");

      // add serialized values to both instances
      JsonSerializable bean = new JsonSerializable("a String", 42, true);
      engineRulePositive.getRuntimeService().setVariable(instance.getId(), "simpleBean",
          objectValue(bean).serializationDataFormat(DataFormats.JSON_DATAFORMAT_NAME).create());

      // when
      Object value = engineRulePositive.getRuntimeService().getVariable(instance.getId(), "simpleBean");

      // then
      assertEquals(bean, value);
    } finally {
      engineRulePositive.getRepositoryService().deleteDeployment(deployment.getId(), true);
    }
  }

  @Test
  public void shouldUseNegativeValidator() {
    // given
    Deployment deployment = engineRuleNegative.getRepositoryService().createDeployment()
        .addModelInstance("foo.bpmn", getOneTaskModel())
        .deploy();
    try {
      ProcessInstance instance = engineRuleNegative.getRuntimeService().startProcessInstanceByKey("oneTaskProcess");

      // add serialized values to both instances
      JsonSerializable bean = new JsonSerializable("a String", 42, true);
      engineRuleNegative.getRuntimeService().setVariable(instance.getId(), "simpleBean",
          objectValue(bean).serializationDataFormat(DataFormats.JSON_DATAFORMAT_NAME).create());

      // then
      thrown.expect(ProcessEngineException.class);
      thrown.expectMessage("Cannot deserialize");
      thrown.expectCause(isA(SpinJsonException.class));

      // when
      engineRuleNegative.getRuntimeService().getVariable(instance.getId(), "simpleBean");
    } finally {
      engineRuleNegative.getRepositoryService().deleteDeployment(deployment.getId(), true);
    }
  }

  protected BpmnModelInstance getOneTaskModel() {
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess("oneTaskProcess")
        .startEvent()
        .userTask()
        .endEvent()
        .done();
    return oneTaskProcess;
  }
}
