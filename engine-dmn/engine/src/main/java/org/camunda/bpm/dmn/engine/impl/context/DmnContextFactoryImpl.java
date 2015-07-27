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

package org.camunda.bpm.dmn.engine.impl.context;

import org.camunda.bpm.dmn.engine.context.DmnContextFactory;
import org.camunda.bpm.dmn.engine.context.DmnDecisionContext;
import org.camunda.bpm.dmn.engine.context.DmnVariableContext;

public class DmnContextFactoryImpl implements DmnContextFactory {

  public DmnDecisionContext createDecisionContext() {
    DmnDecisionContextImpl decisionContext = new DmnDecisionContextImpl();
    decisionContext.setVariableContext(createVariableContext());
    return decisionContext;
  }

  public DmnVariableContext createVariableContext() {
    return new DmnVariableContextImpl();
  }

}
