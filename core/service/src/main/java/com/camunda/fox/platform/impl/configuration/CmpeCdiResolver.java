/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.platform.impl.configuration;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.enterprise.inject.spi.BeanManager;

import org.activiti.cdi.impl.el.CdiResolver;
import org.activiti.engine.impl.javax.el.ELContext;

import com.camunda.fox.platform.impl.context.spi.ProcessArchiveServices;

/**
 * 
 * @author Daniel Meyer
 */
public class CmpeCdiResolver extends CdiResolver {
    
  private final ProcessArchiveServices paServices;
  
  public CmpeCdiResolver(ProcessArchiveServices processArchiveServices) {
    paServices = processArchiveServices;
  }
  
  @Override
  protected BeanManager getBeanManager() {
    return paServices.getBeanManager();
  }
  
  protected boolean skip() {
    return getBeanManager() == null;
  }
  
  @Override
  public Class< ? > getCommonPropertyType(ELContext context, Object base) {
    if(skip()) {
      return null;
    }
    return super.getCommonPropertyType(context, base);
  }

  @Override
  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
    if(skip()) {
      return null;
    }
    return super.getFeatureDescriptors(context, base);
  }

  @Override
  public Class< ? > getType(ELContext context, Object base, Object property) {
    if(skip()) {
      return null;
    }
    return super.getType(context, base, property);
  }

  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    if(skip()) {
      return null;
    }
    return super.getValue(context, base, property);
  }

  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property) {
    if(skip()) {
      return true;
    }
    return super.isReadOnly(context, base, property);
  }

  @Override
  public void setValue(ELContext context, Object base, Object property, Object value) {
    if(skip()) {
      return;
    }
    super.setValue(context, base, property, value);
  }

  @Override
  public Object invoke(ELContext context, Object base, Object method, java.lang.Class< ? >[] paramTypes, Object[] params) {
    if(skip()) {
      return null;
    }
    return super.invoke(context, base, method, paramTypes, params);
  }

}
