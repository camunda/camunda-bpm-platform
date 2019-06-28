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
package org.camunda.bpm.dmn.feel.impl.custom;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.camunda.bpm.dmn.feel.impl.juel.transform.FeelToJuelFunctionTransformer;
import org.camunda.bpm.dmn.feel.impl.juel.transform.FeelToJuelTransform;

public class StartsWithFunctionTransformer implements FeelToJuelFunctionTransformer {

  public static final Pattern STARTS_WITH_PATTERN = Pattern.compile("^starts with\\((.+)\\)$");

  public static final String JUEL_STARTS_WITH = "startsWith";

  protected static Method method;

  static {
    try {
      method  = StartsWithFunctionTransformer.class.getMethod(JUEL_STARTS_WITH, String.class, String.class);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public boolean canTransform(String feelExpression) {
    Matcher startsWithMatcher   = STARTS_WITH_PATTERN.matcher(feelExpression);

    return startsWithMatcher.matches();
  }

  @Override
  public String transform(FeelToJuelTransform transform, String feelExpression, String inputName) {
    Matcher startsWithMatcher   = STARTS_WITH_PATTERN.matcher(feelExpression);

    if (startsWithMatcher.matches()) {
      return JUEL_STARTS_WITH + "(" + inputName + ", " + startsWithMatcher.group(1) + ")";
    } else {
      return feelExpression;
    }
  }

  public static boolean startsWith(final String input, final String match) {
    if (input != null) {
      return input.startsWith(match);
    }
    return false;
  }

  @Override
  public String getName() {
    return JUEL_STARTS_WITH;
  }

  @Override
  public Method getMethod()
  {
    return method;
  }

}
