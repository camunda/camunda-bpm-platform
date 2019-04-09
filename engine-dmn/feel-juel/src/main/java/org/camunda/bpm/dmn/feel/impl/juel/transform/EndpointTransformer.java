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

import static org.camunda.bpm.dmn.feel.impl.juel.el.FeelFunctionMapper.JUEL_DATE_AND_TIME_METHOD;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EndpointTransformer implements FeelToJuelTransformer {

  public static final Pattern DATE_AND_TIME_PATTERN = Pattern.compile("^date and time\\((.+)\\)$");

  public boolean canTransform(String feelExpression) {
    return true;
  }

  public String transform(FeelToJuelTransform transform, String feelExpression, String inputName) {
    Matcher matcher = DATE_AND_TIME_PATTERN.matcher(feelExpression);
    if (matcher.matches()) {
      return JUEL_DATE_AND_TIME_METHOD + "(" + matcher.group(1) + ")";
    }
    else {
      return feelExpression;
    }
  }

}
