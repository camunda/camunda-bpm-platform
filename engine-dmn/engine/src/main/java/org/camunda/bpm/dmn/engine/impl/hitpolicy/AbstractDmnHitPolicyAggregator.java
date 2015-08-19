/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.dmn.engine.impl.hitpolicy;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.hitpolicy.DmnHitPolicyAggregator;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionOutputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionResultImpl;
import org.camunda.bpm.dmn.engine.impl.DmnLogger;

public abstract class AbstractDmnHitPolicyAggregator implements DmnHitPolicyAggregator {

  public final static DmnHitPolicyLogger LOG = DmnLogger.HIT_POLICY_LOGGER;

  protected List<Object> collectSingleValues(List<DmnDecisionOutput> decisionOutputs) {
    List<Object> values = new ArrayList<Object>();
    for (DmnDecisionOutput decisionOutput : decisionOutputs) {
      if (decisionOutput.isEmpty()) {
        continue; // skip empty output
      }
      else if (decisionOutput.size() == 1) {
        values.add(decisionOutput.getValue());
      }
      else {
        throw LOG.countAggregationNotApplicableOnCompoundOutput(decisionOutput);
      }
    }

    return values;
  }

  protected String getDecisionOutputName(List<DmnDecisionOutput> decisionOutputs) {
    for (DmnDecisionOutput decisionOutput : decisionOutputs) {
      if (!decisionOutput.isEmpty()) {
        return decisionOutput.keySet().iterator().next();
      }
    }
    return null;
  }

  protected DmnDecisionResult createAggregatedDecisionResult(String name, Object value) {
    DmnDecisionOutputImpl decisionOutput = new DmnDecisionOutputImpl();
    decisionOutput.put(name, value);
    DmnDecisionResultImpl decisionResult = new DmnDecisionResultImpl();
    decisionResult.add(decisionOutput);
    return decisionResult;
  }

}
