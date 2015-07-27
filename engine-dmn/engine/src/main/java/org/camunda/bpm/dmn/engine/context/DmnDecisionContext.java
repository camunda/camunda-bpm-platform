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

package org.camunda.bpm.dmn.engine.context;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnExpression;
import org.camunda.bpm.dmn.engine.DmnRule;
import org.camunda.bpm.dmn.engine.ScriptEngineResolver;

public interface DmnDecisionContext {

  void setVariableContext(DmnVariableContext variableContext);

  DmnVariableContext getVariableContext();

  DmnVariableContext getVariableContextChecked();

  void setScriptEngineResolver(ScriptEngineResolver scriptEngineResolver);

  ScriptEngineResolver getScriptEngineResolver();

  DmnDecisionResult evaluate(DmnDecision decision);

  boolean isApplicable(DmnRule rule);

  <T> T evaluate(DmnExpression expression);

  boolean isApplicable(DmnExpression expression);

  DmnDecisionOutput getOutput(DmnRule rule);

}
