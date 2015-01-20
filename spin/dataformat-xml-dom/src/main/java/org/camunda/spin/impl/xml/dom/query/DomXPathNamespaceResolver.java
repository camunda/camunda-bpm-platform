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
package org.camunda.spin.impl.xml.dom.query;

import org.camunda.spin.xml.SpinXmlElement;

import javax.swing.plaf.PanelUI;
import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Resolves the namespaces of the given element.
 *
 * @author Stefan Hentschel
 */
public class DomXPathNamespaceResolver implements NamespaceContext {

  protected Map<String, String> namespaces;
  protected SpinXmlElement element;

  public DomXPathNamespaceResolver(SpinXmlElement element) {
    this.namespaces = new HashMap<String, String>();
    this.element = element;
  }

  public String getNamespaceURI(String prefix) {

    // using 'DEFAULT' as prefix for xmlns="<URI>" namespace
    if(prefix.equals("DEFAULT")) {
      return element.namespace();
    } else {
      return namespaces.get(prefix);
    }
  }

  public String getPrefix(String namespaceURI) {
    String key = null;
    if(namespaces.containsValue(namespaceURI)) {
      for(Map.Entry<String, String> entry : namespaces.entrySet()) {
        if(entry.getValue().equals(namespaceURI)) {
          key = entry.getKey();
          break;
        }
      }
    }
    return key;
  }

  public Iterator getPrefixes(String namespaceURI) {
    return namespaces.entrySet().iterator();
  }

  /**
   * Maps a single prefix, uri pair as namespace.
   *
   * @param prefix the prefix to use
   * @param namespaceURI the URI to use
   */
  public void setNamespace(String prefix, String namespaceURI) {
    if(namespaceURI != null) {
      namespaces.put(prefix, namespaceURI);
    } else {
      namespaces.remove(prefix);
    }
  }

  /**
   * Maps a map of prefix, uri pairs as namespaces.
   *
   * @param namespaces the map of namespaces
   */
  public void setNamespaces(Map<String, String> namespaces) {
    this.namespaces = namespaces;
  }

  /**
   * This enables the detection of the namespaces.
   */
  public void autodetectNamespaces() {
    detectNamespaces(element);
  }

  /**
   * Detects the namespaces of the element and all children.
   *
   * @param element the parent element for detection
   */
  protected void detectNamespaces(SpinXmlElement element) {
    if(!namespaces.containsKey(element.prefix())) {
      if(element.namespace() != null && element.prefix() != null) {
        namespaces.put(element.prefix(), element.namespace());
      }

      for (SpinXmlElement childElement : element.childElements()) {
        detectNamespaces(childElement);
      }
    }
  }
}
