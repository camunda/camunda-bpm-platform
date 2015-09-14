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

import org.camunda.bpm.dmn.engine.type.DataTypeTransformer;

/**
 * Transform values of type {@link Number} and {@link String} into {@link Long}.
 *
 * @author Philipp Ossler
 *
 */
public class LongDataTypeTransformer implements DataTypeTransformer {

  @Override
  public Object transform(Object value) throws IllegalArgumentException {
    if (value instanceof Number) {
      return transformNumber((Number) value);

    } else if (value instanceof String) {
      return transformString((String) value);

    } else {
      throw new IllegalArgumentException();
    }
  }

  protected Long transformNumber(Number value) {
    if(isLong(value)) {
      return value.longValue();
    } else {
      throw new IllegalArgumentException();
    }
  }

  protected boolean isLong(Number value) {
    double doubleValue = value.doubleValue();
    return doubleValue == (long) doubleValue;
  }

  protected Long transformString(String value) {
    return Long.parseLong(value);
  }

}
