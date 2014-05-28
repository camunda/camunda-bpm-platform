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

import org.camunda.spin.logging.SpinLogger;
import org.camunda.spin.xml.tree.SpinXmlTreeElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;

import static org.camunda.spin.impl.util.SpinEnsure.ensureNotNull;

/**
 * @author Sebastian Menski
 */
public class SpinXmlDomElementIterable implements Iterable<SpinXmlTreeElement> {

  private final static XmlDomLogger LOG = SpinLogger.XML_DOM_LOGGER;

  protected final Element domElement;
  protected final XmlDomDataFormat dataFormat;
  protected final String namespace;
  protected final String name;
  protected boolean validating;

  public SpinXmlDomElementIterable(Element domElement, XmlDomDataFormat dataFormat) {
    this.domElement = domElement;
    this.dataFormat = dataFormat;
    this.namespace = null;
    this.name = null;
    validating = false;
  }

  public SpinXmlDomElementIterable(Element domElement, XmlDomDataFormat dataFormat, String namespace, String name) {
    ensureNotNull("name", name);
    this.domElement = domElement;
    this.dataFormat = dataFormat;
    this.namespace = namespace;
    this.name = name;
    validating = true;
  }

  public Iterator<SpinXmlTreeElement> iterator() {
    return new Iterator<SpinXmlTreeElement>() {

      private int index = 0;
      private NodeList childs = domElement.getChildNodes();

      private SpinXmlTreeElement getCurrent() {
        if (childs != null) {
          Node item = childs.item(index);
          if (item != null && item instanceof Element) {
            SpinXmlTreeElement current = dataFormat.createElementWrapper((Element) item);
            if (!validating || (current.hasNamespace(namespace) && name.equals(current.name()))) {
                return current;
            }
          }
        }
        return null;
      }

      public boolean hasNext() {
        for (; index < childs.getLength(); index++) {
          if (getCurrent() != null) {
            return true;
          }
        }
        return false;
      }

      public SpinXmlTreeElement next() {
        if (hasNext()) {
          SpinXmlTreeElement current = getCurrent();
          index++;
          return current;
        }
        else {
          throw LOG.iteratorHasNoMoreElements(SpinXmlDomElementIterable.class);
        }
      }

      public void remove() {
        throw LOG.methodNotSupportedByClass("remove", SpinXmlDomElementIterable.class);
      }
    };
  }

}
