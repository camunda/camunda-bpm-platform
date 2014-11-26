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
package org.camunda.bpm.engine.test.cmmn.listener;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.CaseVariableListener;
import org.camunda.bpm.engine.delegate.DelegateCaseVariableInstance;
import org.camunda.bpm.engine.delegate.Expression;

/**
 * @author Thorben Lindhauer
 *
 */
public class LogInjectedValuesListener implements CaseVariableListener {

  protected Expression stringValueExpression;
  protected Expression juelExpression;

  protected static List<Object> resolvedStringValueExpressions = new ArrayList<Object>();
  protected static List<Object> resolvedJuelExpressions = new ArrayList<Object>();


  public void notify(DelegateCaseVariableInstance variableInstance) throws Exception {
    resolvedJuelExpressions.add(juelExpression.getValue(variableInstance.getSourceExecution()));
    resolvedStringValueExpressions.add(stringValueExpression.getValue(variableInstance.getSourceExecution()));
  }

  public static List<Object> getResolvedStringValueExpressions() {
    return resolvedStringValueExpressions;
  }

  public static List<Object> getResolvedJuelExpressions() {
    return resolvedJuelExpressions;
  }

  public static void reset() {
    resolvedJuelExpressions = new ArrayList<Object>();
    resolvedStringValueExpressions = new ArrayList<Object>();
  }

}
