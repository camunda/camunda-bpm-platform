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

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbReferences;

public class TaskMeterLogEntity implements DbEntity, HasDbReferences, Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;

  protected Date timestamp;

  protected long assigneeHash;

  public TaskMeterLogEntity(String assignee, Date timestamp) {
    this.assigneeHash = createHashAsLong(assignee);
    this.timestamp = timestamp;
  }

  protected long createHashAsLong(String assignee) {
    String algorithm = "MD5";
    try {
      // create a 64 bit (8 byte) hash from an MD5 hash (128 bit) and convert to a long (8 byte)
      MessageDigest digest = MessageDigest.getInstance(algorithm);
      digest.update(assignee.getBytes(StandardCharsets.UTF_8));
      return ByteBuffer.wrap(digest.digest(), 0, 8).getLong();
    } catch (NoSuchAlgorithmException e) {
      throw new ProcessEngineException("Cannot lookup hash algorithm '" + algorithm + "'");
    }
  }

  public TaskMeterLogEntity() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public long getAssigneeHash() {
    return assigneeHash;
  }

  public void setAssigneeHash(long assigneeHash) {
    this.assigneeHash = assigneeHash;
  }

  public Object getPersistentState() {
    // immutable
    return TaskMeterLogEntity.class;
  }

  @Override
  public Set<String> getReferencedEntityIds() {
    Set<String> referencedEntityIds = new HashSet<>();
    return referencedEntityIds;
  }

  @Override
  public Map<String, Class> getReferencedEntitiesIdAndClass() {
    Map<String, Class> referenceIdAndClass = new HashMap<>();
    return referenceIdAndClass;
  }
}
