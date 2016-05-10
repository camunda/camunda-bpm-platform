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

package org.camunda.bpm.engine.impl.core.model;

/**
 * Key of a map property.
 *
 * @param K the type of keys maintained by the map
 * @param V the type of mapped values
 *
 * @author Philipp Ossler
 *
 */
public class PropertyMapKey<K,V> {

  protected final String name;
  protected boolean allowOverwrite = true;

  public PropertyMapKey(String name) {
    this(name, true);
  }

  public PropertyMapKey(String name, boolean allowOverwrite) {
    this.name = name;
    this.allowOverwrite = allowOverwrite;
  }

  public boolean allowsOverwrite() {
    return allowOverwrite;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "PropertyMapKey [name=" + name + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PropertyMapKey<?,?> other = (PropertyMapKey<?,?>) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }
}
