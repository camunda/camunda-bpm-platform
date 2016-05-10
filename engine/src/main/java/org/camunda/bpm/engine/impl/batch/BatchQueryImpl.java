/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.batch;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.BatchQuery;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.BatchQueryProperty;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;

public class BatchQueryImpl extends AbstractQuery<BatchQuery, Batch> implements BatchQuery {

  private static final long serialVersionUID = 1L;

  protected String batchId;
  protected String type;
  protected boolean isTenantIdSet = false;
  protected String[] tenantIds;
  protected SuspensionState suspensionState;

  public BatchQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public BatchQuery batchId(String batchId) {
    ensureNotNull("Batch id", batchId);
    this.batchId = batchId;
    return this;
  }

  public String getBatchId() {
    return batchId;
  }

  public BatchQuery type(String type) {
    ensureNotNull("Type", type);
    this.type = type;
    return this;
  }

  public String getType() {
    return type;
  }

  public BatchQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    isTenantIdSet = true;
    return this;
  }

  public String[] getTenantIds() {
    return tenantIds;
  }

  public boolean isTenantIdSet() {
    return isTenantIdSet;
  }

  public BatchQuery withoutTenantId() {
    this.tenantIds = null;
    isTenantIdSet = true;
    return this;
  }

  public BatchQuery active() {
    this.suspensionState = SuspensionState.ACTIVE;
    return this;
  }

  public BatchQuery suspended() {
    this.suspensionState = SuspensionState.SUSPENDED;
    return this;
  }

  public SuspensionState getSuspensionState() {
    return suspensionState;
  }

  public BatchQuery orderById() {
    return orderBy(BatchQueryProperty.ID);
  }

  @Override
  public BatchQuery orderByTenantId() {
    return orderBy(BatchQueryProperty.TENANT_ID);
  }

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext.getBatchManager()
      .findBatchCountByQueryCriteria(this);
  }

  public List<Batch> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext.getBatchManager()
      .findBatchesByQueryCriteria(this, page);
  }

}
