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
package org.camunda.bpm.engine.cdi.impl.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.enterprise.inject.spi.BeanManager;

import org.camunda.bpm.engine.cdi.impl.util.BeanManagerLookup;
import org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;
import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;


/**
 * Resolver wrapping an instance of javax.el.ELResolver obtained from the
 * {@link BeanManager}. Allows the process engine to resolve Cdi-Beans.
 *
 * @author Daniel Meyer
 */
public class CdiResolver extends ELResolver {

  protected BeanManager getBeanManager() {
    return BeanManagerLookup.getBeanManager();
  }

  protected javax.el.ELResolver getWrappedResolver() {
    BeanManager beanManager = getBeanManager();
    javax.el.ELResolver resolver = beanManager.getELResolver();
    return resolver;
  }

  @Override
  public Class< ? > getCommonPropertyType(ELContext context, Object base) {
    return getWrappedResolver().getCommonPropertyType(wrapContext(context), base);
  }

  @Override
  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
    return getWrappedResolver().getFeatureDescriptors(wrapContext(context), base);
  }

  @Override
  public Class< ? > getType(ELContext context, Object base, Object property) {
    return getWrappedResolver().getType(wrapContext(context), base, property);
  }

  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    //we need to resolve a bean only for the first "member" of expression, e.g. bean.property1.property2
    if (base == null) {
      Object result = ProgrammaticBeanLookup.lookup(property.toString(), getBeanManager());
      if (result != null) {
        context.setPropertyResolved(true);
      }
      return result;
    } else {
      return null;
    }
  }

  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property) {
    return getWrappedResolver().isReadOnly(wrapContext(context), base, property);
  }

  @Override
  public void setValue(ELContext context, Object base, Object property, Object value) {
    getWrappedResolver().setValue(wrapContext(context), base, property, value);
  }

  @Override
  public Object invoke(ELContext context, Object base, Object method, java.lang.Class< ? >[] paramTypes, Object[] params) {
    return getWrappedResolver().invoke(wrapContext(context), base, method, paramTypes, params);
  }

  protected javax.el.ELContext wrapContext(ELContext context) {
    return new ElContextDelegate(context, getWrappedResolver());
  }

}
