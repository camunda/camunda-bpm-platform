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

import org.camunda.bpm.engine.batch.BatchStatistics;
import org.camunda.bpm.engine.batch.BatchStatisticsQuery;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.BatchQueryProperty;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;

public class BatchStatisticsQueryImpl extends AbstractQuery<BatchStatisticsQuery, BatchStatistics> implements BatchStatisticsQuery {

  protected static final long serialVersionUID = 1L;

  protected String batchId;
  protected String type;
  protected boolean isTenantIdSet = false;
  protected String[] tenantIds;
  protected SuspensionState suspensionState;

  public BatchStatisticsQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public BatchStatisticsQuery batchId(String batchId) {
    ensureNotNull("Batch id", batchId);
    this.batchId = batchId;
    return this;
  }

  public String getBatchId() {
    return batchId;
  }

  public BatchStatisticsQuery type(String type) {
    ensureNotNull("Type", type);
    this.type = type;
    return this;
  }

  public String getType() {
    return type;
  }

  public BatchStatisticsQuery tenantIdIn(String... tenantIds) {
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

  public BatchStatisticsQuery withoutTenantId() {
    this.tenantIds = null;
    isTenantIdSet = true;
    return this;
  }

  public BatchStatisticsQuery active() {
    this.suspensionState = SuspensionState.ACTIVE;
    return this;
  }

  public BatchStatisticsQuery suspended() {
    this.suspensionState = SuspensionState.SUSPENDED;
    return this;
  }

  public SuspensionState getSuspensionState() {
    return suspensionState;
  }

  public BatchStatisticsQuery orderById() {
    return orderBy(BatchQueryProperty.ID);
  }

  @Override
  public BatchStatisticsQuery orderByTenantId() {
    return orderBy(BatchQueryProperty.TENANT_ID);
  }

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getStatisticsManager()
      .getStatisticsCountGroupedByBatch(this);
  }

  public List<BatchStatistics> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getStatisticsManager()
      .getStatisticsGroupedByBatch(this, page);
  }

}
