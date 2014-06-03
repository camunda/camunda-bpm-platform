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

package org.camunda.spin.impl;

import org.camunda.spin.Spin;
import org.camunda.spin.SpinList;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Sebastian Menski
 */
public class SpinListImpl<E extends Spin<?>> extends ArrayList<E> implements SpinList<E> {

  private static final long serialVersionUID = 1L;

  public SpinListImpl() {
    super();
  }

  public SpinListImpl(int initialCapacity) {
    super(initialCapacity);
  }

  public SpinListImpl(Collection<? extends E> c) {
    super(c);
  }

  public SpinListImpl(Iterable<E> iterable) {
    super();
    for (E e : iterable) {
      add(e);
    }
  }

}
