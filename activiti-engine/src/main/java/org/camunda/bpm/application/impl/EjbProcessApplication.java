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
package org.camunda.bpm.application.impl;

import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.ProcessApplicationReference;

import com.camunda.fox.platform.FoxPlatformException;

/**
 * @author Daniel Meyer
 * 
 */
public class EjbProcessApplication extends ProcessApplication {

  private static final Logger log = Logger.getLogger(EjbProcessApplication.class.getName());

  private String javaEeApplicationName;

  private String sessionObjectName;

  private EjbProcessApplicationReference ejbProcessApplicationReference;
  
  @Override
  public void start() {
    ensureInitialized();
    super.start();
  }
  
  public ProcessApplicationReference getReference() {    
    ensureInitialized();
    return ejbProcessApplicationReference;
  }

  private void ensureInitialized() {
    if(javaEeApplicationName == null) {
      javaEeApplicationName = getJavaEEApplicationName();
    }
    if(sessionObjectName == null) {
      sessionObjectName = composeSessionObjectName();
    }
    if(ejbProcessApplicationReference == null) {
      ejbProcessApplicationReference = new EjbProcessApplicationReference(javaEeApplicationName, sessionObjectName);      
    }
  }
  
  protected String autodetectProcessApplicationName() {
    return javaEeApplicationName;
  }

  protected String getJavaEEApplicationName() {
    try {

      InitialContext initialContext = new InitialContext();

      String appName = (String) initialContext.lookup("java:app/AppName");
      String moduleName = (String) initialContext.lookup("java:module/ModuleName");

      String detectedName = null;
      if (moduleName != null && !moduleName.equals(appName)) {
        // make sure that if an EAR carries multiple PAs, they are correctly
        // identified by appName + moduleName
        detectedName = appName + "/" + moduleName;
      } else {
        detectedName = appName;
      }

      return detectedName;

    } catch (NamingException e) {
      throw new FoxPlatformException("Could not autodetect EjbProcessApplicationName: "+e.getMessage(), e);
    }
  }
  
  protected String composeSessionObjectName() {
    Class<? extends EjbProcessApplication> applicationClass = getClass();
    return "java:global/" + javaEeApplicationName + "/" + applicationClass.getSimpleName() + "!" + applicationClass.getName();  
  }
  
}
