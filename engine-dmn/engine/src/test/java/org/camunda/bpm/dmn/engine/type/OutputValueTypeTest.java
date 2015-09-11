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

import org.camunda.bpm.dmn.engine.DmnEngineException;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnDecisionTest;
import org.junit.Test;

/**
 * @author Philipp Ossler
 */
public class OutputValueTypeTest extends DmnDecisionTest {

  private static final String DMN_FILE = "org/camunda/bpm/dmn/engine/type/OutputDefinition.dmn";

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void noOutputDefinition() {
    assertThat(engine).evaluates(decision, outputClause().type("none").value("a")).hasResult("a");
    assertThat(engine).evaluates(decision, outputClause().type("none").value(true)).hasResult(true);
    assertThat(engine).evaluates(decision, outputClause().type("none").value(4)).hasResult(4);
    assertThat(engine).evaluates(decision, outputClause().type("none").value(2L)).hasResult(2L);
    assertThat(engine).evaluates(decision, outputClause().type("none").value(4.2)).hasResult(4.2);
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void nullValue() {
    assertThat(engine).evaluates(decision, outputClause().type("string").value(null)).hasResult(null);
    assertThat(engine).evaluates(decision, outputClause().type("boolean").value(null)).hasResult(null);
    assertThat(engine).evaluates(decision, outputClause().type("integer").value(null)).hasResult(null);
    assertThat(engine).evaluates(decision, outputClause().type("long").value(null)).hasResult(null);
    assertThat(engine).evaluates(decision, outputClause().type("double").value(null)).hasResult(null);
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void stringOutputDefinition() {
    assertThat(engine).evaluates(decision, outputClause().type("string").value("a")).hasResult("a");
    assertThat(engine).evaluates(decision, outputClause().type("string").value(true)).hasResult("true");
    assertThat(engine).evaluates(decision, outputClause().type("string").value(4)).hasResult("4");
    assertThat(engine).evaluates(decision, outputClause().type("string").value(2L)).hasResult("2");
    assertThat(engine).evaluates(decision, outputClause().type("string").value(4.2)).hasResult("4.2");
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void booleanOutputDefinition() {
    assertThat(engine).evaluates(decision, outputClause().type("boolean").value(true)).hasResult(true);
    assertThat(engine).evaluates(decision, outputClause().type("boolean").value(false)).hasResult(false);

    assertThat(engine).evaluates(decision, outputClause().type("boolean").value("true")).hasResult(true);
    assertThat(engine).evaluates(decision, outputClause().type("boolean").value("false")).hasResult(false);
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void invalidValuesForBooleanOutputDefinition() {
    assertThat(engine)
      .evaluates(decision, outputClause().type("boolean").value("NaB"))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value 'NaB' for output clause with type 'boolean'");

    assertThat(engine)
      .evaluates(decision, outputClause().type("boolean").value(4))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value '4' for output clause with type 'boolean'");

    assertThat(engine)
      .evaluates(decision, outputClause().type("boolean").value(2L))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value '2' for output clause with type 'boolean'");

    assertThat(engine)
      .evaluates(decision, outputClause().type("boolean").value(4.2))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value '4.2' for output clause with type 'boolean'");
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void integerOutputDefinition() {
    assertThat(engine).evaluates(decision, outputClause().type("integer").value(4)).hasResult(4);

    assertThat(engine).evaluates(decision, outputClause().type("integer").value("4")).hasResult(4);
    assertThat(engine).evaluates(decision, outputClause().type("integer").value(2L)).hasResult(2);
    assertThat(engine).evaluates(decision, outputClause().type("integer").value(4.2)).hasResult(4);
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void invalidValuesForIntegerOutputDefinition() {
    assertThat(engine)
      .evaluates(decision, outputClause().type("integer").value("NaI"))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value 'NaI' for output clause with type 'integer'");

    assertThat(engine)
      .evaluates(decision, outputClause().type("integer").value(true))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value 'true' for output clause with type 'integer'");
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void longOutputDefinition() {
    assertThat(engine).evaluates(decision, outputClause().type("long").value(2L)).hasResult(2L);

    assertThat(engine).evaluates(decision, outputClause().type("long").value("2")).hasResult(2L);
    assertThat(engine).evaluates(decision, outputClause().type("long").value(4)).hasResult(4L);
    assertThat(engine).evaluates(decision, outputClause().type("long").value(4.2)).hasResult(4L);
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void invalidValuesForLongOutputDefinition() {
    assertThat(engine)
      .evaluates(decision, outputClause().type("long").value("NaL"))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value 'NaL' for output clause with type 'long'");

    assertThat(engine)
      .evaluates(decision, outputClause().type("long").value(true))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value 'true' for output clause with type 'long'");
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void doubleValueForDoubleOutputDefinition() {
    assertThat(engine).evaluates(decision, outputClause().type("double").value(4.2)).hasResult(4.2);

    assertThat(engine).evaluates(decision, outputClause().type("double").value("4.2")).hasResult(4.2);
    assertThat(engine).evaluates(decision, outputClause().type("double").value(4)).hasResult(4.0);
    assertThat(engine).evaluates(decision, outputClause().type("double").value(2L)).hasResult(2.0);
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void invalidValuesForDoubleOutputDefinition() {
    assertThat(engine)
      .evaluates(decision, outputClause().type("double").value("NaD"))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value 'NaD' for output clause with type 'double'");

    assertThat(engine)
      .evaluates(decision, outputClause().type("double").value(true))
      .thrown(DmnEngineException.class)
      .hasMessageContaining("Invalid value 'true' for output clause with type 'double'");
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void unsupportedOutputDefinition() {
    assertThat(engine).evaluates(decision, outputClause().type("custom").value(42)).hasResult(42);
  }

  protected OutputClauseBuilder outputClause() {
    return new OutputClauseBuilder();
  }

  protected class OutputClauseBuilder {
    public OutputVariableBuilder type(String typeName) {
      return new OutputVariableBuilder(typeName);
    }
  }

  protected class OutputVariableBuilder {
    protected String typeName;

    public OutputVariableBuilder(String typeName) {
      this.typeName = typeName;
    }

    public Map<String, Object> value(Object output) {
      Map<String, Object> variables = new HashMap<String, Object>();
      variables.put("type", typeName);
      variables.put("output", output);
      return variables;
    }
  }
}
