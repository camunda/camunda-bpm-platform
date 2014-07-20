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
package org.camunda.bpm.engine.rest.impl;

import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.engine.rest.exception.ProcessEngineExceptionHandler;
import org.camunda.bpm.engine.rest.exception.RestExceptionHandler;
import org.camunda.bpm.engine.rest.hal.JacksonHalJsonProvider;
import org.camunda.bpm.engine.rest.impl.history.HistoryRestServiceImpl;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.camunda.bpm.engine.rest.mapper.MultipartPayloadProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.jaxrs.JsonMappingExceptionMapper;
import org.codehaus.jackson.jaxrs.JsonParseExceptionMapper;

/**
 * <p>Class providing static methods returning all the resource classes provided by camunda BPM.</p>
 *
 * @author Daniel Meyer
 *
 */
public class CamundaRestResources {

  private static final Set<Class<?>> RESOURCE_CLASSES = new HashSet<Class<?>>();

  private static final Set<Class<?>> CONFIGURATION_CLASSES = new HashSet<Class<?>>();

  static {
    RESOURCE_CLASSES.add(ProcessEngineRestServiceImpl.class);
    RESOURCE_CLASSES.add(ProcessDefinitionRestServiceImpl.class);
    RESOURCE_CLASSES.add(ProcessInstanceRestServiceImpl.class);
    RESOURCE_CLASSES.add(TaskRestServiceImpl.class);
    RESOURCE_CLASSES.add(IdentityRestServiceImpl.class);
    RESOURCE_CLASSES.add(MessageRestServiceImpl.class);
    RESOURCE_CLASSES.add(JobDefinitionRestServiceImpl.class);
    RESOURCE_CLASSES.add(IncidentRestServiceImpl.class);
    RESOURCE_CLASSES.add(JobRestServiceImpl.class);
    RESOURCE_CLASSES.add(ExecutionRestServiceImpl.class);
    RESOURCE_CLASSES.add(VariableInstanceRestServiceImpl.class);
    RESOURCE_CLASSES.add(UserRestServiceImpl.class);
    RESOURCE_CLASSES.add(GroupRestServiceImpl.class);
    RESOURCE_CLASSES.add(AuthorizationRestServiceImpl.class);
    RESOURCE_CLASSES.add(HistoryRestServiceImpl.class);
    RESOURCE_CLASSES.add(DeploymentRestServiceImpl.class);

    RESOURCE_CLASSES.add(CaseDefinitionRestServiceImpl.class);
    RESOURCE_CLASSES.add(CaseInstanceRestServiceImpl.class);
    RESOURCE_CLASSES.add(CaseExecutionRestServiceImpl.class);

    CONFIGURATION_CLASSES.add(JacksonConfigurator.class);
    CONFIGURATION_CLASSES.add(JacksonJsonProvider.class);
    CONFIGURATION_CLASSES.add(JsonMappingExceptionMapper.class);
    CONFIGURATION_CLASSES.add(JsonParseExceptionMapper.class);
    CONFIGURATION_CLASSES.add(ProcessEngineExceptionHandler.class);
    CONFIGURATION_CLASSES.add(RestExceptionHandler.class);
    CONFIGURATION_CLASSES.add(MultipartPayloadProvider.class);
    CONFIGURATION_CLASSES.add(JacksonHalJsonProvider.class);
  }

  /**
   * Returns a set containing all resource classes provided by camunda BPM.
   * @return a set of resource classes.
   */
  public static Set<Class<?>> getResourceClasses() {
    return RESOURCE_CLASSES;
  }

  /**
   * Returns a set containing all provider / mapper / config classes used in the
   * default setup of the camunda REST api.
   * @return a set of provider / mapper / config classes.
   */
  public static Set<Class<?>> getConfigurationClasses() {
    return CONFIGURATION_CLASSES;
  }

}
