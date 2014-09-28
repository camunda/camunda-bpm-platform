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
package org.camunda.bpm.engine.rest.helper.variable;

import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

/**
 * @author Thorben Lindhauer
 *
 */
public class EqualsNullValue extends ArgumentMatcher<TypedValue> {

  public boolean matches(Object argument) {
    if (argument == null || !TypedValue.class.isAssignableFrom(argument.getClass())) {
      return false;
    }

    TypedValue typedValue = (TypedValue) argument;

    if (typedValue.getType() != ValueType.NULL) {
      return false;
    }

    if (typedValue.getValue() != null) {
      return false;
    }

    return true;
  }

  public static EqualsNullValue matcher() {
    return new EqualsNullValue();
  }

  public void describeTo(Description description) {
    StringBuilder sb = new StringBuilder();
    sb.append(this.getClass().getSimpleName());

    description.appendText(sb.toString());
  }

}
