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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.camunda.bpm.dmn.feel.impl.juel.FeelEngineLogger;
import org.camunda.bpm.dmn.feel.impl.juel.FeelLogger;

public class NotTransformer implements FeelToJuelTransformer {

  public static final FeelEngineLogger LOG = FeelLogger.ENGINE_LOGGER;
  public static final Pattern NOT_PATTERN = Pattern.compile("^not\\((.+)\\)$");

  public boolean canTransform(String feelExpression) {
    return feelExpression.startsWith("not(");
  }

  public String transform(FeelToJuelTransform transform, String feelExpression, String inputName) {
    String simplePositiveUnaryTests = extractInnerExpression(feelExpression);
    String juelExpression = transform.transformSimplePositiveUnaryTests(simplePositiveUnaryTests, inputName);
    return "not(" + juelExpression + ")";
  }

  public String extractInnerExpression(String feelExpression) {
    Matcher matcher = NOT_PATTERN.matcher(feelExpression);
    if (matcher.matches()) {
      return matcher.group(1);
    }
    else {
      throw LOG.invalidNotExpression(feelExpression);
    }
  }

}
