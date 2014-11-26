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

import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * Utility class for performing programmatic bean lookups.
 *
 * @author Daniel Meyer
 * @author Mark Struberg
 */
public class ProgrammaticBeanLookup {

  public final static Logger LOG = Logger.getLogger(ProgrammaticBeanLookup.class.getName());

  public static <T> T lookup(Class<T> clazz, BeanManager bm) {
    return lookup(clazz, bm, true);
  }

  public static <T> T lookup(Class<T> clazz, BeanManager bm, boolean optional) {
    Set<Bean<?>> beans = bm.getBeans(clazz);
    T instance = getContextualReference(bm, beans, clazz);
    if (!optional && instance == null) {
      throw new IllegalStateException("CDI BeanManager cannot find an instance of requested type '" + clazz.getName() + "'");
    }
    return instance;
  }

  public static Object lookup(String name, BeanManager bm) {
    return lookup(name, bm, true);
  }

  public static Object lookup(String name, BeanManager bm, boolean optional) {
    Set<Bean<?>> beans = bm.getBeans(name);

    // NOTE: we use Object.class as BeanType of the ContextualReference to resolve.
    // Mark says this is not strictly spec compliant but should work on all implementations.
    // A strictly compliant implementation would
    // - collect all bean types of the bean
    // - calculate the type such that it has the most types in the set of bean types which are assignable from this type.
    Object instance = getContextualReference(bm, beans, Object.class);
    if (!optional && instance == null) {
      throw new IllegalStateException("CDI BeanManager cannot find an instance of requested type '" + name + "'");
    }
    return instance;
  }

  /**
   * @return a ContextualInstance of the given type
   * @throws javax.enterprise.inject.AmbiguousResolutionException if the given type is satisfied by more than one Bean
   * @see #lookup(Class, boolean)
   */
  public static <T> T lookup(Class<T> clazz) {
    return lookup(clazz, true);
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


  @SuppressWarnings("unchecked")
  private static <T> T getContextualReference(BeanManager bm, Set<Bean<?>> beans, Class<?> type) {
    if (beans == null || beans.size() == 0) {
      return null;
    }

    // if we would resolve to multiple beans then BeanManager#resolve would throw an AmbiguousResolutionException
    Bean<?> bean = bm.resolve(beans);
    if (bean == null) {
      return null;

    } else {
      CreationalContext<?> creationalContext = bm.createCreationalContext(bean);

      // if we obtain a contextual reference to a @Dependent scope bean, make sure it is released
      if(isDependentScoped(bean)) {
        releaseOnContextClose(creationalContext, bean);
      }

      return (T) bm.getReference(bean, type, creationalContext);

    }
  }

  private static boolean isDependentScoped(Bean<?> bean) {
    return Dependent.class.equals(bean.getScope());
  }

  private static void releaseOnContextClose(CreationalContext<?> creationalContext, Bean<?> bean) {
    CommandContext commandContext = Context.getCommandContext();
    if(commandContext != null) {
      commandContext.registerCommandContextListener(new CreationalContextReleaseListener(creationalContext));

    } else {
      LOG.warning("Obtained instance of @Dependent scoped bean "+bean +" outside of process engine command context. "
          + "Bean instance will not be destroyed. This is likely to create a memory leak. Please use a normal scope like @ApplicationScoped for this bean.");

    }
  }

}
