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

import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;

/**
 * @author Philipp Ossler
 */
public class MockDecisionResultBuilder {

  protected List<DmnDecisionOutput> decisionOutputs = new ArrayList<DmnDecisionOutput>();

  public MockDecisionOutputBuilder decisionOutput() {
    return new MockDecisionOutputBuilder(this);
  }

  public void addDecisionOutput(DmnDecisionOutput decisionOutput) {
    decisionOutputs.add(decisionOutput);
  }

  public DmnDecisionResult build() {
    SimpleDecisionResult decisionResult = new SimpleDecisionResult();
    decisionResult.addAll(decisionOutputs);
    return decisionResult;
  }

  protected class SimpleDecisionResult extends ArrayList<DmnDecisionOutput>implements DmnDecisionResult {

    private static final long serialVersionUID = 1L;

    @Override
    public DmnDecisionOutput getFirstOutput() {
      throw new UnsupportedOperationException();
    }

    @Override
    public DmnDecisionOutput getSingleOutput() {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T> List<T> collectOutputValues(String outputName) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<Map<String, Object>> getOutputList() {
      throw new UnsupportedOperationException();
    }

  }
}
