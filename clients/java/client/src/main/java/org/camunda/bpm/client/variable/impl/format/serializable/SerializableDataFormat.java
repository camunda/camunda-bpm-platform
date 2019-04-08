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
package org.camunda.bpm.client.variable.impl.format.serializable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.camunda.bpm.client.spi.DataFormat;
import org.camunda.commons.utils.IoUtil;

public class SerializableDataFormat implements DataFormat {

  private static final SerializableLogger LOG = ExternalTaskClientLogger.SERIALIZABLE_FORMAT_LOGGER;

  protected String name;

  public SerializableDataFormat(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public boolean canMap(Object value) {
    return value instanceof Serializable;
  }

  public String writeValue(Object value) {
    ByteArrayOutputStream baos = null;
    ObjectOutputStream ois = null;

    try {
      baos = new ByteArrayOutputStream();
      ois = new ObjectOutputStream(baos);

      ois.writeObject(value);
      byte[] deserializedObjectByteArray = baos.toByteArray();

      Encoder encoder = Base64.getEncoder();
      return encoder.encodeToString(deserializedObjectByteArray);
    }
    catch (IOException e) {
      throw LOG.unableToWriteValue(value, e);
    }
    finally {
      IoUtil.closeSilently(ois);
      IoUtil.closeSilently(baos);
    }
  }

  public <T> T readValue(String value, String typeIdentifier) {
    return readValue(value);
  }

  public <T> T readValue(String value, Class<T> cls) {
    return readValue(value);
  }

  @SuppressWarnings("unchecked")
  protected <T> T readValue(String value) {
    Decoder decoder = Base64.getDecoder();
    byte[] base64DecodedSerializedValue = decoder.decode(value);

    InputStream is = null;
    ObjectInputStream ois = null;

    try {
      is = new ByteArrayInputStream(base64DecodedSerializedValue);
      ois = new ObjectInputStream(is);
      return (T) ois.readObject();
    }
    catch (ClassNotFoundException e) {
      throw LOG.classNotFound(e);
    }
    catch (IOException e) {
      throw LOG.unableToReadValue(value, e);
    }
    finally {
      IoUtil.closeSilently(ois);
      IoUtil.closeSilently(is);
    }
  }

  public String getCanonicalTypeName(Object value) {
    return value.getClass().getName();
  }

}
