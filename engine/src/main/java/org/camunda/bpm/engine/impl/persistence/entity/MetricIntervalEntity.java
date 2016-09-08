/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

import java.io.Serializable;
import java.util.Date;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.management.MetricIntervalValue;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class MetricIntervalEntity implements MetricIntervalValue, DbEntity, Serializable {


  protected Date timestamp;

  protected String name;

  protected String reporter;

  protected long value;

  public MetricIntervalEntity(Date timestamp, String name, String reporter) {
    this.timestamp = timestamp;
    this.name = name;
    this.reporter = reporter;
  }

  /**
   * Ctor will be used by Mybatis
   *
   * @param timestamp
   * @param name
   * @param reporter
   */
  public MetricIntervalEntity(Long timestamp, String name, String reporter) {
    this.timestamp = new Date(timestamp);
    this.name = name;
    this.reporter = reporter;
  }

  @Override
  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = new Date(timestamp);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getReporter() {
    return reporter;
  }

  public void setReporter(String reporter) {
    this.reporter = reporter;
  }

  public long getValue() {
    return value;
  }

  public void setValue(long value) {
    this.value = value;
  }

  @Override
  public String getId() {
    return name + reporter + timestamp.toString();
  }

  @Override
  public void setId(String id) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object getPersistentState() {
    return MetricIntervalEntity.class;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 67 * hash + (this.timestamp != null ? this.timestamp.hashCode() : 0);
    hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 67 * hash + (this.reporter != null ? this.reporter.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MetricIntervalEntity other = (MetricIntervalEntity) obj;
    if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
      return false;
    }
    if ((this.reporter == null) ? (other.reporter != null) : !this.reporter.equals(other.reporter)) {
      return false;
    }
    if (this.timestamp != other.timestamp && (this.timestamp == null || !this.timestamp.equals(other.timestamp))) {
      return false;
    }
    return true;
  }

}
