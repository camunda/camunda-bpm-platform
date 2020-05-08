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
package org.camunda.bpm.dmn.engine.type;

import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.spi.type.DmnDataTypeTransformer;
import org.camunda.bpm.dmn.engine.impl.spi.type.DmnDataTypeTransformerRegistry;
import org.camunda.bpm.dmn.engine.impl.type.DefaultDataTypeTransformerRegistry;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.junit.Test;

/**
 * @author Philipp Ossler
 */
public class CustomDataTypeTransformerRegistryTest extends DmnEngineTest {

  protected static final String DMN_INPUT_FILE = "org/camunda/bpm/dmn/engine/type/CustomInputDefinition.dmn";
  protected static final String DMN_OUTPUT_FILE = "org/camunda/bpm/dmn/engine/type/CustomOutputDefinition.dmn";

  @Override
  public DmnEngineConfiguration getDmnEngineConfiguration() {
    DefaultDmnEngineConfiguration configuration = new DefaultDmnEngineConfiguration();
    configuration.getTransformer().setDataTypeTransformerRegistry(new CustomDataTypeTransformerRegistry());
    configuration.enableFeelLegacyBehavior(true);
    return configuration;
  }

  @Test
  @DecisionResource(resource = DMN_OUTPUT_FILE)
  public void customOutputTransformer() {
    variables.put("output", 21);

    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry(CustomDataTypeTransformer.CUSTOM_OBJECT.getValue());
  }

  @Test
  @DecisionResource(resource = DMN_INPUT_FILE)
  public void customInputTransformer() {
    variables.put("input", 21);

    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry("isCustom");
  }

  protected static class CustomDataTypeTransformerRegistry implements DmnDataTypeTransformerRegistry {

    protected final DmnDataTypeTransformerRegistry defaultRegistry = new DefaultDataTypeTransformerRegistry();

    @Override
    public DmnDataTypeTransformer getTransformer(String typeName) {
      if (typeName.equals("custom")) {
        return new CustomDataTypeTransformer();
      } else {
        return defaultRegistry.getTransformer(typeName);
      }
    }

    @Override
    public void addTransformer(String typeName, DmnDataTypeTransformer transformer) {
      defaultRegistry.addTransformer(typeName, transformer);
    }
  }

  protected static class CustomDataTypeTransformer implements DmnDataTypeTransformer {

    protected static final TypedValue CUSTOM_OBJECT = Variables.integerValue(42);

    @Override
    public TypedValue transform(Object value) throws IllegalArgumentException {
      return CUSTOM_OBJECT;
    }
  }

}
