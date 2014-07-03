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

import java.io.Serializable;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author Tom Baeyens
 */
public class CreateUserCmd extends AbstractWritableIdentityServiceCmd<User> implements Command<User>, Serializable {

  private static final long serialVersionUID = 1L;
  
  protected String userId;
  
  public CreateUserCmd(String userId) {
    ensureNotNull("userId", userId);
    this.userId = userId;
  }
  
  protected User executeCmd(CommandContext commandContext) {
    return commandContext
      .getWritableIdentityProvider()
      .createNewUser(userId);
  }
}
