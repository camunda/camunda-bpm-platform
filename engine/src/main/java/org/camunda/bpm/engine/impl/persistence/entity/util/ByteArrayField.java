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

  protected final ValueFields valueFields;

  public ByteArrayField(ValueFields valueFields) {
    this.valueFields = valueFields;
  }

  public String getByteArrayId() {
    return byteArrayId;
  }

  public void setByteArrayId(String byteArrayId) {
    this.byteArrayId = byteArrayId;
    this.byteArrayValue = null;
  }

  public byte[] getByteArrayValue() {
    if ((byteArrayValue == null) && (byteArrayId != null)) {
      // no lazy fetching outside of command context
      if (Context.getCommandContext() != null) {
        byteArrayValue = Context
            .getCommandContext()
            .getDbEntityManager()
            .selectById(ByteArrayEntity.class, byteArrayId);
      }
    }

    if (byteArrayValue != null) {
      return byteArrayValue.getBytes();
    }
    else {
      return null;
    }
  }

  public void setByteArrayValue(byte[] bytes) {
    if (bytes != null) {
      if (this.byteArrayId != null && this.byteArrayValue != null) {
        byteArrayValue.setBytes(bytes);
      }
      else {
        deleteByteArrayValue();

        byteArrayValue = new ByteArrayEntity(valueFields.getName(), bytes);
        Context.
          getCommandContext()
          .getDbEntityManager()
          .insert(byteArrayValue);

        byteArrayId = byteArrayValue.getId();
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
      getByteArrayValue();

      Context
        .getCommandContext()
        .getByteArrayManager()
        .deleteByteArrayById(this.byteArrayId);

      byteArrayId = null;
    }
  }

  public void setByteArrayValue(ByteArrayEntity byteArrayValue) {
    this.byteArrayValue = byteArrayValue;
  }

}