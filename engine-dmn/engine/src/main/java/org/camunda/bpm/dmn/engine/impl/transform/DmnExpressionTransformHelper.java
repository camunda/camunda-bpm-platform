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

import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnElementTransformContext;
import org.camunda.bpm.dmn.engine.impl.spi.type.DmnDataTypeTransformer;
import org.camunda.bpm.dmn.engine.impl.spi.type.DmnTypeDefinition;
import org.camunda.bpm.dmn.engine.impl.type.DefaultTypeDefinition;
import org.camunda.bpm.dmn.engine.impl.type.DmnTypeDefinitionImpl;
import org.camunda.bpm.model.dmn.instance.InformationItem;
import org.camunda.bpm.model.dmn.instance.LiteralExpression;
import org.camunda.bpm.model.dmn.instance.Text;
import org.camunda.bpm.model.dmn.instance.UnaryTests;

public class DmnExpressionTransformHelper {

  public static DmnTypeDefinition createTypeDefinition(DmnElementTransformContext context, LiteralExpression expression) {
    return createTypeDefinition(context, expression.getTypeRef());
  }

  public static DmnTypeDefinition createTypeDefinition(DmnElementTransformContext context, InformationItem informationItem) {
    return createTypeDefinition(context, informationItem.getTypeRef());
  }

  protected static DmnTypeDefinition createTypeDefinition(DmnElementTransformContext context, String typeRef) {
    if (typeRef != null) {
      DmnDataTypeTransformer transformer = context.getDataTypeTransformerRegistry().getTransformer(typeRef);
      return new DmnTypeDefinitionImpl(typeRef, transformer);
    }
    else {
      return new DefaultTypeDefinition();
    }
  }

  public static String getExpressionLanguage(DmnElementTransformContext context, LiteralExpression expression) {
    return getExpressionLanguage(context, expression.getExpressionLanguage());
  }

  public static String getExpressionLanguage(DmnElementTransformContext context, UnaryTests expression) {
    return getExpressionLanguage(context, expression.getExpressionLanguage());
  }

  protected static String getExpressionLanguage(DmnElementTransformContext context, String expressionLanguage) {
    if (expressionLanguage != null) {
      return expressionLanguage;
    }
    else {
      return getGlobalExpressionLanguage(context);
    }
  }

  protected static String getGlobalExpressionLanguage(DmnElementTransformContext context) {
    String expressionLanguage = context.getModelInstance().getDefinitions().getExpressionLanguage();
    if (!DefaultDmnEngineConfiguration.FEEL_EXPRESSION_LANGUAGE.equals(expressionLanguage) &&
        !DefaultDmnEngineConfiguration.FEEL_EXPRESSION_LANGUAGE_DMN12.equals(expressionLanguage) &&
        !DefaultDmnEngineConfiguration.FEEL_EXPRESSION_LANGUAGE_DMN13.equals(expressionLanguage)) {
      return expressionLanguage;
    }
    else {
      return null;
    }
  }

  public static String getExpression(LiteralExpression expression) {
    return getExpression(expression.getText());
  }

  public static String getExpression(UnaryTests expression) {
    return getExpression(expression.getText());
  }

  protected static String getExpression(Text text) {
    if (text != null) {
      String textContent = text.getTextContent();
      if (textContent != null && !textContent.isEmpty()) {
        return textContent;
      }
    }
    return null;
  }


}
