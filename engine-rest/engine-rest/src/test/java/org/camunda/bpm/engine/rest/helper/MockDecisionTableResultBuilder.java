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

import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;

/**
 * @author Philipp Ossler
 */
public class MockDecisionTableResultBuilder {

  protected List<DmnDecisionRuleResult> ruleResults = new ArrayList<DmnDecisionRuleResult>();

  public MockDecisionRuleResultBuilder ruleResult() {
    return new MockDecisionRuleResultBuilder(this);
  }

  public void addRuleResult(DmnDecisionRuleResult ruleResult) {
    ruleResults.add(ruleResult);
  }

  public DmnDecisionTableResult build() {
    SimpleDecisionTableResult decisionTableResult = new SimpleDecisionTableResult();
    decisionTableResult.addAll(ruleResults);
    return decisionTableResult;
  }

  protected class SimpleDecisionTableResult extends ArrayList<DmnDecisionRuleResult> implements DmnDecisionTableResult {

    private static final long serialVersionUID = 1L;

    @Override
    public DmnDecisionRuleResult getFirstResult() {
      throw new UnsupportedOperationException();
    }

    @Override
    public DmnDecisionRuleResult getSingleResult() {
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

  }
}
