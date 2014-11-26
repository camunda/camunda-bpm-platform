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
package org.camunda.bpm.engine.impl.el;

import java.beans.FeatureDescriptor;
import java.util.Collections;
import java.util.Iterator;

import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;

/**
 * @author Thorben Lindhauer
 */
public abstract class AbstractElResolverDelegate extends ELResolver {

  protected abstract ELResolver getElResolverDelegate();

  public Class<?> getCommonPropertyType(ELContext context, Object base) {
    ELResolver delegate = getElResolverDelegate();
    if(delegate == null) {
      return null;
    } else {
      return delegate.getCommonPropertyType(context, base);
    }
  }

  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
    ELResolver delegate = getElResolverDelegate();
    if(delegate == null) {
      return Collections.<FeatureDescriptor>emptySet().iterator();
    } else {
      return delegate.getFeatureDescriptors(context, base);
    }
  }


  public Class<?> getType(ELContext context, Object base, Object property) {
    context.setPropertyResolved(false);
    ELResolver delegate = getElResolverDelegate();
    if(delegate == null) {
      return null;
    } else {
      return delegate.getType(context, base, property);
    }
  }


  public Object getValue(ELContext context, Object base, Object property) {
    context.setPropertyResolved(false);
    ELResolver delegate = getElResolverDelegate();
    if(delegate == null) {
      return null;
    } else {
      return delegate.getValue(context, base, property);
    }
  }

  public boolean isReadOnly(ELContext context, Object base, Object property) {
    context.setPropertyResolved(false);
    ELResolver delegate = getElResolverDelegate();
    if(delegate == null) {
      return true;
    } else {
      return delegate.isReadOnly(context, base, property);
    }
  }

  public void setValue(ELContext context, Object base, Object property, Object value) {
    context.setPropertyResolved(false);
    ELResolver delegate = getElResolverDelegate();
    if(delegate != null) {
      delegate.setValue(context, base, property, value);
    }
  }

  public Object invoke(ELContext context, Object base, Object method, Class<?>[] paramTypes, Object[] params) {
    context.setPropertyResolved(false);
    ELResolver delegate = getElResolverDelegate();
    if(delegate == null) {
      return null;
    } else {
      return delegate.invoke(context, base, method, paramTypes, params);
    }
  }

}
