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
package org.camunda.bpm.engine.rest.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Philipp Ossler
 */
public class MockDecisionOutputBuilder {

  protected final MockDecisionResultBuilder mockDecisionResultBuilder;

  protected Map<String, TypedValue> outputValues = new HashMap<String, TypedValue>();

  public MockDecisionOutputBuilder(MockDecisionResultBuilder mockDecisionResultBuilder) {
    this.mockDecisionResultBuilder = mockDecisionResultBuilder;
  }

  public MockDecisionOutputBuilder output(String key, TypedValue value) {
    outputValues.put(key, value);
    return this;
  }

  public MockDecisionResultBuilder endDecisionOutput() {
    SimpleDecisionOutput decisionOutput = new SimpleDecisionOutput(outputValues);

    mockDecisionResultBuilder.addDecisionOutput(decisionOutput);

    return mockDecisionResultBuilder;
  }

  public MockDecisionOutputBuilder decisionOutput() {
    return endDecisionOutput().decisionOutput();
  }

  public DmnDecisionResult build() {
    return endDecisionOutput().build();
  }

  protected class SimpleDecisionOutput implements DmnDecisionOutput {

    private static final long serialVersionUID = 1L;

    protected final Map<String, TypedValue> outputValues;

    public SimpleDecisionOutput(Map<String, TypedValue> outputValues) {
      this.outputValues = outputValues;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue(String name) {
      return (T) outputValues.get(name).getValue();
    }

    @Override
    public <T> T getFirstValue() {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getSingleValue() {
      throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends TypedValue> T getValueTyped(String name) {
      return (T) outputValues.get(name);
    }

    @Override
    public <T extends TypedValue> T getFirstValueTyped() {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T extends TypedValue> T getSingleValueTyped() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsKey(String key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Collection<TypedValue> valuesTyped() {
      return outputValues.values();
    }

    @Override
    public int size() {
      return outputValues.size();
    }

    @Override
    public boolean isEmpty() {
      return outputValues.isEmpty();
    }

    @Override
    public Set<String> keySet() {
      return outputValues.keySet();
    }

    @Override
    public Collection<Object> values() {
      List<Object> values = new LinkedList<Object>();

      for(TypedValue typedValue : outputValues.values()) {
        values.add(typedValue.getValue());
      }
      return values();
    }

  }

}
