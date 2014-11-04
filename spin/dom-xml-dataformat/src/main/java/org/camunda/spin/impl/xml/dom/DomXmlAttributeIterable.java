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

import org.camunda.spin.impl.xml.dom.format.DomXmlDataFormat;
import org.camunda.spin.xml.SpinXmlAttribute;
import org.w3c.dom.Attr;
import org.w3c.dom.NodeList;

import java.util.Iterator;

/**
 * @author Sebastian Menski
 */
public class DomXmlAttributeIterable implements Iterable<SpinXmlAttribute> {

  protected final NodeList nodeList;
  protected final DomXmlDataFormat dataFormat;
  protected final String namespace;
  protected final boolean validating;

  public DomXmlAttributeIterable(NodeList nodeList, DomXmlDataFormat dataFormat) {
    this.nodeList = nodeList;
    this.dataFormat = dataFormat;
    this.namespace = null;
    validating = false;
  }

  public DomXmlAttributeIterable(NodeList nodeList, DomXmlDataFormat dataFormat, String namespace) {
    this.nodeList = nodeList;
    this.dataFormat = dataFormat;
    this.namespace = namespace;
    validating = true;
  }

  public Iterator<SpinXmlAttribute> iterator() {
    return new DomXmlNodeIterator<SpinXmlAttribute>() {

      private NodeList attributes = nodeList;

      protected int getLength() {
        return attributes.getLength();
      }

      protected SpinXmlAttribute getCurrent() {
        if (attributes != null) {
          Attr attribute = (Attr) attributes.item(index);
          SpinXmlAttribute current = dataFormat.createAttributeWrapper(attribute);
          if (!validating || (current.hasNamespace(namespace))) {
            return current;
          }
        }
        return null;
      }
    };
  }

}
