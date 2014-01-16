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

package org.camunda.bpm.model.xml.impl.type.reference;

import org.camunda.bpm.model.xml.impl.type.ModelElementTypeImpl;
import org.camunda.bpm.model.xml.impl.type.child.ChildElementCollectionImpl;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/**
 * @author Sebastian Menski
 */
public class QNameElementReferenceCollectionBuilderImpl<T extends ModelElementInstance, V extends ModelElementInstance> extends ElementReferenceCollectionBuilderImpl<T,V> {

  public QNameElementReferenceCollectionBuilderImpl(Class<V> childElementType, Class<T> referenceTargetClass, ChildElementCollectionImpl<V> collection, ModelElementTypeImpl containingType) {
    super(childElementType, referenceTargetClass, collection);
    this.elementReferenceCollectionImpl = new QNameElementReferenceCollectionImpl<T,V>(collection);
  }
}
