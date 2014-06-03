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

package org.camunda.spin.impl.xml.dom;

import org.camunda.spin.xml.tree.SpinXmlTreeAttribute;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import java.util.Iterator;

/**
 * @author Sebastian Menski
 */
public class SpinXmlDomAttributeMapIterable implements Iterable<SpinXmlTreeAttribute> {

  protected final Element domElement;
  protected final XmlDomDataFormat dataFormat;
  protected final String namespace;
  protected final boolean validating;

  public SpinXmlDomAttributeMapIterable(Element domElement, XmlDomDataFormat dataFormat) {
    this.domElement = domElement;
    this.dataFormat = dataFormat;
    this.namespace = null;
    validating = false;
  }

  public SpinXmlDomAttributeMapIterable(Element domElement, XmlDomDataFormat dataFormat, String namespace) {
    this.domElement = domElement;
    this.dataFormat = dataFormat;
    this.namespace = namespace;
    validating = true;
  }

  public Iterator<SpinXmlTreeAttribute> iterator() {
    return new SpinXmlDomNodeIterator<SpinXmlTreeAttribute>() {

      private NamedNodeMap attributes = domElement.getAttributes();

      protected int getLength() {
        return attributes.getLength();
      }

      protected SpinXmlTreeAttribute getCurrent() {
        if (attributes != null) {
          Attr attribute = (Attr) attributes.item(index);
          SpinXmlTreeAttribute current = dataFormat.createAttributeWrapper(attribute);
          if (!validating || (current.hasNamespace(namespace))) {
            return current;
          }
        }
        return null;
      }
    };
  }

}
