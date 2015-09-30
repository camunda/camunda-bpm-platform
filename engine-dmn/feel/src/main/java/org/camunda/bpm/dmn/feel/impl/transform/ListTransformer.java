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

package org.camunda.bpm.dmn.feel.impl.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListTransformer implements FeelToJuelTransformer {

  public boolean canTransform(String feelExpression) {
    return feelExpression.split(",").length > 1;
  }

  public String transform(FeelToJuelTransform transform, String feelExpression, String inputName) {
    List<String> expressions = collectExpressions(feelExpression);
    List<String> juelExpressions = transformExpressions(transform, expressions, inputName);
    return joinExpressions(juelExpressions);
  }

  protected List<String> collectExpressions(String feelExpression) {
    return Arrays.asList(feelExpression.split(","));
  }

  protected List<String> transformExpressions(FeelToJuelTransform transform, List<String> expressions, String inputName) {
    List<String> juelExpressions = new ArrayList<String>();
    for (String expression : expressions) {
      String juelExpression = transform.transformSimplePositiveUnaryTest(expression, inputName);
      juelExpressions.add(juelExpression);
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
