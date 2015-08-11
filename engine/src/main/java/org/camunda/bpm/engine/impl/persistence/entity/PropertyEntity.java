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

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.db.DbEntity;



/**
 * @author Tom Baeyens
 */
public class PropertyEntity implements DbEntity, HasDbRevision, Serializable {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;
  private static final long serialVersionUID = 1L;

  String name;
  int revision;
  String value;

  public PropertyEntity() {
  }

  public PropertyEntity(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  // persistent object methods ////////////////////////////////////////////////

  public String getId() {
    return name;
  }

  public Object getPersistentState() {
    return value;
  }

  public void setId(String id) {
    throw LOG.notAllowedIdException(id);
  }

  public int getRevisionNext() {
    return revision+1;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[name=" + name
           + ", revision=" + revision
           + ", value=" + value
           + "]";
  }
}
