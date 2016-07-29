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

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionResultEntries;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Philipp Ossler
 */
public class MockDecisionResultEntriesBuilder {

  protected final MockDecisionResultBuilder mockDecisionResultBuilder;

  protected Map<String, TypedValue> entries = new HashMap<String, TypedValue>();

  public MockDecisionResultEntriesBuilder(MockDecisionResultBuilder mockDecisionResultBuilder) {
    this.mockDecisionResultBuilder = mockDecisionResultBuilder;
  }

  public MockDecisionResultEntriesBuilder entry(String key, TypedValue value) {
    entries.put(key, value);
    return this;
  }

  public MockDecisionResultBuilder endResultEntries() {
    SimpleDecisionResultEntries resultEntires = new SimpleDecisionResultEntries(entries);

    mockDecisionResultBuilder.addResultEntries(resultEntires);

    return mockDecisionResultBuilder;
  }

  public MockDecisionResultEntriesBuilder resultEntries() {
    return endResultEntries().resultEntries();
  }

  public DmnDecisionResult build() {
    return endResultEntries().build();
  }

  protected static class SimpleDecisionResultEntries extends HashMap<String, Object> implements DmnDecisionResultEntries {

    private static final long serialVersionUID = 1L;

    protected final Map<String, TypedValue> typedEntries;

    public SimpleDecisionResultEntries(Map<String, TypedValue> entries) {
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
