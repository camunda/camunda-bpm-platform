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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnEngineException;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnDecisionTest;
import org.junit.Test;

/**
 * @author Philipp Ossler
 */
public class InputValueTypeTest extends DmnDecisionTest {

  private static final String DMN_FILE = "org/camunda/bpm/dmn/engine/type/InputDefinition.dmn";

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void noInputDefinition() {
    assertThat(engine).evaluates(decision, inputClause().type("none").value("a")).hasResult("isOriginal");
    assertThat(engine).evaluates(decision, inputClause().type("none").value(true)).hasResult("isOriginal");
    assertThat(engine).evaluates(decision, inputClause().type("none").value(4)).hasResult("isOriginal");
    assertThat(engine).evaluates(decision, inputClause().type("none").value(2L)).hasResult("isOriginal");
    assertThat(engine).evaluates(decision, inputClause().type("none").value(4.2)).hasResult("isOriginal");
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void nullValue() {
    assertThat(engine).evaluates(decision, inputClause().type("string").value(null)).hasResult("isNull");
    assertThat(engine).evaluates(decision, inputClause().type("boolean").value(null)).hasResult("isNull");
    assertThat(engine).evaluates(decision, inputClause().type("integer").value(null)).hasResult("isNull");
    assertThat(engine).evaluates(decision, inputClause().type("long").value(null)).hasResult("isNull");
    assertThat(engine).evaluates(decision, inputClause().type("double").value(null)).hasResult("isNull");
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void stringInputDefinition() {
    assertThat(engine).evaluates(decision, inputClause().type("string").value("a")).hasResult("isString");
    assertThat(engine).evaluates(decision, inputClause().type("string").value(true)).hasResult("isString");
    assertThat(engine).evaluates(decision, inputClause().type("string").value(4)).hasResult("isString");
    assertThat(engine).evaluates(decision, inputClause().type("string").value(2L)).hasResult("isString");
    assertThat(engine).evaluates(decision, inputClause().type("string").value(4.2)).hasResult("isString");
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void booleanInputDefinition() {
    assertThat(engine).evaluates(decision, inputClause().type("boolean").value(true)).hasResult("isBoolean");
    assertThat(engine).evaluates(decision, inputClause().type("boolean").value(false)).hasResult("isBoolean");

    assertThat(engine).evaluates(decision, inputClause().type("boolean").value("true")).hasResult("isBoolean");
    assertThat(engine).evaluates(decision, inputClause().type("boolean").value("false")).hasResult("isBoolean");
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void invalidValuesForBooleanInputDefinition() {
    assertThat(engine)
      .evaluates(decision, inputClause().type("boolean").value("NaB"))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value 'NaB' for output clause with type 'boolean'");

    assertThat(engine)
      .evaluates(decision, inputClause().type("boolean").value(4))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value '4' for output clause with type 'boolean'");

    assertThat(engine)
      .evaluates(decision, inputClause().type("boolean").value(2L))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value '2' for output clause with type 'boolean'");

    assertThat(engine)
      .evaluates(decision, inputClause().type("boolean").value(4.2))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value '4.2' for output clause with type 'boolean'");
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void integerInputDefinition() {
    assertThat(engine).evaluates(decision, inputClause().type("integer").value(4)).hasResult("isInteger");

    assertThat(engine).evaluates(decision, inputClause().type("integer").value("4")).hasResult("isInteger");
    assertThat(engine).evaluates(decision, inputClause().type("integer").value(2L)).hasResult("isInteger");
    assertThat(engine).evaluates(decision, inputClause().type("integer").value(4.0)).hasResult("isInteger");
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void invalidValuesForIntegerInputDefinition() {
    assertThat(engine)
      .evaluates(decision, inputClause().type("integer").value("NaI"))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value 'NaI' for output clause with type 'integer'");

    assertThat(engine)
      .evaluates(decision, inputClause().type("integer").value(true))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value 'true' for output clause with type 'integer'");

    assertThat(engine)
      .evaluates(decision, inputClause().type("integer").value(4.2))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value '4.2' for output clause with type 'integer'");

    assertThat(engine)
      .evaluates(decision, inputClause().type("integer").value(Long.MAX_VALUE))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value '" + Long.MAX_VALUE + "' for output clause with type 'integer'");
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void longInputDefinition() {
    assertThat(engine).evaluates(decision, inputClause().type("long").value(2L)).hasResult("isLong");

    assertThat(engine).evaluates(decision, inputClause().type("long").value("2")).hasResult("isLong");
    assertThat(engine).evaluates(decision, inputClause().type("long").value(4)).hasResult("isLong");
    assertThat(engine).evaluates(decision, inputClause().type("long").value(4.0)).hasResult("isLong");
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void invalidValuesForLongInputDefinition() {
    assertThat(engine)
      .evaluates(decision, inputClause().type("long").value("NaL"))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value 'NaL' for output clause with type 'long'");

    assertThat(engine)
      .evaluates(decision, inputClause().type("long").value(true))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value 'true' for output clause with type 'long'");

    assertThat(engine)
      .evaluates(decision, inputClause().type("long").value(4.2))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value '4.2' for output clause with type 'long'");
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void doubleValueForDoubleInputDefinition() {
    assertThat(engine).evaluates(decision, inputClause().type("double").value(4.2)).hasResult("isDouble");

    assertThat(engine).evaluates(decision, inputClause().type("double").value("4.2")).hasResult("isDouble");
    assertThat(engine).evaluates(decision, inputClause().type("double").value(4)).hasResult("isDouble");
    assertThat(engine).evaluates(decision, inputClause().type("double").value(2L)).hasResult("isDouble");
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void invalidValuesForDoubleInputDefinition() {
    assertThat(engine)
      .evaluates(decision, inputClause().type("double").value("NaD"))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value 'NaD' for output clause with type 'double'");

    assertThat(engine)
      .evaluates(decision, inputClause().type("double").value(true))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value 'true' for output clause with type 'double'");
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void unsupportedInputDefinition() {
    assertThat(engine).evaluates(decision, inputClause().type("custom").value(21)).hasResult("isOriginal");
  }

  protected InputClauseBuilder inputClause() {
    return new InputClauseBuilder();
  }

  protected class InputClauseBuilder {
    public InputVariableBuilder type(String typeName) {
      return new InputVariableBuilder(typeName);
    }
  }

  protected class InputVariableBuilder {
    protected List<String> types = Arrays.asList("string", "boolean", "integer", "long", "double", "custom", "none");

    protected String typeName;

    public InputVariableBuilder(String typeName) {
      this.typeName = typeName;
    }

    public Map<String, Object> value(Object input) {
      Map<String, Object> variables = new HashMap<String, Object>();
      variables.put("type", typeName);
      variables.put("input", input);

      for(String type : types) {
        if(type.equals(typeName)) {
          variables.put(type + "Input", input);
        } else {
          variables.put(type + "Input", null);
        }
      }

      return variables;
    }
  }
}
