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
      return "dateTime(" + matcher.group(1) + ")";
    }
    else {
      return feelExpression;
    }
  }

}
