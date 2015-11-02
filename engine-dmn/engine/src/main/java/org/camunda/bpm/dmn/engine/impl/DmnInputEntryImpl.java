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

package org.camunda.bpm.dmn.engine.impl;

import org.camunda.bpm.dmn.engine.DmnInput;
import org.camunda.bpm.dmn.engine.DmnInputEntry;

public class DmnInputEntryImpl extends DmnExpressionImpl implements DmnInputEntry {

  protected DmnInput input;

  public DmnInput getInput() {
    return input;
  }

  public void setInput(DmnInput input) {
    this.input = input;
  }

  public String toString() {
    String inputKey = null;
    if (input != null) {
      inputKey = input.getKey();
    }
    return "DmnClauseEntryImpl{" +
      "key='" + key + '\'' +
      ", name='" + name + '\'' +
      ", expressionLanguage='" + expressionLanguage + '\'' +
      ", expression='" + expression + '\'' +
      ", inputKey=" + inputKey +
      '}';
  }

}
