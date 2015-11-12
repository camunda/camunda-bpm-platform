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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.spi.impl.AbstractProcessEngineAware;

import javax.ws.rs.core.Response.Status;

public abstract class AbstractRestProcessEngineAware extends AbstractProcessEngineAware {

  protected ObjectMapper objectMapper;

  protected String relativeRootResourcePath = "/";

  public AbstractRestProcessEngineAware(String engineName, final ObjectMapper objectMapper) {
    super(engineName);
    this.objectMapper = objectMapper;
  }

  protected ProcessEngine getProcessEngine() {
    if (processEngine == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "No process engine available");
    }
    return processEngine;
  }

  /**
   * Override the root resource path, if this resource is a sub-resource.
   * The relative root resource path is used for generation of links to resources in results.
   *
   * @param relativeRootResourcePath
   */
  public void setRelativeRootResourceUri(String relativeRootResourcePath) {
    this.relativeRootResourcePath = relativeRootResourcePath;
  }

  protected ObjectMapper getObjectMapper() {
    return objectMapper;
  }
}
