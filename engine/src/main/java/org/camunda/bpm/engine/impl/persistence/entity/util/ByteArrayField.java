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

package org.camunda.bpm.engine.impl.persistence.entity.util;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.Nameable;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;

/**
 * A byte array value field what load and save {@link ByteArrayEntity}. It can
 * be used in an entity which implements {@link ValueFields}.
 *
 * @author Philipp Ossler
 */
public class ByteArrayField {

  protected ByteArrayEntity byteArrayValue;
  protected String byteArrayId;

  protected final Nameable nameProvider;

  public ByteArrayField(Nameable nameProvider) {
    this.nameProvider = nameProvider;
  }

  public String getByteArrayId() {
    return byteArrayId;
  }

  public void setByteArrayId(String byteArrayId) {
    this.byteArrayId = byteArrayId;
    this.byteArrayValue = null;
  }

  public byte[] getByteArrayValue() {
    getByteArrayEntity();

    if (byteArrayValue != null) {
      return byteArrayValue.getBytes();
    }
    else {
      return null;
    }
  }

  protected ByteArrayEntity getByteArrayEntity() {

    if (byteArrayValue == null) {
      if (byteArrayId != null) {
        // no lazy fetching outside of command context
        if (Context.getCommandContext() != null) {
          return byteArrayValue = Context
              .getCommandContext()
              .getDbEntityManager()
              .selectById(ByteArrayEntity.class, byteArrayId);
        }
      }
    }

    return byteArrayValue;
  }

  public void setByteArrayValue(byte[] bytes) {
    setByteArrayValue(bytes, false);
  }

  public void setByteArrayValue(byte[] bytes, boolean isTransient) {
    if (bytes != null) {
      // note: there can be cases where byteArrayId is not null
      //   but the corresponding byte array entity has been removed in parallel;
      //   thus we also need to check if the actual byte array entity still exists
      if (this.byteArrayId != null && getByteArrayEntity() != null) {
        byteArrayValue.setBytes(bytes);
      }
      else {
        deleteByteArrayValue();

        byteArrayValue = new ByteArrayEntity(nameProvider.getName(), bytes);

        // avoid insert of byte array value for a transient variable
        if (!isTransient) {
          Context.
          getCommandContext()
          .getDbEntityManager()
          .insert(byteArrayValue);

          byteArrayId = byteArrayValue.getId();
        }
      }
    }
    else {
      deleteByteArrayValue();
    }

  }

  public void deleteByteArrayValue() {
    if (byteArrayId != null) {
      // the next apparently useless line is probably to ensure consistency in the DbSqlSession cache,
      // but should be checked and docked here (or removed if it turns out to be unnecessary)
      getByteArrayEntity();

      if (byteArrayValue != null) {
        Context.getCommandContext()
               .getDbEntityManager()
               .delete(byteArrayValue);
      }

      byteArrayId = null;
    }
  }

  public void setByteArrayValue(ByteArrayEntity byteArrayValue) {
    this.byteArrayValue = byteArrayValue;
  }

}