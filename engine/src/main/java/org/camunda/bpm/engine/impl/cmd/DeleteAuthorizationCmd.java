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
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.impl.AuthorizationQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author Daniel Meyer
 *
 */
public class DeleteAuthorizationCmd implements Command<Void> {

  protected String authorizationId;

  public DeleteAuthorizationCmd(String authorizationId) {
    this.authorizationId = authorizationId;
  }

  public Void execute(CommandContext commandContext) {

    final AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();

    AuthorizationEntity authorization = (AuthorizationEntity) new AuthorizationQueryImpl()
      .authorizationId(authorizationId)
      .singleResult();

    ensureNotNull("Authorization for Id '" + authorizationId + "' does not exist", "authorization", authorization);

    authorizationManager.delete(authorization);

    return null;
  }

}
