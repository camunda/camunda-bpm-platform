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
package org.camunda.bpm.application;

import java.util.Map;
import java.util.concurrent.Callable;

import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.application.impl.EjbProcessApplication;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.javax.el.BeanELResolver;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;
import org.camunda.bpm.engine.repository.DeploymentBuilder;

/**
 * <p>A Process Application is an ordinary Java Application that uses the camunda process engine for
 * BPM and Worklow functionality. Most such applications will start their own process engine (or use
 * a process engine provided by the runtime container), deploy some BPMN 2.0 process definitions and
 * interact with process instances derived from these process definitions. Since most process applications
 * perform very similar bootstrapping, deployment and runtime tasks, we generalized this functionality.
 * The concept is similar to the javax.ws.rs.core.Application class in JAX-RS: adding the process
 * application class allows you to bootstrap and configure the provided services.</p>
 *
 * <p>Adding a ProcessApplication class to your Java Application provides your applications with the
 * following services:
 *
 * <ul>
 * <li><strong>Bootstrapping</strong> embedded process engine(s) or looking up container managed process engine(s).
 * You can define multiple process engines in a file named processes.xml which is added to your application.
 * The ProcessApplication class makes sure this file is picked up and the defined process engines are started
 * and stopped as the application is deployed / undeployed.</li>
 * <li><strong>Automatic deployment</strong> of classpath BPMN 2.0 resources. You can define multiple deployments
 * (process archives) in the processes.xml file. The process application class makes sure the deployments
 * are performed upon deployment of your application. Scanning your application for process definition
 * resource files (engine in *.bpmn20.xml or *.bpmn) is supported as well.</li>
 * <li><strong>Classloading & Thread context switching:</strong> Resolution of application-local Java Delegate Implementations and Beans in case of a
 * multi-application deployment. The process application class allows your java application to
 * expose your local Java Delegate implementations or Spring / CDI beans to a shared, container managed
 * process engine. This way you can start a single process engine that dispatches to multiple process
 * applications that can be (re-)deployed independently.</li>
 * </ul>
 * </p>
 *
 * <p>Transforming an existing Java Application into a Process Application is easy and non-intrusive.
 * You simply have to add:
 * <ul>
 * <li>A Process Application class: The Process Application class constitutes the interface between
 * your application and the process engine. There are different base classes you can extent to reflect
 * different environments (e.g. Servlet vs. EJB Container):
 * <ul>
 *  <li> {@link ServletProcessApplication}: To be used for Process Applications is a Servlet Container like Apache Tomcat.</li>
 *  <li> {@link EjbProcessApplication}: To be used in a Java EE application server like JBoss, Glassfish or WebSphere Application Server.</li>
 *  <li> {@link EmbeddedProcessApplication}: To be used when embedding the process engine is an ordinary Java SE application.</li>
 *  <li> org.camunda.bpm.engine.spring.application.SpringProcessApplication: To be used for bootstrapping the process application from a Spring Application Context.</li>
 * </ul>
 * </li>
 * <li>A processes.xml file to META-INF: The deployment descriptor file allows to provide a declarative
 * configuration of the deployment(s) this process application makes to the process engine. It can be
 * empty and serve as simple marker file - but it must be present.</li>
 * </ul>
 * </p>
 *
 * @author Daniel Meyer
 *
 */
public interface ProcessApplicationInterface {

  /**
   * <p>Deploy this process application into the runtime container.</p>
   *
   * <strong>NOTE:</strong> on some containers (like JBoss AS 7) the deployment of
   * the process application is performed asynchronously and via introspection at deployment
   * time. This means that there is no guarantee that the process application is fully
   * deployed after this method returns.
   *
   * <p>If you need a post deployment hook, use the {@literal @}{@link PostDeploy}
   * annotation.</p>
   */
  public void deploy();

  /**
   * <p>Undeploy this process application from the runtime container.</p>
   *
   * <p>If your application needs to be notified of the undeployment,
   * add a {@literal @}{@link PreUndeploy} method to your subclass.</p>
   */
  public void undeploy();

  /**
   * @return the name of this process application
   */
  public String getName();

  /**
   * <p>Returns a globally sharable reference to this process application. This reference may be safely passed
   * to the process engine. And other applications.</p>
   *
   * @return a globally sharable reference to this process application.
   */
  public ProcessApplicationReference getReference();

  /**
   * Since {@link #getReference()} may return a proxy object, this method returs the actual, unproxied object and is
   * meant to be called from the {@link #execute(Callable)} method. (ie. from a Callable implementation passed to
   * the method.).
   */
  public ProcessApplicationInterface getRawObject();

  /**
   * The default implementation simply modifies the Context {@link ClassLoader}
   *
   * @param callable to be executed "within" the context of this process application.
   * @return the result of the callback
   */
  public <T> T execute(Callable<T> callable) throws ProcessApplicationExecutionException;

  /**
   * Is invoked instead of {@link #execute(Callable)} if a context is available.
   * The default implementation simply forward the call to
   * {@link #execute(Callable)}. A custom implementation can override the method
   * to hook into the invocation.
   *
   * @param callable to be executed "within" the context of this process application.
   * @param context of the current invocation, can be <code>null</code>
   * @return the result of the callback
   */
  public <T> T execute(Callable<T> callable, InvocationContext context) throws ProcessApplicationExecutionException;

  /**
   * <p>Override this method to provide an environment-specific {@link ClassLoader} to be used by the process
   * engine for loading resources from the process application</p>
   *
   * <p><strong>NOTE: the process engine must <em>never</em> cache any references to this {@link ClassLoader}
   * or to classes obtained through this {@link ClassLoader}.</strong></p>
   *
   * @return the {@link ClassLoader} that can be used to load classes and resources from this process application.
   */
  public ClassLoader getProcessApplicationClassloader();

  /**
   * <p>override this method in order to provide a map of properties.</p>
   *
   * <p>The properties are made available globally through the {@link ProcessApplicationService}</p>
   *
   * @see ProcessApplicationService
   * @see ProcessApplicationInfo#getProperties()
   */
  public Map<String, String> getProperties();

  /**
   * <p>This allows the process application to provide a custom ElResolver to the process engine.</p>
   *
   * <p>The process engine will use this ElResolver whenever it is executing a
   * process in the context of this process application.</p>
   *
   * <p>The process engine must only call this method from Callable implementations passed
   * to {@link #execute(Callable)}</p>
   */
  public ELResolver getElResolver();

  /**
   * <p>Returns an instance of {@link BeanELResolver} that a process application caches.</p>
   * <p>Has to be managed by the process application since {@link BeanELResolver} keeps
   * hard references to classes in a cache.</p>
   */
  public BeanELResolver getBeanElResolver();

  /**
   * <p>Override this method in order to programmatically add resources to the
   * deployment created by this process application.</p>
   *
   * <p>This method is invoked at deployment time once for each process archive
   * deployed by this process application.</p>
   *
   * <p><strong>NOTE:</strong> this method must NOT call the {@link DeploymentBuilder#deploy()}
   * method.</p>
   *
   * @param deploymentBuilder the {@link DeploymentBuilder} used to construct the deployment.
   * @param processArchiveName the name of the processArchive which is currently being deployed.
   */
  public void createDeployment(String processArchiveName, DeploymentBuilder deploymentBuilder);


  /**
   * <p>Allows the process application to provide an {@link ExecutionListener} which is notified about
   * all execution events in all of the process instances deployed by this process application.</p>
   *
   * <p>If this method returns 'null', the process application is not notified about execution events.</p>
   *
   * @return an {@link ExecutionListener} or null.
   */
  public ExecutionListener getExecutionListener();

  /**
   * <p>Allows the process application to provide a {@link TaskListener} which is notified about
   * all Task events in all of the process instances deployed by this process application.</p>
   *
   * <p>If this method returns 'null', the process application is not notified about Task events.</p>
   *
   * @return a {@link TaskListener} or null.
   */
  public TaskListener getTaskListener();

}
