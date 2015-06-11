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

package org.camunda.dmn.engine.impl;

import java.util.ArrayList;
import java.util.List;

import org.camunda.dmn.engine.DmnDecisionOutput;
import org.camunda.dmn.engine.DmnDecisionResult;

public class DmnDecisionResultImpl implements DmnDecisionResult {

  protected List<DmnDecisionOutput> outputs = new ArrayList<DmnDecisionOutput>();

  public void setOutputs(List<DmnDecisionOutput> outputs) {
    this.outputs = outputs;
  }

  public List<DmnDecisionOutput> getOutputs() {
    return outputs;
  }

  public void addOutput(DmnDecisionOutput decisionOutput) {
    outputs.add(decisionOutput);
  }

}
