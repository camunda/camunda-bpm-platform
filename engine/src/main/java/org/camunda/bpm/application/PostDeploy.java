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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.camunda.bpm.engine.ProcessEngine;

/**
 * <p>Annotation that can be placed on a method of a {@link AbstractProcessApplication ProcessApplication} class.</p>
 * 
 * <p>The method will be invoked after the process application has been successfully deployed, meaning that 
 * <ul>
 *  <li>If the process application defines one or more {@link ProcessEngine ProcessEngines}, all process 
 *  engines have been successfully started and can be looked up.</li>
 *  <li>If the process application defines one or more ProcessArchvies (deployments), all deployments have
 *  completed successfully.</li>
 * </ul>
 * </p>
 * 
 * <p><strong>LIMITATION:</strong> the annotation must be placed on a method of the same class carrying the 
 * <code>{@literal @}ProcessApplication</code> annotation. Methods of superclasses are not detected.</p>
 * 
 * <p><strong>NOTE:</strong> A process application class must only define a single <code>{@literal @}PostDeploy</code>
 * Method.</p>
 * 
 * <p><strong>NOTE:</strong> if the {@literal @}PostDeploy method throws an exception, the deployment of the process application will 
 * be rolled back, all process engine deployments will be removed and all process engines defined by this 
 * application will be stopped.</p>
 * 
 * <h2>Basic Usage example:</h2>
 * <pre>
 * {@literal @}ProcessApplication("My Process Application")
 * public class MyProcessApplication extends ServletProcessApplication {
 *  
 *  {@literal @}PostDeploy
 *  public void startProcess(ProcessEngine processEngine) {
 *    processEngine.getRuntimeService()
 *      .startProcessInstanceByKey("invoiceProcess");
 *  }
 *  
 * }
 * </pre>
 * 
 * <p>A method annotated with <code>{@literal @}PostDeploy</code> may additionally take the following set of 
 * parameters, in any oder: 
 * <ul>
 *  <li>{@link ProcessApplicationInfo}: the {@link ProcessApplicationInfo} object for this process application is injected</li>
 *  <li>{@link ProcessEngine} the default process engine is injected</li>
 *  <li>{@code List<ProcessEngine>} all process engines to which this process application has performed deployments are 
 *  injected.</li>
 * </ul>
 * 
 * 
 * @author Daniel Meyer
 * 
 * @see PreUndeploy
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostDeploy {

}
