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
import org.camunda.spin.xml.SpinXmlElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;

import static org.camunda.commons.utils.EnsureUtil.ensureNotNull;

/**
 * @author Sebastian Menski
 */
public class DomXmlElementIterable implements Iterable<SpinXmlElement> {

  protected final NodeList nodeList;
  protected final DomXmlDataFormat dataFormat;
  protected final String namespace;
  protected final String name;
  protected boolean validating;

  public DomXmlElementIterable(Element domElement, DomXmlDataFormat dataFormat) {
    this(domElement.getChildNodes(), dataFormat);
  }

  public DomXmlElementIterable(NodeList nodeList, DomXmlDataFormat dataFormat) {
    this.nodeList = nodeList;
    this.dataFormat = dataFormat;
    this.namespace = null;
    this.name = null;
    validating = false;
  }

  public DomXmlElementIterable(Element domElement, DomXmlDataFormat dataFormat, String namespace, String name) {
    this(domElement.getChildNodes(), dataFormat, namespace, name);
  }

  public DomXmlElementIterable(NodeList nodeList, DomXmlDataFormat dataFormat, String namespace, String name) {
    ensureNotNull("name", name);
    this.nodeList = nodeList;
    this.dataFormat = dataFormat;
    this.namespace = namespace;
    this.name = name;
    validating = true;
  }

  public Iterator<SpinXmlElement> iterator() {
    return new DomXmlNodeIterator<SpinXmlElement>() {

      private NodeList childs = nodeList;

      protected int getLength() {
        return childs.getLength();
      }

      protected SpinXmlElement getCurrent() {
        if (childs != null) {
          Node item = childs.item(index);
          if (item != null && item instanceof Element) {
            SpinXmlElement current = dataFormat.createElementWrapper((Element) item);
            if (!validating || (current.hasNamespace(namespace) && name.equals(current.name()))) {
                return current;
            }
          }
        }
        return null;
      }
    };

  }

}
