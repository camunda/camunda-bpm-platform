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

import java.util.Iterator;

import org.camunda.spin.xml.SpinXmlNode;

/**
 * @author Sebastian Menski
 */
public abstract class DomXmlNodeIterator<T extends SpinXmlNode> implements Iterator<T> {

  private static final DomXmlLogger LOG = DomXmlLogger.XML_DOM_LOGGER;

  protected int index = 0;

  public boolean hasNext() {
    for (; index < getLength() ; index++) {
      if (getCurrent() != null) {
        return true;
      }
    }
    return false;
  }

  public T next() {
    if (hasNext()) {
      T current = getCurrent();
      index++;
      return current;
    }
    else {
      throw LOG.iteratorHasNoMoreElements(DomXmlNodeIterator.class);
    }
  }

  public void remove() {
    throw LOG.methodNotSupportedByClass("remove", DomXmlElementIterable.class);
  }

  protected abstract int getLength();

  protected abstract T getCurrent();

}
