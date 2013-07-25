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
package org.camunda.bpm.webapp.impl.engine;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.camunda.bpm.engine.rest.exception.ExceptionHandler;
import org.camunda.bpm.engine.rest.exception.ProcessEngineExceptionHandler;
import org.camunda.bpm.engine.rest.exception.RestExceptionHandler;
import org.camunda.bpm.engine.rest.impl.ProcessEngineRestServiceImpl;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

/**
 * The engine rest api exposed by the application.
 *
 * @author nico.rehwaldt
 */
public class EngineRestApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<Class<?>>();

    // only provide named process engine access.
    classes.add(ProcessEngineRestServiceImpl.class);

    // mandatory
    classes.add(JacksonConfigurator.class);
    classes.add(JacksonJsonProvider.class);
    classes.add(RestExceptionHandler.class);
    
    classes.add(ProcessEngineExceptionHandler.class);
    classes.add(ExceptionHandler.class);
    
    return classes;
  }
}
