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
package org.camunda.bpm.engine.rest.impl.fetchAndLock;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.rest.impl.AbstractRestProcessEngineAware;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

/**
 * @author Tassilo Weidner
 */
public class FetchAndLockRestServiceImpl extends AbstractRestProcessEngineAware implements FetchAndLockRestService {

  private FetchAndLockHandler fetchAndLockHandler;

  public FetchAndLockRestServiceImpl(String processEngine, ObjectMapper objectMapper, FetchAndLockHandler fetchAndLockHandler) {
    super(processEngine, objectMapper);
    this.fetchAndLockHandler = fetchAndLockHandler;
  }

  @Override
  public void fetchAndLock(FetchExternalTasksExtendedDto dto, @Suspended final AsyncResponse asyncResponse, @Context HttpHeaders headers) {
    fetchAndLockHandler.addPendingRequest(dto, asyncResponse, headers, processEngine);
  }

}
