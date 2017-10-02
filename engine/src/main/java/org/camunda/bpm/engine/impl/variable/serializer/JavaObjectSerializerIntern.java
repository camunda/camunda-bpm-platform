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
package org.camunda.bpm.engine.impl.variable.serializer;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.util.StringUtil;

import javax.xml.bind.DatatypeConverter;

/**
 * Uses default java serialization to serialize java objects as byte streams.
 * Uses base64 en-/decoding from javax.xml.bind.DatatypeConverter
 *
 * @author Philipp Moisel
 */
public class JavaObjectSerializerIntern extends JavaObjectSerializer {

    protected String getSerializedStringValue(byte[] serializedByteValue) {
        if (serializedByteValue != null) {
            if (!isSerializationTextBased()) {
                return DatatypeConverter.printBase64Binary(serializedByteValue);
            }
            return StringUtil.fromBytes(serializedByteValue);
        } else {
            return null;
        }
    }

    protected byte[] getSerializedBytesValue(String serializedStringValue) {
        if (serializedStringValue != null) {
            byte[] serializedByteValue = null;
            if (!isSerializationTextBased()) {
                serializedByteValue = DatatypeConverter.parseBase64Binary(serializedStringValue);
            } else {
                serializedByteValue = StringUtil.toByteArray(serializedStringValue);
            }
            return serializedByteValue;
        } else {
            return null;
        }
    }
}
