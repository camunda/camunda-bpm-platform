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
package org.camunda.bpm.engine.rest.util;

import java.util.Properties;

import org.camunda.bpm.engine.rest.impl.ProcessDefinitionRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.ProcessEngineRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.ProcessInstanceRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.TaskRestServiceImpl;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;

public class ResteasyServerBootstrap extends EmbeddedServerBootstrap {

  private TJWSEmbeddedJaxrsServer server;
  
  public ResteasyServerBootstrap() {
    setupServer();
  }
  
  public void start() {
    server.start();
  }
  
  public void stop() {
    server.stop();
  }
  
  private void setupServer() {
    Properties serverProperties = readProperties();
    int port = Integer.parseInt(serverProperties.getProperty(PORT_PROPERTY));
    
    server = new TJWSEmbeddedJaxrsServer();
    server.setRootResourcePath(ROOT_RESOURCE_PATH);
    server.setPort(port);
    
    server.getDeployment().getActualResourceClasses().add(ProcessDefinitionRestServiceImpl.class);
    server.getDeployment().getActualResourceClasses().add(ProcessInstanceRestServiceImpl.class);
    server.getDeployment().getActualResourceClasses().add(TaskRestServiceImpl.class);
    server.getDeployment().getActualResourceClasses().add(ProcessEngineRestServiceImpl.class);
    
    server.getDeployment().getActualProviderClasses().add(JacksonConfigurator.class);
    
    server.getDeployment().getActualProviderClasses().add(JacksonJsonProvider.class);
  }

}
