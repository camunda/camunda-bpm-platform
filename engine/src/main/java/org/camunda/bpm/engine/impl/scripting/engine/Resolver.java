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
package org.camunda.bpm.engine.impl.scripting.engine;

import java.util.Set;

/**
 * A resolver for Objects bound to a key. A resolver provides a set of read only key bindings.
 * This means that key it is not possible to clear a key binding (remove the object bound to the key)
 * or to replace a key binding (set a new object bound to the key).
 *
 * @author Daniel Meyer
 * @author Tom Baeyens
 */
public interface Resolver {

  /**
   * Allows checking whether there is currently an object bound to the key.
   *
   * @param key the key to check
   * @return true if there is currently an object bound to the key. False otherwise.
   */
  boolean containsKey(Object key);

  /**
   * Returns the object currently bound to the key or false if no object is currently bound
   * to the key
   *
   * @param key the key of the object to retrieve.
   * @return the object currently bound to the key or 'null' if no object is currently bound to the key.
   */
  Object get(Object key);

  /**
   * Returns the set of key that can be resolved using this resolver.
   * @return the set of keys that can be resolved by this resolver.
   */
  Set<String> keySet();

}
