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

package org.camunda.bpm.model.xml.test.assertions;

import org.assertj.core.api.AbstractAssert;
import org.camunda.bpm.model.xml.type.attribute.Attribute;

/**
 * @author Sebastian Menski
 */
public class AttributeAssert extends AbstractAssert<AttributeAssert, Attribute<?>> {

  protected AttributeAssert(Attribute<?> actual) {
    super(actual, AttributeAssert.class);
  }

  public AttributeAssert isRequired() {
    isNotNull();

    if (!actual.isRequired()) {
      failWithMessage("Expected attribute <%s> to be required but was not", actual.getAttributeName());
    }

    return this;
  }

  public AttributeAssert isOptional() {
    isNotNull();

    if (actual.isRequired()) {
      failWithMessage("Expected attribute <%s> to be optional but was required", actual.getAttributeName());
    }

    return this;
  }

  public AttributeAssert isIdAttribute() {
    isNotNull();

    if (!actual.isIdAttribute()) {
      failWithMessage("Expected attribute <%s> to be an ID attribute but was not", actual.getAttributeName());
    }

    return this;
  }

  public AttributeAssert isNotIdAttribute() {
    isNotNull();

    if (actual.isIdAttribute()) {
      failWithMessage("Expected attribute <%s> to be not an ID attribute but was", actual.getAttributeName());
    }

    return this;
  }

  public AttributeAssert hasDefaultValue(Object value) {
    isNotNull();

    Object defaultValue = actual.getDefaultValue();

    if (!value.equals(defaultValue)) {
      failWithMessage("Expected attribute <%s> to have default value <%s> but was <%s>", actual.getAttributeName(), value, defaultValue);
    }

    return this;
  }

  public AttributeAssert hasNoDefaultValue() {
    isNotNull();

    Object defaultValue = actual.getDefaultValue();

    if (defaultValue != null) {
      failWithMessage("Expected attribute <%s> to have no default value but was <%s>", actual.getAttributeName(), defaultValue);
    }

    return this;
  }

}
