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

package org.camunda.bpm.engine.impl.bpmn.parser;

import java.util.List;

import org.camunda.bpm.engine.impl.el.Expression;

public class FailedJobParseRetryConf {

  private boolean hasIntervals;
  private Expression retryCycle;
  private List<String> retryIntervals;

  public FailedJobParseRetryConf(Expression retryCycle) {
    this.retryCycle = retryCycle;
  }

  public FailedJobParseRetryConf(List<String> retryIntervals) {
    this.retryIntervals = retryIntervals;
    if (retryIntervals != null && !retryIntervals.isEmpty()) {
      hasIntervals = true;
    }
  }

  public boolean hasIntervals() {
    return hasIntervals;
  }

  public Expression getRetryCycle() {
    return retryCycle;
  }

  public List<String> getRetryIntervals() {
    return retryIntervals;
  }

}
