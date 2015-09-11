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

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.impl.DmnEngineConfigurationImpl;
import org.camunda.bpm.dmn.engine.impl.type.DefaultDataTypeTransformerFactory;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Philipp Ossler
 */
public class CustomDataTypeTransformerFactoryTest {

  private static final String DMN_FILE = "org/camunda/bpm/dmn/engine/type/OutputDefinition.dmn";

  protected DmnEngine engine;
  protected DmnDecision decision;

  @Rule
  public DmnEngineRule dmnEngineRule = new DmnEngineRule(customConfiguration());

  @Before
  public void initEngineAndDecision() {
    engine = dmnEngineRule.getEngine();
    decision = dmnEngineRule.getDecision();
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void customTransformer() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("type", "custom");
    variables.put("output", 42);

    assertThat(engine)
      .evaluates(decision, variables)
      .hasResult(CustomDataTypeTransformer.CUSTOM_OBJECT);
  }

  protected static DmnEngineConfigurationImpl customConfiguration() {
    DmnEngineConfigurationImpl configuration = new DmnEngineConfigurationImpl();
    configuration.setDataTypeTransformerFactory(new CustomDataTypeTransformerFactory());
    return configuration;
  }

  protected static class CustomDataTypeTransformerFactory extends DefaultDataTypeTransformerFactory {

    @Override
    public DataTypeTransformer getTransformerForType(String typeName) {

      if(typeName.equals("custom")) {
        return new CustomDataTypeTransformer();
      } else {
        return super.getTransformerForType(typeName);
      }
    }
  }

  protected static class CustomDataTypeTransformer implements DataTypeTransformer {

    protected static final Object CUSTOM_OBJECT = "custom object";

    @Override
    public Object transform(Object value) throws IllegalArgumentException {
      return CUSTOM_OBJECT;
    }
  }

}
