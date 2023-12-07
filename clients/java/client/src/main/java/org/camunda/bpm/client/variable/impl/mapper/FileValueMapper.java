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
package org.camunda.bpm.client.variable.impl.mapper;

import static org.camunda.bpm.engine.variable.type.FileValueType.VALUE_INFO_FILE_ENCODING;
import static org.camunda.bpm.engine.variable.type.FileValueType.VALUE_INFO_FILE_MIME_TYPE;
import static org.camunda.bpm.engine.variable.type.FileValueType.VALUE_INFO_FILE_NAME;
import static org.camunda.bpm.engine.variable.type.ValueType.FILE;

import java.util.HashMap;
import java.util.Map;

import org.apache.hc.client5.http.utils.Base64;
import org.camunda.bpm.client.impl.EngineClient;
import org.camunda.bpm.client.variable.impl.AbstractTypedValueMapper;
import org.camunda.bpm.client.variable.impl.TypedValueField;
import org.camunda.bpm.client.variable.impl.value.DeferredFileValueImpl;
import org.camunda.bpm.client.variable.value.DeferredFileValue;
import org.camunda.bpm.engine.variable.impl.value.FileValueImpl;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Tassilo Weidner
 */
public class FileValueMapper extends AbstractTypedValueMapper<FileValue> {

  protected EngineClient engineClient;

  public FileValueMapper(EngineClient engineClient) {
    super(FILE);
    this.engineClient = engineClient;
  }

  public FileValue convertToTypedValue(UntypedValueImpl untypedValue) {
    throw new UnsupportedOperationException("Currently no automatic conversation from UntypedValue to FileValue");
  }

  public FileValue readValue(TypedValueField value, boolean deserializeValue) {
    Map<String, Object> valueInfo = value.getValueInfo();

    String filename = (String) valueInfo.get(VALUE_INFO_FILE_NAME);
    DeferredFileValueImpl fileValue = new DeferredFileValueImpl(filename, engineClient);

    String mimeType = (String) valueInfo.get(VALUE_INFO_FILE_MIME_TYPE);
    if (mimeType != null) {
      fileValue.setMimeType(mimeType);
    }

    String encoding = (String) valueInfo.get(VALUE_INFO_FILE_ENCODING);
    if (encoding != null) {
      fileValue.setEncoding(encoding);
    }

    return fileValue;
  }

  public void writeValue(FileValue fileValue, TypedValueField typedValueField) {
    Map<String, Object> valueInfo = new HashMap<>();
    valueInfo.put(VALUE_INFO_FILE_NAME, fileValue.getFilename());

    if (fileValue.getEncoding() != null) {
      valueInfo.put(VALUE_INFO_FILE_ENCODING, fileValue.getEncoding());
    }

    if (fileValue.getMimeType() != null) {
      valueInfo.put(VALUE_INFO_FILE_MIME_TYPE, fileValue.getMimeType());
    }

    typedValueField.setValueInfo(valueInfo);

    byte[] bytes = ((FileValueImpl) fileValue).getByteArray();

    if (bytes != null) {
      typedValueField.setValue(Base64.encodeBase64String(bytes));
    }

  }

  protected boolean canWriteValue(TypedValue typedValue) {
    if (typedValue == null || typedValue.getType() == null) {
      // untyped value
      return false;
    }
    return typedValue.getType().getName().equals(valueType.getName()) && !isDeferred(typedValue);
  }

  protected boolean canReadValue(TypedValueField typedValueField) {
    Object value = typedValueField.getValue();
    return value == null || value instanceof String;
  }

  protected boolean isDeferred(Object variableValue) {
    return variableValue instanceof DeferredFileValue && !((DeferredFileValue) variableValue).isLoaded();
  }

}
