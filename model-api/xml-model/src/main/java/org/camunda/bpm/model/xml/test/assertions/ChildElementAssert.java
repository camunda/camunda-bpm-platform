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
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;

/**
 * @author Sebastian Menski
 */
public class ChildElementAssert extends AbstractAssert<ChildElementAssert, ChildElementCollection<?>> {

  protected ChildElementAssert(ChildElementCollection<?> actual) {
    super(actual, ChildElementAssert.class);
  }

  public ChildElementAssert minOccurs(int occurs) {
    isNotNull();

    int minOccurs = actual.getMinOccurs();

    if (minOccurs != occurs) {
      failWithMessage("Expected child element <%s> to have a min occurs of <%s> but was <%s>", actual.getChildElementTypeClass(), occurs, minOccurs);
    }

    return this;
  }

  public ChildElementAssert maxOccurs(int occurs) {
    isNotNull();

    int maxOccurs = actual.getMaxOccurs();

    if (maxOccurs != occurs) {
      failWithMessage("Expected child element <%s> to have a max occurs of <%s> but was <%s>", actual.getChildElementTypeClass(), occurs, maxOccurs);
    }

    return this;
  }

  public ChildElementAssert isOptional() {
    isNotNull();

    int minOccurs = actual.getMinOccurs();

    if (minOccurs != 0) {
      failWithMessage("Expected child element <%s> to be optional but has min occurs of <%s>", actual.getChildElementTypeClass(), minOccurs);
    }

    return this;
  }

  public ChildElementAssert isUnbounded() {
    isNotNull();

    int maxOccurs = actual.getMaxOccurs();

    if (maxOccurs != -1) {
      failWithMessage("Expected child element <%s> to be unbounded but has a max occurs of <%s>", actual.getChildElementTypeClass(), maxOccurs);
    }

    return this;
  }

}
