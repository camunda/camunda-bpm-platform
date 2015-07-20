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

package org.camunda.dmn.engine.impl.handler;

import org.camunda.bpm.model.dmn.instance.Clause;
import org.camunda.dmn.engine.handler.DmnElementHandlerContext;
import org.camunda.dmn.engine.impl.DmnClauseImpl;

public class DmnClauseHandler extends AbstractDmnElementHandler<Clause, DmnClauseImpl> {

  protected DmnClauseImpl createElement(DmnElementHandlerContext context, Clause clause) {
    return new DmnClauseImpl();
  }

  protected void initElement(DmnElementHandlerContext context, Clause clause, DmnClauseImpl dmnClause) {
    super.initElement(context, clause, dmnClause);
    initIsOrdered(context, clause, dmnClause);
    initOutputName(context, clause, dmnClause);
  }

  protected void initIsOrdered(DmnElementHandlerContext context, Clause clause, DmnClauseImpl dmnClause) {
    dmnClause.setIsOrdered(clause.isOrdered());
  }

  protected void initOutputName(DmnElementHandlerContext context, Clause clause, DmnClauseImpl dmnClause) {
    dmnClause.setOutputName(clause.getCamundaOutput());
  }

}
