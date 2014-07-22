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
 */
public class ProgrammaticBeanLookup {

  public final static Logger LOG = Logger.getLogger(ProgrammaticBeanLookup.class.getName());

  @SuppressWarnings("unchecked")
  public static <T> T lookup(Class<T> clazz, BeanManager bm) {
    Iterator<Bean< ? >> iter = bm.getBeans(clazz).iterator();
    if (!iter.hasNext()) {
      throw new IllegalStateException("CDI BeanManager cannot find an instance of requested type " + clazz.getName());
    }
    Bean<T> bean = (Bean<T>) iter.next();
    CreationalContext<T> ctx = bm.createCreationalContext(bean);
    if(isDependentScoped(bean)) {
      releaseOnContextClose(ctx, bean);
    }
    T dao = (T) bm.getReference(bean, clazz, ctx);
    return dao;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static Object lookup(String name, BeanManager bm) {
    Set<Bean< ? >> beans = bm.getBeans(name);
    if (beans.isEmpty()) {
      throw new IllegalStateException("CDI BeanManager cannot find an instance of requested type '" + name + "'");
    }
    Bean bean = bm.resolve(beans);
    CreationalContext ctx = bm.createCreationalContext(bean);
    if(isDependentScoped(bean)) {
      releaseOnContextClose(ctx, bean);
    }
    // select one beantype randomly. A bean has a non-empty set of beantypes.
    Type type = (Type) bean.getTypes().iterator().next();
    return bm.getReference(bean, type, ctx);
  }

  public static <T> boolean isDependentScoped(Bean<T> bean) {
    return Dependent.class.equals(bean.getScope());
  }

  public static void releaseOnContextClose(CreationalContext<?> creationalContext, Bean bean) {
    CommandContext commandContext = Context.getCommandContext();
    if(commandContext != null) {
      commandContext.registerCommandContextCloseListener(new CreationalContextReleaseListener(creationalContext));

    } else {
      LOG.warning("Obtained instance of @Dependent scoped bean "+bean +" outside of process engine command context. "
          + "Bean instance will not be destroyed. This is likely to create a memory leak. Please use a normal scope like @ApplicationScoped for this bean.");

    }
  }

  public static <T> T lookup(Class<T> clazz) {
    BeanManager bm = BeanManagerLookup.getBeanManager();
    return lookup(clazz, bm);
  }

  public static Object lookup(String name) {
    BeanManager bm = BeanManagerLookup.getBeanManager();
    return lookup(name, bm);
  }

}
