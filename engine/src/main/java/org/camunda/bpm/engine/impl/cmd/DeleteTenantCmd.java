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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

public class DeleteTenantCmd extends AbstractWritableIdentityServiceCmd<Void>  implements Command<Void>, Serializable  {

  private static final long serialVersionUID = 1L;

  protected final String tenantId;

  public DeleteTenantCmd(String tenantId) {
    ensureNotNull("groupId", tenantId);

    this.tenantId = tenantId;
  }

  @Override
  protected Void executeCmd(CommandContext commandContext) {
    commandContext
      .getWritableIdentityProvider()
      .deleteTenant(tenantId);

    return null;
  }

}
