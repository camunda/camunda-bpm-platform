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
package org.camunda.bpm.engine.impl.batch.history;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;

import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.batch.history.HistoricBatchQuery;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.HistoricBatchQueryProperty;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

public class HistoricBatchQueryImpl extends AbstractQuery<HistoricBatchQuery, HistoricBatch> implements HistoricBatchQuery {

  private static final long serialVersionUID = 1L;

  protected String batchId;
  protected String type;
  protected Boolean completed;
  protected boolean isTenantIdSet = false;
  protected String[] tenantIds;

  public HistoricBatchQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public HistoricBatchQuery batchId(String batchId) {
    ensureNotNull("Batch id", batchId);
    this.batchId = batchId;
    return this;
  }

  public String getBatchId() {
    return batchId;
  }

  public HistoricBatchQuery type(String type) {
    ensureNotNull("Type", type);
    this.type = type;
    return this;
  }

  public HistoricBatchQuery completed(boolean completed) {
    this.completed = completed;
    return this;
  }

  public HistoricBatchQuery tenantIdIn(String... tenantIds) {
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

  public HistoricBatchQuery withoutTenantId() {
    this.tenantIds = null;
    isTenantIdSet = true;
    return this;
  }

  public String getType() {
    return type;
  }

  public HistoricBatchQuery orderById() {
    return orderBy(HistoricBatchQueryProperty.ID);
  }

  public HistoricBatchQuery orderByStartTime() {
    return orderBy(HistoricBatchQueryProperty.START_TIME);
  }

  public HistoricBatchQuery orderByEndTime() {
    return orderBy(HistoricBatchQueryProperty.END_TIME);
  }

  @Override
  public HistoricBatchQuery orderByTenantId() {
    return orderBy(HistoricBatchQueryProperty.TENANT_ID);
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getHistoricBatchManager()
    .findBatchCountByQueryCriteria(this);
  }


  @Override
  public List<HistoricBatch> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getHistoricBatchManager()
      .findBatchesByQueryCriteria(this, page);
  }
}
