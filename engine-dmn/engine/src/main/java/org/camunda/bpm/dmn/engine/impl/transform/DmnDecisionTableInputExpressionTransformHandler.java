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

import static org.camunda.bpm.dmn.engine.impl.transform.DmnExpressionTransformHelper.createTypeDefinition;
import static org.camunda.bpm.dmn.engine.impl.transform.DmnExpressionTransformHelper.getExpression;
import static org.camunda.bpm.dmn.engine.impl.transform.DmnExpressionTransformHelper.getExpressionLanguage;

import org.camunda.bpm.dmn.engine.impl.DmnExpressionImpl;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnElementTransformContext;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnElementTransformHandler;
import org.camunda.bpm.model.dmn.instance.InputExpression;

public class DmnDecisionTableInputExpressionTransformHandler implements DmnElementTransformHandler<InputExpression, DmnExpressionImpl> {

  public DmnExpressionImpl handleElement(DmnElementTransformContext context, InputExpression inputExpression) {
    return createFromInputExpression(context, inputExpression);
  }

  protected DmnExpressionImpl createFromInputExpression(DmnElementTransformContext context, InputExpression inputExpression) {
    DmnExpressionImpl dmnExpression = createDmnElement(context, inputExpression);

    dmnExpression.setId(inputExpression.getId());
    dmnExpression.setName(inputExpression.getLabel());
    dmnExpression.setTypeDefinition(createTypeDefinition(context, inputExpression));
    dmnExpression.setExpressionLanguage(getExpressionLanguage(context, inputExpression));
    dmnExpression.setExpression(getExpression(inputExpression));

    return dmnExpression;
  }

  protected DmnExpressionImpl createDmnElement(DmnElementTransformContext context, InputExpression inputExpression) {
    return new DmnExpressionImpl();
  }

}
