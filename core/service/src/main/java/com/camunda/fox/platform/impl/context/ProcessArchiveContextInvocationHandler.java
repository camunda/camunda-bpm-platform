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
package com.camunda.fox.platform.impl.context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.camunda.fox.platform.FoxPlatformException;

/**
 * <p>Wraps each call to an activiti service method within a {@link ProcessArchiveContext}</p> 
 * 
 * @author Daniel Meyer
 */
public class ProcessArchiveContextInvocationHandler implements InvocationHandler {

  private final ProcessArchiveContext context;
  private final Object delegate;

  public ProcessArchiveContextInvocationHandler(ProcessArchiveContext context, Object delegate) {
    this.context = context;
    this.delegate = delegate;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if(method.getDeclaringClass().equals(Object.class)) {
      return method.invoke(delegate, args);
    } else {
      return intercept(method, args);
    }
  }

  protected Object intercept(Method method, Object[] args) throws Throwable  {
    assertContextActive();
    ProcessArchiveContext.setCurrentContext(context);
    ProcessArchiveContext.setWithinProcessArchive(true);
    try {
      return method.invoke(delegate, args);
    }catch (InvocationTargetException e) {
      // throw the actual exception
      Throwable cause = e.getCause();
      if(cause != null) {
        throw cause;
      }
      throw e;
    } finally {
      ProcessArchiveContext.setWithinProcessArchive(false);
      ProcessArchiveContext.clearCurrentContext();
    }
  }

  protected void assertContextActive() {
    if(!context.isActive()) {
      throw new FoxPlatformException("Process archive context ['"+context.getProcessArchive().getName()+"'] is inactive.");
    }
  }
}
