/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.application.impl;

import java.util.concurrent.Callable;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationExecutionException;
import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.util.ClassLoaderUtil;

public abstract class AbstractEjbProcessApplication extends AbstractProcessApplication {

  private static ProcessApplicationLogger LOG = ProcessEngineLogger.PROCESS_APPLICATION_LOGGER;

  protected static String MODULE_NAME_PATH  = "java:module/ModuleName";
  protected static String JAVA_APP_APP_NAME_PATH = "java:app/AppName";
  protected static String EJB_CONTEXT_PATH = "java:comp/EJBContext";

  protected ProcessApplicationInterface selfReference;

  @Override
  public ProcessApplicationReference getReference() {
    ensureInitialized();
    return getEjbProcessApplicationReference();
  }

  @Override
  protected String autodetectProcessApplicationName() {
    return lookupEeApplicationName();
  }

  /** allows subclasses to provide a custom business interface */
  protected Class<? extends ProcessApplicationInterface> getBusinessInterface() {
    return ProcessApplicationInterface.class;
  }

  @Override
  public <T> T execute(Callable<T> callable) throws ProcessApplicationExecutionException {
    ClassLoader originalClassloader = ClassLoaderUtil.getContextClassloader();
    ClassLoader processApplicationClassloader = getProcessApplicationClassloader();

    try {
      if (originalClassloader != processApplicationClassloader) {
        ClassLoaderUtil.setContextClassloader(processApplicationClassloader);
      }

      return callable.call();

    }
    catch(Exception e) {
      throw LOG.processApplicationExecutionException(e);
    }
    finally {
      ClassLoaderUtil.setContextClassloader(originalClassloader);
    }
  }

  protected void ensureInitialized() {
    if(selfReference == null) {
      selfReference = lookupSelfReference();
    }
    ensureEjbProcessApplicationReferenceInitialized();
  }

  protected abstract void ensureEjbProcessApplicationReferenceInitialized();
  protected abstract ProcessApplicationReference getEjbProcessApplicationReference();

  /**
   * lookup a proxy object representing the invoked business view of this component.
   */
  protected abstract ProcessApplicationInterface lookupSelfReference();

  /**
   * determine the ee application name based on information obtained from JNDI.
   */
  protected String lookupEeApplicationName() {

    try {
      InitialContext initialContext = new InitialContext();

      String appName = (String) initialContext.lookup(JAVA_APP_APP_NAME_PATH);
      String moduleName = (String) initialContext.lookup(MODULE_NAME_PATH);

      // make sure that if an EAR carries multiple PAs, they are correctly
      // identified by appName + moduleName
      if (moduleName != null && !moduleName.equals(appName)) {
        return appName + "/" + moduleName;
      } else {
        return appName;
      }
    }
    catch (NamingException e) {
      throw LOG.ejbPaCannotAutodetectName(e);
    }
  }

}
