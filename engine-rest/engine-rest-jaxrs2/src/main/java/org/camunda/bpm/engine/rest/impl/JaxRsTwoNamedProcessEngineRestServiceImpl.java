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

import org.camunda.bpm.engine.rest.FetchAndLockRestService;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @author Tassilo Weidner
 */
@Path(NamedProcessEngineRestServiceImpl.PATH)
public class JaxRsTwoNamedProcessEngineRestServiceImpl extends NamedProcessEngineRestServiceImpl {

  @Path("/{name}" + FetchAndLockRestService.PATH)
  public FetchAndLockRestService fetchAndLock(@PathParam("name") String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    FetchAndLockRestServiceImpl subResource = new FetchAndLockRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

}
