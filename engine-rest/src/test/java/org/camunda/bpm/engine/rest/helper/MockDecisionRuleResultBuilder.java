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

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Philipp Ossler
 */
public class MockDecisionRuleResultBuilder {

  protected final MockDecisionTableResultBuilder mockDecisionTableResultBuilder;

  protected Map<String, TypedValue> entries = new HashMap<String, TypedValue>();

  public MockDecisionRuleResultBuilder(MockDecisionTableResultBuilder mockDecisionTableResultBuilder) {
    this.mockDecisionTableResultBuilder = mockDecisionTableResultBuilder;
  }

  public MockDecisionRuleResultBuilder entry(String key, TypedValue value) {
    entries.put(key, value);
    return this;
  }

  public MockDecisionTableResultBuilder endRuleResult() {
    SimpleDecisionRuleResult ruleResult = new SimpleDecisionRuleResult(entries);

    mockDecisionTableResultBuilder.addRuleResult(ruleResult);

    return mockDecisionTableResultBuilder;
  }

  public MockDecisionRuleResultBuilder ruleResult() {
    return endRuleResult().ruleResult();
  }

  public DmnDecisionTableResult build() {
    return endRuleResult().build();
  }

  protected static class SimpleDecisionRuleResult extends HashMap<String, Object> implements DmnDecisionRuleResult {

    private static final long serialVersionUID = 1L;

    protected final Map<String, TypedValue> typedEntries;

    public SimpleDecisionRuleResult(Map<String, TypedValue> entries) {
      super(asEntryMap(entries));

      this.typedEntries = entries;
    }

    private static Map<? extends String, ?> asEntryMap(Map<String, TypedValue> typedValueMap) {
      Map<String, Object> entryMap = new HashMap<String, Object>();

      for(Map.Entry<String, TypedValue> entry : typedValueMap.entrySet()) {
        entryMap.put(entry.getKey(), entry.getValue().getValue());
      }

      return entryMap;
    }

    @Override
    public <T> T getFirstEntry() {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getSingleEntry() {
      throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getEntry(String name) {
      return (T) typedEntries.get(name).getValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends TypedValue> T getEntryTyped(String name) {
      return (T) typedEntries.get(name);
    }

    @Override
    public <T extends TypedValue> T getFirstEntryTyped() {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T extends TypedValue> T getSingleEntryTyped() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getEntryMap() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, TypedValue> getEntryMapTyped() {
      throw new UnsupportedOperationException();
    }

  }

}
