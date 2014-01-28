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
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.camunda.bpm.model.xml.type.attribute.Attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Sebastian Menski
 */
public class ModelElementTypeAssert extends AbstractAssert<ModelElementTypeAssert, ModelElementType> {

  protected ModelElementTypeAssert(ModelElementType actual) {
    super(actual, ModelElementTypeAssert.class);
  }

  public ModelElementTypeAssert isAbstract() {
    isNotNull();

    if (!actual.isAbstract()) {
      failWithMessage("Expected to be abstract but was not");
    }

    return this;
  }

  public ModelElementTypeAssert isNotAbstract() {
    isNotNull();

    if (actual.isAbstract()) {
      failWithMessage("Expected not to be abstract but was");
    }

    return this;
  }

  public ModelElementTypeAssert extendsType(ModelElementType type) {
    isNotNull();

    if (!actual.getBaseType().equals(type)) {
      failWithMessage("Expected to extend type <%s> but extends <%s>", actual.getBaseType(), type);
    }

    return this;
  }

  public ModelElementTypeAssert extendsNoType() {
    isNotNull();

    if (actual.getBaseType() != null) {
      failWithMessage("Expected to not extend any type but extends <%s>", actual.getBaseType());
    }

    return this;
  }

  public ModelElementTypeAssert hasAttributes() {
    isNotNull();

    if (actual.getAttributes().isEmpty()) {
      failWithMessage("Expected to have attributes but has none");
    }

    return this;
  }

  public ModelElementTypeAssert hasAttributes(String... names) {
    isNotNull();

    List<Attribute<?>> attributes = actual.getAttributes();
    List<String> attributeNames = new ArrayList<String>();
    for (Attribute<?> attribute : attributes) {
      attributeNames.add(attribute.getAttributeName());
    }

    if (!attributeNames.containsAll(Arrays.asList(names))) {
      failWithMessage("Expected to have attributes <%s> but has <%s>", names, attributeNames);
    }

    return this;
  }

  public ModelElementTypeAssert hasNoAttributes() {
    isNotNull();

    if (!actual.getAttributes().isEmpty()) {
      failWithMessage("Expected to have no attributes but has <%s>", actual.getAttributes());
    }

    return this;
  }

  public ModelElementTypeAssert hasChildElements() {
    isNotNull();

    if (actual.getChildElementTypes().isEmpty()) {
      failWithMessage("Expected to have child elements but has non");
    }

    return this;
  }

  public ModelElementTypeAssert hasChildElements(ModelElementType... types) {
    isNotNull();

    List<ModelElementType> childElementTypes = actual.getChildElementTypes();

    if (!childElementTypes.containsAll(Arrays.asList(types))) {
      failWithMessage("Expected to have child elements <%s> but has <%s>", types, childElementTypes);
    }

    return this;
  }

  public ModelElementTypeAssert hasNoChildElements() {
    isNotNull();

    if (!actual.getChildElementTypes().isEmpty()) {
      failWithMessage("Expected to have no child elements but has <%s>", actual.getAllChildElementTypes());
    }

    return this;
  }
}
