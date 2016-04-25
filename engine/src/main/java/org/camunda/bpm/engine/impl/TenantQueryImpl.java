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

package org.camunda.bpm.engine.impl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

public abstract class TenantQueryImpl extends AbstractQuery<TenantQuery, Tenant> implements TenantQuery {

  private static final long serialVersionUID = 1L;
  protected String id;
  protected String[] ids;
  protected String name;
  protected String nameLike;

  public TenantQueryImpl() {
  }

  public TenantQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public TenantQuery tenantId(String id) {
    ensureNotNull("tenant ud", id);
    this.id = id;
    return this;
  }

  public TenantQuery tenantIdIn(String... ids) {
    ensureNotNull("tenant ids", (Object[]) ids);
    this.ids = ids;
    return this;
  }

  public TenantQuery tenantName(String name) {
    ensureNotNull("tenant name", name);
    this.name = name;
    return this;
  }

  public TenantQuery tenantNameLike(String nameLike) {
    ensureNotNull("tenant name like", nameLike);
    this.nameLike = nameLike;
    return this;
  }

  //sorting ////////////////////////////////////////////////////////

  public TenantQuery orderByTenantId() {
    return orderBy(TenantQueryProperty.GROUP_ID);
  }

  public TenantQuery orderByTenantName() {
    return orderBy(TenantQueryProperty.NAME);
  }

  //getters ////////////////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getNameLike() {
    return nameLike;
  }

  public String[] getIds() {
    return ids;
  }

}
