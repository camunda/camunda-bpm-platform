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

import org.camunda.bpm.model.xml.impl.instance.ModelElementInstanceImpl;
import org.camunda.bpm.model.xml.impl.type.ModelElementTypeImpl;
import org.camunda.bpm.model.xml.impl.util.DomUtil.ElementByNameListFilter;
import org.camunda.bpm.model.xml.impl.util.DomUtil.ElementNodeListFilter;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/**
 * <p>Collection containing all elements with a given QName.</p>
 *
 * @author Daniel Meyer
 *
 */
public class NamedChildElementCollection<T extends ModelElementInstance> extends ChildElementCollectionImpl<T> {

  /** the filter to use */
  private final ElementNodeListFilter filter;

  final String namespaceUri;

  final String localName;

  /**
   * Crates a mutable collection.
   *
   * @param localName the local name of the elements in the same namespace as xmlElement
   * @param namespaceUri the namespace URI of the elements
   * @param containingType the containing type in the collection
   */
  public NamedChildElementCollection(String localName, String namespaceUri, ModelElementTypeImpl containingType) {
    this.localName = localName;
    this.namespaceUri = namespaceUri;
    this.containingType = containingType;
    filter = new ElementByNameListFilter(localName, namespaceUri);
  }

  public String getNamespaceUri() {
    return namespaceUri;
  }

  protected ElementNodeListFilter getFilter(ModelElementInstanceImpl modelElement) {
    return filter;
  }

}
