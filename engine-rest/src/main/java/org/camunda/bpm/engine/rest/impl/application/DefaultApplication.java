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
package org.camunda.bpm.engine.rest.impl.application;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.camunda.bpm.engine.rest.exception.ProcessEngineExceptionHandler;
import org.camunda.bpm.engine.rest.exception.RestExceptionHandler;
import org.camunda.bpm.engine.rest.impl.AuthorizationRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.ExecutionRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.GroupRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.HistoryRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.IdentityRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.JobRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.MessageRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.ProcessDefinitionRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.ProcessEngineRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.ProcessInstanceRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.TaskRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.UserRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.VariableInstanceRestServiceImpl;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.jaxrs.JsonMappingExceptionMapper;
import org.codehaus.jackson.jaxrs.JsonParseExceptionMapper;

/**
 * <p>Default {@link Application} registering all resources.</p>
 * 
 * <p><strong>NOTE</strong> This class is excluded from the classes-jar, 
 * such that users that want to embed the REST API as a JAR file into a 
 * custom JAX-RS application are able to build a deployment based on their 
 * requirements.</p>
 * 
 * @author Daniel Meyer
 *
 */
@ApplicationPath("/")
public class DefaultApplication extends Application {
  
  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<Class<?>>();
    classes.add(ProcessEngineRestServiceImpl.class);
    classes.add(ProcessDefinitionRestServiceImpl.class);
    classes.add(ProcessInstanceRestServiceImpl.class);
    classes.add(TaskRestServiceImpl.class);
    classes.add(IdentityRestServiceImpl.class);
    classes.add(MessageRestServiceImpl.class);
    classes.add(JobRestServiceImpl.class);    
    classes.add(ExecutionRestServiceImpl.class);
    classes.add(VariableInstanceRestServiceImpl.class);
    classes.add(UserRestServiceImpl.class);
    classes.add(GroupRestServiceImpl.class);
    classes.add(AuthorizationRestServiceImpl.class);
    
    classes.add(JacksonConfigurator.class);
    
    classes.add(JacksonJsonProvider.class);
    classes.add(JsonMappingExceptionMapper.class);
    classes.add(JsonParseExceptionMapper.class);
    
    classes.add(ProcessEngineExceptionHandler.class);
    classes.add(RestExceptionHandler.class);
    
    classes.add(HistoryRestServiceImpl.class);
    
    return classes;
  }

}
