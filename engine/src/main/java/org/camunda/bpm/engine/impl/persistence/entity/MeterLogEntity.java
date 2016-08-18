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

import java.io.Serializable;
import java.util.Date;

import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.management.Metric;

/**
 * @author Daniel Meyer
 *
 */
public class MeterLogEntity implements Metric, DbEntity, Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;

  protected Date timestamp;

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

}
