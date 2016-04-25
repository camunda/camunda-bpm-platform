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

import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.TenantQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

public class DbTenantQueryImpl extends TenantQueryImpl {

  private static final long serialVersionUID = 1L;

  public DbTenantQueryImpl() {
    super();
  }

  public DbTenantQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  //results ////////////////////////////////////////////////////////

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    DbReadOnlyIdentityServiceProvider identityProvider = getIdentityProvider(commandContext);
    return identityProvider.findTenantCountByQueryCriteria(this);
  }

  @Override
  public List<Tenant> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    DbReadOnlyIdentityServiceProvider identityProvider = getIdentityProvider(commandContext);
    return identityProvider.findTenantByQueryCriteria(this);
  }

  protected DbReadOnlyIdentityServiceProvider getIdentityProvider(CommandContext commandContext) {
    return (DbReadOnlyIdentityServiceProvider) commandContext.getReadOnlyIdentityProvider();
  }

}
