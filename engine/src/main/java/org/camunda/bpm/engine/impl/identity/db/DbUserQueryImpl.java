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
package org.camunda.bpm.engine.impl.identity.db;

import java.util.List;

import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.UserQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

/**
 * @author Daniel Meyer
 *
 */
public class DbUserQueryImpl extends UserQueryImpl {

  private static final long serialVersionUID = 1L;

  public DbUserQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public DbUserQueryImpl() {
    super();
  }

  // results //////////////////////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    final DbReadOnlyIdentityServiceProvider identityProvider = getIdentityProvider(commandContext);
    return identityProvider.findUserCountByQueryCriteria(this);
  }

  public List<User> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    final DbReadOnlyIdentityServiceProvider identityProvider = getIdentityProvider(commandContext);
    return identityProvider.findUserByQueryCriteria(this);
  }

  private DbReadOnlyIdentityServiceProvider getIdentityProvider(CommandContext commandContext) {
    return (DbReadOnlyIdentityServiceProvider) commandContext.getReadOnlyIdentityProvider();
  }


}
