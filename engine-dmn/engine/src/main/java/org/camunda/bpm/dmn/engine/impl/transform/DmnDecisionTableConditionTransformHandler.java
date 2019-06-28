/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.dmn.engine.impl.transform;

import static org.camunda.bpm.dmn.engine.impl.transform.DmnExpressionTransformHelper.getExpression;
import static org.camunda.bpm.dmn.engine.impl.transform.DmnExpressionTransformHelper.getExpressionLanguage;

import org.camunda.bpm.dmn.engine.impl.DmnExpressionImpl;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnElementTransformContext;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnElementTransformHandler;
import org.camunda.bpm.model.dmn.instance.InputEntry;

public class DmnDecisionTableConditionTransformHandler implements DmnElementTransformHandler<InputEntry, DmnExpressionImpl> {

  public DmnExpressionImpl handleElement(DmnElementTransformContext context, InputEntry inputEntry) {
    return createFromInputEntry(context, inputEntry);
  }

  protected DmnExpressionImpl createFromInputEntry(DmnElementTransformContext context, InputEntry inputEntry) {
    DmnExpressionImpl condition = createDmnElement(context, inputEntry);

    condition.setId(inputEntry.getId());
    condition.setName(inputEntry.getLabel());
    condition.setExpressionLanguage(getExpressionLanguage(context, inputEntry));
    condition.setExpression(getExpression(inputEntry));

    return condition;
  }

  protected DmnExpressionImpl createDmnElement(DmnElementTransformContext context, InputEntry inputEntry) {
    return new DmnExpressionImpl();
  }

}
