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

import org.camunda.bpm.application.impl.DefaultElResolverLookup;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;
import org.camunda.bpm.engine.impl.util.ClassLoaderUtil;
import org.camunda.bpm.engine.repository.DeploymentBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;


public interface ProcessApplicationInterface {
  public void deploy();
  public void undeploy();
  public String getName();
  public ProcessApplicationReference getReference();
  public <T> T execute(Callable<T> callable) throws ProcessApplicationExecutionException;
  public ClassLoader getProcessApplicationClassloader();
  public Map<String, String> getProperties();
  public ELResolver getElResolver();
  public void createDeployment(String processArchiveName, DeploymentBuilder deploymentBuilder);
}
