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

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationExecutionException;
import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.util.ClassLoaderUtil;

import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.util.concurrent.Callable;

/**
 * <p>Common base class for writing EJB process applications.</p>
 *
 * <p>An EJB Process Application exposes itself as a Session Bean Component inside the EJB container.
 * This determines the invocation semantics when invoking code from the process application and the
 * nature of the {@link ProcessApplicationReference} held by the process engine.</p>
 *
 * <h2>Usage</h2>
 * <p>In order to add a custom EJB process application to your application, extend this class and configure
 * it as follows:</p>
 * <pre>
 * {@literal @}ProcessApplication("my process application")
 * {@literal @}Singleton
 * {@literal @}Startup
 * {@literal @}ConcurrencyManagement(ConcurrencyManagementType.BEAN)
 * {@literal @}TransactionAttribute(TransactionAttributeType.REQUIRED)
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
 * and resolve resources from the component's Environment (such as a <code>java:comp/BeanManager</code>).
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

  private static ProcessApplicationLogger LOG = ProcessEngineLogger.PROCESS_APPLICATION_LOGGER;

  protected static String MODULE_NAME_PATH  = "java:module/ModuleName";
  protected static String JAVA_APP_APP_NAME_PATH = "java:app/AppName";
  protected static String EJB_CONTEXT_PATH = "java:comp/EJBContext";

  private EjbProcessApplicationReference ejbProcessApplicationReference;
  private ProcessApplicationInterface selfReference;

  public ProcessApplicationReference getReference() {
    ensureInitialized();
    return ejbProcessApplicationReference;
  }

  protected String autodetectProcessApplicationName() {
    return lookupEeApplicationName();
  }

  /** allows subclasses to provide a custom business interface */
  protected Class<? extends ProcessApplicationInterface> getBusinessInterface() {
    return ProcessApplicationInterface.class;
  }

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
    if(ejbProcessApplicationReference == null) {
      ejbProcessApplicationReference = new EjbProcessApplicationReference(selfReference, getName());
    }
  }

  /**
   * lookup a proxy object representing the invoked business view of this component.
   */
  protected ProcessApplicationInterface lookupSelfReference() {

    try {
      InitialContext ic = new InitialContext();
      SessionContext sctxLookup = (SessionContext) ic.lookup(EJB_CONTEXT_PATH);
      return sctxLookup.getBusinessObject(getBusinessInterface());
    }
    catch (NamingException e) {
      throw LOG.ejbPaCannotLookupSelfReference(e);
    }

  }

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
