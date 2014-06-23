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
package org.camunda.bpm.engine.cdi.impl.util;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

/**
 * Utility class for performing programmatic bean lookups.
 * 
 * @author Daniel Meyer
 * @author Mark Struberg
 */
public class ProgrammaticBeanLookup {

  public static <T> T lookup(Class<T> clazz, BeanManager bm) {
    return lookup(clazz, bm, true);
  }

  public static <T> T lookup(Class<T> clazz, BeanManager bm, boolean optional) {
    Set<Bean<?>> beans = bm.getBeans(clazz);
    T instance = getContextualReference(bm, beans);
    if (!optional && instance == null) {
      throw new IllegalStateException("CDI BeanManager cannot find an instance of requested type '" + clazz.getName() + "'");
    }

    return instance;
  }

  public static Object lookup(String name, BeanManager bm) {
    Set<Bean<?>> beans = bm.getBeans(name);
    Object instance = getContextualReference(bm, beans);
    if (instance == null) {
      throw new IllegalStateException("CDI BeanManager cannot find an instance of requested type '" + name + "'");
    }

    return instance;
  }

  private static <T> T getContextualReference(BeanManager bm, Set<Bean<?>> beans) {
    if (beans == null || beans.size() == 0) {
      return null;
    }

    // if we would resolve to multiple beans than BeanManager#resolve would throw an AmbiguousResolutionException
    Bean<?> bean = bm.resolve(beans);
    if (bean == null) {
      return null;
    }
    CreationalContext<?> creationalContext = bm.createCreationalContext(bean);

    return (T) bm.getReference(bean, bean.getBeanClass(), creationalContext);
  }

  /**
   * @return a ContextualInstance of the given type
   * @throws IllegalStateException if there is no bean of the given class
   * @throws javax.enterprise.inject.AmbiguousResolutionException if the given type is satisfied by more than one Bean
   * @see #lookup(Class, boolean)
   */
  public static <T> T lookup(Class<T> clazz) {
    return lookup(clazz, false);
  }

  /**
   * @param optional if <code>false</code> then the bean must exist.
   * @return a ContextualInstance of the given type if optional is <code>false</code>. If optional is <code>true</code> null might be returned if no bean got found.
   * @throws IllegalStateException if there is no bean of the given class, but only if optional is <code>false</code>
   * @throws javax.enterprise.inject.AmbiguousResolutionException if the given type is satisfied by more than one Bean
   * @see #lookup(Class, boolean)
   */
  public static <T> T lookup(Class<T> clazz, boolean optional) {
    BeanManager bm = BeanManagerLookup.getBeanManager();
    return lookup(clazz, bm, optional);
  }

  public static Object lookup(String name) {
    BeanManager bm = BeanManagerLookup.getBeanManager();
    return lookup(name, bm);
  }

}
