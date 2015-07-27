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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnClause;
import org.camunda.bpm.dmn.engine.DmnClauseEntry;
import org.camunda.bpm.dmn.engine.DmnExpression;

public class DmnClauseImpl extends DmnElementImpl implements DmnClause {

  public static final String DEFAULT_INPUT_VARIABLE_NAME = "cellInput";

  protected DmnExpression inputExpression;
  protected String outputName;
  protected boolean isOrdered = false;

  protected List<DmnClauseEntry> inputEntries = new ArrayList<DmnClauseEntry>();
  protected List<DmnExpression> outputEntries = new ArrayList<DmnExpression>();

  public DmnExpression getInputExpression() {
    return inputExpression;
  }

  public void setInputExpression(DmnExpression inputExpression) {
    this.inputExpression = inputExpression;
  }

  public String getOutputName() {
    return outputName;
  }

  public void setOutputName(String outputName) {
    this.outputName = outputName;
  }

  public boolean isOrdered() {
    return isOrdered;
  }

  public void setIsOrdered(boolean isOrdered) {
    this.isOrdered = isOrdered;
  }

  public List<DmnClauseEntry> getInputEntries() {
    return inputEntries;
  }

  public void setInputEntries(List<DmnClauseEntry> inputEntries) {
    this.inputEntries = inputEntries;
  }

  public void addInputEntry(DmnClauseEntry inputEntry) {
    inputEntries.add(inputEntry);
  }

  public List<DmnExpression> getOutputEntries() {
    return outputEntries;
  }

  public void setOutputEntries(List<DmnExpression> outputEntries) {
    this.outputEntries = outputEntries;
  }

  public void addOutputEntry(DmnExpression outputEntry) {
    outputEntries.add(outputEntry);
  }

  public String toString() {
    return "DmnClauseImpl{" +
      "key='" + key + '\'' +
      ", name='" + name + '\'' +
      ", inputExpression=" + inputExpression +
      ", outputName='" + outputName + '\'' +
      ", isOrdered=" + isOrdered +
      ", inputEntries=" + inputEntries +
      ", outputEntries=" + outputEntries +
      "} ";
  }

}
