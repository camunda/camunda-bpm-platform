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

import java.util.concurrent.Callable;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationExecutionException;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/**
 * <p>Common base class for writing EJB process applications.</p>
 * 
 * <p>An EJB Process Application exposes itself as a Session Bean Component inside the EJB container.
 * This determines the invocation semantics when invoking code from the process application and the 
 * nature of the {@link ProcessApplicationReference} held by the process engine.</p>
 * 
 * <h2>Usage</h2>
 * <p>In order to add an EJB process application to your application, extend this class and configure 
 * it as follows:</p>
 * <pre>
 * {@literal @}ProcessApplication("my process application")
 * {@literal @}Singleton
 * {@literal @}Startup 
 * {@literal @}ConcurrencyManagement(ConcurrencyManagementType.BEAN) 
 * {@literal @}TransactionAttribute(TransactionAttributeType.SUPPORTS)
 * public class DefaultEjbProcessApplication extends EjbProcessApplication {
 *   
 *   {@literal @}PostConstruct
 *   public void deploy() {
 *     super.deploy();
 *   }
 * 
 *   {@literal @}PreDestroy
 *   public void undeploy() {
 *     super.undeploy();
 *   }
 * }
 * </pre>
 * <p>(the same Metadata can of course be provided using an XML-based <code>ejb-jar.xml</code> 
 * deployment descriptor</p> 
 *  
 * <h2>Invocation Semantics</h2>
 * <p>This allows the process engine as well as other applications to invoke this EJB Process 
 * Application and get EJB invocation semantics for the invocation. For example, if your
 * process application provides a {@link JavaDelegate} implementation, the process engine
 * will call the {@link EjbProcessApplication EjbProcessApplication's} 
 * {@link #execute(java.util.concurrent.Callable)} Method and from that method invoke 
 * the {@link JavaDelegate}. This makes sure that 
 * <ul>
 * <li>the call is intercepted by the EJB container and "enters" the process application legally.</li>
 * <li>the {@link JavaDelegate} may take advantage of the {@link EjbProcessApplication}'s invocation context 
 * and resolve resources form the component's Environment (such as a <code>java:comp/BeanManager</code>).
 * </ul>
 * </p>
 * 
 * 
 * <pre>
 *                        Big pile of EJB interceptors
 *                                     |
 *                                     |  +--------------------+    
 *                                     |  |Process Application |
 *                       invoke        v  |                    |
 *      ProcessEngine ----------------OOOOO--> Java Delegate   |
 *                                        |                    |
 *                                        |                    |
 *                                        +--------------------+
 *                                        
 * </pre>
 * 
 * <h2>Process Application Reference</h2>
 * <p>When the process application registers with a process engine 
 * (see {@link ManagementService#registerProcessApplication(String, ProcessApplicationReference)}, 
 * the process application passes a reference to itself to the process engine. This reference allows the 
 * process engine to reference the process application. The {@link EjbProcessApplication} takes advantage 
 * of the Ejb Containers naming context and passes a reference containing the EJBProcessApplication's 
 * Component Name to the process engine. Whenever the process engine needs access to process application, 
 * the actual component instance is looked up and invoked.</p>
 *
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
  
  public <T> T execute(Callable<T> callable) throws ProcessApplicationExecutionException {
    try {
      return callable.call();
    } catch(Exception e) {
      throw new ProcessApplicationExecutionException(e);
    }
  }
  
  protected String composeSessionObjectName() {
    
    try {

      InitialContext initialContext = new InitialContext();

      String appName = (String) initialContext.lookup("java:app/AppName");
      String moduleName = (String) initialContext.lookup("java:module/ModuleName");

      if (moduleName != null && !moduleName.equals(appName)) {
        // make sure that if an EAR carries multiple PAs, they are correctly
        // identified by appName + moduleName        
        // NOTE: this may be broken on IBM since there WARs or EJBs 
        // are always wrapped inside an EAR deployment. 
        eeModulePath = appName + "/" + moduleName;
      } else {
        eeModulePath = appName;
      }

      Class<? extends EjbProcessApplication> applicationClass = getClass();
      return "java:global/" + eeModulePath + "/" + applicationClass.getSimpleName() + "!" + applicationClass.getName();  

    } catch (NamingException e) {
      throw new ProcessEngineException("Could not autodetect EjbProcessApplicationName: "+e.getMessage(), e);
    }
    
    
  }
  
}
