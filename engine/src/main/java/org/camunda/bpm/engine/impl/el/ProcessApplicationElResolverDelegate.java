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

import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.javax.el.CompositeELResolver;
import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;

/**
 * <p>This is an {@link ELResolver} implementation that delegates to a ProcessApplication-provided 
 * {@link ELResolver}. The idea is that in a multi-application setup, a shared process engine may orchestrate 
 * multiple process applications. In this setting we want to delegate to the current process application 
 * for performing expression resolving. This also allows individual process applications to integrate with 
 * different kinds of Di Containers or other expression-context providing frameworks. For instance, a first 
 * process application may use the spring application context for resolving Java Delegate implementations 
 * while a second application may use CDI or even an Apache Camel Context.</p>
 * 
 * <p>The behavior of this implementation is as follows: if we are not currently running in the context of 
 * a process application, we are skipped. If we are, this implementation delegates to the underlying 
 * application-provided {@link ELResolver} which may itself be a {@link CompositeELResolver}.</p>    
 * 
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationElResolverDelegate extends ELResolver {
  
  protected ELResolver getProcessApplicationElResolverDelegate() {
    
    ProcessApplicationReference processApplicationReference = Context.getCurrentProcessApplication();
    if(processApplicationReference != null) {
      
      try {
        ProcessApplicationInterface processApplication = processApplicationReference.getProcessApplication();
        return processApplication.getElResolver();
        
      } catch (ProcessApplicationUnavailableException e) {
        throw new ProcessEngineException("Cannot access process application '"+processApplicationReference.getName()+"'", e);
      }
      
    } else {
      return null;
    }
    
  }

  public Class<?> getCommonPropertyType(ELContext context, Object base) {
    ELResolver delegate = getProcessApplicationElResolverDelegate();
    if(delegate == null) {
      return null;
    } else {
      return delegate.getCommonPropertyType(context, base);
    }
  }
  
  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
    ELResolver delegate = getProcessApplicationElResolverDelegate();
    if(delegate == null) {
      return Collections.<FeatureDescriptor>emptySet().iterator();
    } else {
      return delegate.getFeatureDescriptors(context, base);
    }
  }

 
  public Class<?> getType(ELContext context, Object base, Object property) {
    context.setPropertyResolved(false);
    ELResolver delegate = getProcessApplicationElResolverDelegate();
    if(delegate == null) {
      return null;
    } else {
      return delegate.getType(context, base, property);
    }
  }

  
  public Object getValue(ELContext context, Object base, Object property) {
    context.setPropertyResolved(false);
    ELResolver delegate = getProcessApplicationElResolverDelegate();
    if(delegate == null) {
      return null;
    } else {
      return delegate.getValue(context, base, property);
    }
  }

  public boolean isReadOnly(ELContext context, Object base, Object property) {
    context.setPropertyResolved(false);
    ELResolver delegate = getProcessApplicationElResolverDelegate();
    if(delegate == null) {
      return true;
    } else {
      return delegate.isReadOnly(context, base, property);
    }
  }

  public void setValue(ELContext context, Object base, Object property, Object value) {
    context.setPropertyResolved(false);
    ELResolver delegate = getProcessApplicationElResolverDelegate();
    if(delegate != null) {      
      delegate.setValue(context, base, property, value);
    }
  }
  
  public Object invoke(ELContext context, Object base, Object method, Class<?>[] paramTypes, Object[] params) {
    context.setPropertyResolved(false);
    ELResolver delegate = getProcessApplicationElResolverDelegate();
    if(delegate == null) {
      return null;
    } else {
      return delegate.invoke(context, base, method, paramTypes, params);
    }
  }

}
