/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.dmn.engine.impl.type;

import java.util.regex.Pattern;

import org.camunda.bpm.dmn.engine.type.DataTypeTransformer;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.PeriodValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Transform values of type {@link String} into duration as {@link PeriodValue}.
 * A String should have the format {@code PnYnMnDTnHnMnS}.
 *
 * @author Philipp Ossler
 */
public class DurationDataTypeTransformer implements DataTypeTransformer {

  protected Pattern periodPattern = Pattern.compile("P(\\d+Y)?(\\d+M)?(\\d+D)?(T(\\d+H)?(\\d+M)?(\\d+S)?)?");

  @Override
  public TypedValue transform(Object value) throws IllegalArgumentException {
    if (value instanceof String) {
      String period = (String) value;
      validatePeriod(period);
      return Variables.periodValue(period);

    } else {
      throw new IllegalArgumentException();
    }
  }

  protected void validatePeriod(String period) throws IllegalArgumentException {
    if (!matchesPattern(period) || isEmptyPeriod(period)) {
      throw new IllegalArgumentException();
    }
  }

  protected boolean matchesPattern(String period) {
    return periodPattern.matcher(period).matches();
  }

  protected boolean isEmptyPeriod(String period) {
    return period.equals("P") || period.equals("PT");
  }

}
