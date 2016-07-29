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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionResultEntries;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Philipp Ossler
 */
public class MockDecisionResultBuilder {

  protected List<DmnDecisionResultEntries> entries = new ArrayList<DmnDecisionResultEntries>();

  public MockDecisionResultEntriesBuilder resultEntries() {
    return new MockDecisionResultEntriesBuilder(this);
  }

  public void addResultEntries(DmnDecisionResultEntries resultEntries) {
    entries.add(resultEntries);
  }

  public DmnDecisionResult build() {
    SimpleDecisionResult decisionTableResult = new SimpleDecisionResult();
    decisionTableResult.addAll(entries);
    return decisionTableResult;
  }

  protected class SimpleDecisionResult extends ArrayList<DmnDecisionResultEntries> implements DmnDecisionResult {

    private static final long serialVersionUID = 1L;

    @Override
    public DmnDecisionResultEntries getFirstResult() {
      throw new UnsupportedOperationException();
    }

    @Override
    public DmnDecisionResultEntries getSingleResult() {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T> List<T> collectEntries(String outputName) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<Map<String, Object>> getResultList() {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getSingleEntry() {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T extends TypedValue> T getSingleEntryTyped() {
      throw new UnsupportedOperationException();
    }

  }
}
