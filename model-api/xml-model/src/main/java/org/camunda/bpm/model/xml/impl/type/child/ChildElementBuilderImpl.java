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
package org.camunda.bpm.model.xml.impl.type.child;

import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.ChildElementBuilder;
import org.camunda.bpm.model.xml.type.ModelElementType;

/**
 * @author Daniel Meyer
 *
 */
public class ChildElementBuilderImpl<T extends ModelElementInstance> extends ChildElementCollectionBuilderImpl<T> implements ChildElementBuilder<T> {

  public ChildElementBuilderImpl(Class<T> childElementType, String localName, String namespaceUri, ModelElementType containingType) {
    super(childElementType, localName, namespaceUri, containingType);
  }

  public ChildElementBuilderImpl(Class<T> childElementType, ModelElementType containingType) {
    super(childElementType, containingType);
  }

  public ChildElement<T> build() {
    return (ChildElement<T>) super.build();
  }

  @Override
  protected ChildElementCollectionImpl<T> createCollectionInstance(String localName, String namespaceUri) {
    return new ChildElementImpl<T>(childElementType, localName, namespaceUri, containingType);
  }

}
