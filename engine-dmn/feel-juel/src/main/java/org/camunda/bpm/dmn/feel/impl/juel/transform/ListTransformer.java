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
package org.camunda.bpm.dmn.feel.impl.juel.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.dmn.feel.impl.juel.FeelEngineLogger;
import org.camunda.bpm.dmn.feel.impl.juel.FeelLogger;

public class ListTransformer implements FeelToJuelTransformer {

  public static final FeelEngineLogger LOG = FeelLogger.ENGINE_LOGGER;
  // regex to split by comma which does a positive look ahead to ignore commas enclosed in quotes
  public static final String COMMA_SEPARATOR_REGEX = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

  public boolean canTransform(String feelExpression) {
    return splitExpression(feelExpression).size() > 1;
  }

  public String transform(FeelToJuelTransform transform, String feelExpression, String inputName) {
    List<String> juelExpressions = transformExpressions(transform, feelExpression, inputName);
    return joinExpressions(juelExpressions);
  }

  protected List<String> collectExpressions(String feelExpression) {
    return splitExpression(feelExpression);
  }

  private List<String> splitExpression(String feelExpression) {
    return Arrays.asList(feelExpression.split(COMMA_SEPARATOR_REGEX, -1));
  }

  protected List<String> transformExpressions(FeelToJuelTransform transform, String feelExpression, String inputName) {
    List<String> expressions = collectExpressions(feelExpression);
    List<String> juelExpressions = new ArrayList<String>();
    for (String expression : expressions) {
      if (!expression.trim().isEmpty()) {
        String juelExpression = transform.transformSimplePositiveUnaryTest(expression, inputName);
        juelExpressions.add(juelExpression);
      }
      else {
        throw LOG.invalidListExpression(feelExpression);
      }
    }
    return juelExpressions;
  }

  protected String joinExpressions(List<String> juelExpressions) {
    StringBuilder builder = new StringBuilder();
    builder.append("(").append(juelExpressions.get(0)).append(")");
    for (int i = 1; i < juelExpressions.size(); i++) {
      builder.append(" || (").append(juelExpressions.get(i)).append(")");
    }
    return builder.toString();
  }

}
