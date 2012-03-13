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
package com.camunda.fox.platform.impl.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.impl.configuration.JtaCmpeProcessEngineConfigurationFactory;
import com.camunda.fox.platform.impl.configuration.spi.ProcessEngineConfigurationFactory;
import com.camunda.fox.platform.impl.context.DefaultProcessArchiveServices;
import com.camunda.fox.platform.impl.context.spi.ProcessArchiveServices;
import com.camunda.fox.platform.impl.deployment.ClassPathScanner;
import com.camunda.fox.platform.impl.deployment.spi.ProcessArchiveScanner;
import com.camunda.fox.platform.impl.jobexecutor.simple.SimpleJobExecutorFactory;
import com.camunda.fox.platform.impl.jobexecutor.spi.JobExecutorFactory;

/**
 * A simple service registry delegating to a {@link java.util.ServiceLoader}
 * 
 * @author Daniel Meyer
 */
public class Services {
  
  protected final static Map<Class<?>, Class<?>> defaultImplementations = new HashMap<Class<?>, Class<?>>();
  
  protected final static Map<Class<?>, Object> cachedInstances = new HashMap<Class<?>, Object>();
  
  static {
    // register default implementations    
    defaultImplementations.put(ProcessEngineConfigurationFactory.class, JtaCmpeProcessEngineConfigurationFactory.class);
    defaultImplementations.put(ProcessArchiveScanner.class, ClassPathScanner.class);
    defaultImplementations.put(JobExecutorFactory.class, SimpleJobExecutorFactory.class);    
    defaultImplementations.put(ProcessArchiveServices.class, DefaultProcessArchiveServices.class);
  }
  
  @SuppressWarnings("unchecked")
  protected static <T> T getService(Class<T> serviceInterface, Class<?>  defaultImplementation) {
    ServiceLoader<T> loader = ServiceLoader.load(serviceInterface);
    Iterator<T> iterator = loader.iterator();
    if(iterator.hasNext()) {
      T instance = iterator.next();
      cachedInstances.put(serviceInterface, instance);
      return instance;
    } else if(defaultImplementation != null) {
      Class<? extends T> defaultImplementationClass = (Class<? extends T>) defaultImplementation;
      try {
        T instance = defaultImplementationClass.newInstance();
        cachedInstances.put(serviceInterface, instance);
        return instance;
      } catch (Exception e) {
        throw new FoxPlatformException("Could not instantiate service using default implementation: "+defaultImplementation, e);      
      }
    } else {
      cachedInstances.put(serviceInterface, null);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T getService(Class<T> serviceInterface) {
    Object instance = cachedInstances.get(serviceInterface);
    if(instance != null) {
      return (T) instance;
    } else {
      Class<?> defaultImplementation = defaultImplementations.get(serviceInterface);
      return getService(serviceInterface, defaultImplementation);
    }
  }
  
  public static void clear() {
    cachedInstances.clear();
  }
}
