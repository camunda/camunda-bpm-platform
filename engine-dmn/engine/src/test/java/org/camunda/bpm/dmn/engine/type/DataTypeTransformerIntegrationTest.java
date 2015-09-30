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
package org.camunda.bpm.dmn.engine.type;

import static org.camunda.bpm.dmn.engine.test.asserts.DmnAssertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DmnEngineConfigurationImpl;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnDecisionTest;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.junit.Test;

/**
 * Tests that {@link DataTypeTransformer} is invoked while evaluation of the
 * decision.
 *
 * @author Philipp Ossler
 */
public class DataTypeTransformerIntegrationTest extends DmnDecisionTest {

  protected static final String DMN_OUTPUT_FILE = "org/camunda/bpm/dmn/engine/type/DataTypeTransformerTest-Output.dmn";
  protected static final String DMN_INPUT_FILE = "org/camunda/bpm/dmn/engine/type/DataTypeTransformerTest-Input.dmn";
  protected static final String DMN_NO_TYPE_FILE = "org/camunda/bpm/dmn/engine/type/DataTypeTransformerTest-NoTypes.dmn";

  protected static final TypedValue TRANSFORMED_VALUE = Variables.integerValue(42);

  protected static DataTypeTransformer dataTypeTransformerMock;


  @Test
  @DecisionResource(resource = DMN_OUTPUT_FILE)
  public void invokeTransformerForOutputDefinition() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("output", 21);

    assertThat(engine).evaluates(decision, variables).hasResultValue(TRANSFORMED_VALUE.getValue());

    verify(dataTypeTransformerMock, atLeastOnce()).transform(21);
  }

  @Test
  @DecisionResource(resource = DMN_OUTPUT_FILE)
  public void dontInvokeTransformerForOutputDefinitionWithNull() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("output", null);

    assertThat(engine).evaluates(decision, variables).hasResultValue(null);

    verify(dataTypeTransformerMock, never()).transform(any());
  }

  @Test
  @DecisionResource(resource = DMN_INPUT_FILE)
  public void invokeTransformerForInputTypeDefinition() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input", 21);

    assertThat(engine).evaluates(decision, variables).hasResultValue("is transformed");

    verify(dataTypeTransformerMock, atLeastOnce()).transform(21);
  }

  @Test
  @DecisionResource(resource = DMN_INPUT_FILE)
  public void dontInvokeTransformerForInputTypeDefinitionWithNull() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input", null);

    assertThat(engine).evaluates(decision, variables).hasResultValue("is not transformed");

    verify(dataTypeTransformerMock, never()).transform(any());
  }

  @Test
  @DecisionResource(resource = DMN_NO_TYPE_FILE)
  public void dontInvokeTransformerForNoTypeDefinition() {
    Map<String, Object> variables = new HashMap<String, Object>();
    // no type definition for input clause
    variables.put("input", 21);
    // no output definition for output clause
    variables.put("output", 42);

    assertThat(engine).evaluates(decision, variables).hasResultValue(42);

    verify(dataTypeTransformerMock, never()).transform(any());
  }

  public DmnEngineConfiguration createDmnEngineConfiguration() {
    DmnEngineConfigurationImpl configuration = new DmnEngineConfigurationImpl();

    dataTypeTransformerMock = mock(DataTypeTransformer.class);
    when(dataTypeTransformerMock.transform(any())).thenReturn(TRANSFORMED_VALUE);

    DataTypeTransformerFactory dataTypeTransformerFactory = mock(DataTypeTransformerFactory.class);
    when(dataTypeTransformerFactory.getTransformerForType(anyString())).thenReturn(dataTypeTransformerMock);

    configuration.setDataTypeTransformerFactory(dataTypeTransformerFactory);
    return configuration;
  }

}
