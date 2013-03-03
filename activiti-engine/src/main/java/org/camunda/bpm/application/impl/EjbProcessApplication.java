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

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationReference;

import com.camunda.fox.platform.FoxPlatformException;

/**
 * @author Daniel Meyer
 * 
 */
public class EjbProcessApplication extends AbstractProcessApplication {

  private String sessionObjectName;
  private String eeModulePath;

  private EjbProcessApplicationReference ejbProcessApplicationReference;
  
  @Override
  public void deploy() {
    ensureInitialized();
    super.deploy();
  }
  
  public ProcessApplicationReference getReference() {    
    ensureInitialized();
    return ejbProcessApplicationReference;
  }

  protected void ensureInitialized() {    
    if(sessionObjectName == null) {
      sessionObjectName = composeSessionObjectName();
    }
    if(ejbProcessApplicationReference == null) {
      ejbProcessApplicationReference = new EjbProcessApplicationReference(eeModulePath, sessionObjectName);      
    }
  }
  
  protected String autodetectProcessApplicationName() {
    ensureInitialized();
    return eeModulePath;
  }
  
  protected String composeSessionObjectName() {
    
    try {

      InitialContext initialContext = new InitialContext();

      String appName = (String) initialContext.lookup("java:app/AppName");
      String moduleName = (String) initialContext.lookup("java:module/ModuleName");

      if (moduleName != null && !moduleName.equals(appName)) {
        // make sure that if an EAR carries multiple PAs, they are correctly
        // identified by appName + moduleName
        eeModulePath = appName + "/" + moduleName;
      } else {
        eeModulePath = appName;
      }

      Class<? extends EjbProcessApplication> applicationClass = getClass();
      return "java:global/" + eeModulePath + "/" + applicationClass.getSimpleName() + "!" + applicationClass.getName();  

    } catch (NamingException e) {
      throw new FoxPlatformException("Could not autodetect EjbProcessApplicationName: "+e.getMessage(), e);
    }
    
    
  }
  
}
