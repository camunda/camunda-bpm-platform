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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbReferences;

/**
 * @author Daniel Meyer
 *
 */
public class MeterLogEntity implements DbEntity, HasDbReferences, Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;

  protected Date timestamp;
  protected Long milliseconds;

  protected String name;

  protected String reporter;

  protected long value;

  public MeterLogEntity(String name, long value, Date timestamp) {
    this(name, null, value, timestamp);
  }

  public MeterLogEntity(String name, String reporter, long value, Date timestamp) {
    this.name = name;
    this.reporter = reporter;
    this.value = value;
    this.timestamp = timestamp;
    this.milliseconds = timestamp.getTime();
  }

  public MeterLogEntity() {
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

  public Long getMilliseconds() {
    return milliseconds;
  }

  public void setMilliseconds(Long milliseconds) {
    this.milliseconds = milliseconds;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getValue() {
    return value;
  }

  public void setValue(long value) {
    this.value = value;
  }

  public String getReporter() {
    return reporter;
  }

  public void setReporter(String reporter) {
    this.reporter = reporter;
  }

  public Object getPersistentState() {
    // immutable
    return MeterLogEntity.class;
  }

  @Override
  public Set<String> getReferencedEntityIds() {
    Set<String> referencedEntityIds = new HashSet<String>();
    return referencedEntityIds;
  }

  @Override
  public Map<String, Class> getReferencedEntitiesIdAndClass() {
    Map<String, Class> referenceIdAndClass = new HashMap<String, Class>();
    return referenceIdAndClass;
  }
}
