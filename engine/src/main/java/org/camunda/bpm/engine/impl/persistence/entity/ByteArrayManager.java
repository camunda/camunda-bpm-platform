/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.impl.util.ClockUtil;

/**
 * @author Joram Barrez
 */
public class ByteArrayManager extends AbstractManager {

  /**
   * Deletes the {@link ByteArrayEntity} with the given id from the database.
   * Important: this operation will NOT do any optimistic locking, to avoid loading the
   * bytes in memory. So use this method only in conjunction with an entity that has
   * optimistic locking!.
   */
  public void deleteByteArrayById(String byteArrayEntityId) {
    getDbEntityManager().delete(ByteArrayEntity.class, "deleteByteArrayNoRevisionCheck", byteArrayEntityId);
  }

  public void insertByteArray(ByteArrayEntity arr) {
    arr.setCreateTime(ClockUtil.getCurrentTime());
    getDbEntityManager().insert(arr);
  }

  public DbOperation addRemovalTimeToByteArraysByRootProcessInstanceId(String rootProcessInstanceId, Date removalTime, Integer batchSize) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("rootProcessInstanceId", rootProcessInstanceId);
    parameters.put("removalTime", removalTime);
    parameters.put("maxResults", batchSize);

    return getDbEntityManager()
      .updatePreserveOrder(ByteArrayEntity.class, "updateByteArraysByRootProcessInstanceId", parameters);
  }

  public List<DbOperation> addRemovalTimeToByteArraysByProcessInstanceId(String processInstanceId, Date removalTime, Integer batchSize) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("processInstanceId", processInstanceId);
    parameters.put("removalTime", removalTime);
    parameters.put("maxResults", batchSize);

    // Make individual statements for each entity type that references byte arrays.
    // This can lead to query plans that involve less aggressive locking by databases (e.g. DB2).
    // See CAM-10360 for reference.
    List<DbOperation> operations = new ArrayList<>();
    operations.add(getDbEntityManager()
      .updatePreserveOrder(ByteArrayEntity.class, "updateVariableByteArraysByProcessInstanceId", parameters));
    operations.add(getDbEntityManager()
      .updatePreserveOrder(ByteArrayEntity.class, "updateDecisionInputsByteArraysByProcessInstanceId", parameters));
    operations.add(getDbEntityManager()
      .updatePreserveOrder(ByteArrayEntity.class, "updateDecisionOutputsByteArraysByProcessInstanceId", parameters));
    operations.add(getDbEntityManager()
      .updatePreserveOrder(ByteArrayEntity.class, "updateJobLogByteArraysByProcessInstanceId", parameters));
    operations.add(getDbEntityManager()
      .updatePreserveOrder(ByteArrayEntity.class, "updateExternalTaskLogByteArraysByProcessInstanceId", parameters));
    operations.add(getDbEntityManager()
      .updatePreserveOrder(ByteArrayEntity.class, "updateAttachmentByteArraysByProcessInstanceId", parameters));
    return operations;
  }

  public DbOperation deleteByteArraysByRemovalTime(Date removalTime, int minuteFrom, int minuteTo, int batchSize) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("removalTime", removalTime);
    if (minuteTo - minuteFrom + 1 < 60) {
      parameters.put("minuteFrom", minuteFrom);
      parameters.put("minuteTo", minuteTo);
    }
    parameters.put("batchSize", batchSize);

    return getDbEntityManager()
      .deletePreserveOrder(ByteArrayEntity.class, "deleteByteArraysByRemovalTime",
        new ListQueryParameterObject(parameters, 0, batchSize));
  }

}
