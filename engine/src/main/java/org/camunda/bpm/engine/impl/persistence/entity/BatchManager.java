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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.List;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchQueryImpl;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;

public class BatchManager extends AbstractManager {

  public void insertBatch(BatchEntity batch) {
    getDbEntityManager().insert(batch);
  }

  public BatchEntity findBatchById(String id) {
    return getDbEntityManager().selectById(BatchEntity.class, id);
  }

  public long findBatchCountByQueryCriteria(BatchQueryImpl batchQuery) {
    configureQuery(batchQuery);
    return (Long) getDbEntityManager().selectOne("selectBatchCountByQueryCriteria", batchQuery);
  }

  @SuppressWarnings("unchecked")
  public List<Batch> findBatchesByQueryCriteria(BatchQueryImpl batchQuery, Page page) {
    configureQuery(batchQuery);
    return getDbEntityManager().selectList("selectBatchesByQueryCriteria", batchQuery, page);
  }

  protected void configureQuery(BatchQueryImpl batchQuery) {
    // TODO: authorization
    getTenantManager().configureQuery(batchQuery);
  }

}
