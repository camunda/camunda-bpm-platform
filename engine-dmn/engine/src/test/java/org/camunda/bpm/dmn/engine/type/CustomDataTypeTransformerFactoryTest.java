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

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DmnEngineConfigurationImpl;
import org.camunda.bpm.dmn.engine.impl.type.DefaultDataTypeTransformerFactory;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnDecisionTest;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.junit.Test;

/**
 * @author Philipp Ossler
 */
public class CustomDataTypeTransformerFactoryTest extends DmnDecisionTest {

  protected static final String DMN_INPUT_FILE = "org/camunda/bpm/dmn/engine/type/CustomInputDefinition.dmn";
  protected static final String DMN_OUTPUT_FILE = "org/camunda/bpm/dmn/engine/type/CustomOutputDefinition.dmn";

  @Override
  public DmnEngineConfiguration createDmnEngineConfiguration() {
    DmnEngineConfigurationImpl configuration = new DmnEngineConfigurationImpl();
    configuration.setDataTypeTransformerFactory(new CustomDataTypeTransformerFactory());
    return configuration;
  }

  @Test
  @DecisionResource(resource = DMN_OUTPUT_FILE)
  public void customOutputTransformer() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("output", 21);

    assertThat(engine).evaluates(decision, variables).hasResultValue(CustomDataTypeTransformer.CUSTOM_OBJECT.getValue());
  }

  @Test
  @DecisionResource(resource = DMN_INPUT_FILE)
  public void customInputTransformer() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input", 21);

    assertThat(engine).evaluates(decision, variables).hasResultValue("isCustom");
  }

  protected static class CustomDataTypeTransformerFactory implements DataTypeTransformerFactory {

    protected final DataTypeTransformerFactory defaultFactory = new DefaultDataTypeTransformerFactory();

    @Override
    public DataTypeTransformer getTransformerForType(String typeName) {

      if (typeName.equals("custom")) {
        return new CustomDataTypeTransformer();
      } else {
        return defaultFactory.getTransformerForType(typeName);
      }
    }
  }

  protected static class CustomDataTypeTransformer implements DataTypeTransformer {

    protected static final TypedValue CUSTOM_OBJECT = Variables.integerValue(42);

    @Override
    public TypedValue transform(Object value) throws IllegalArgumentException {
      return CUSTOM_OBJECT;
    }
  }

}
