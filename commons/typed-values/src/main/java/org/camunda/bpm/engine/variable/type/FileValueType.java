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
package org.camunda.bpm.engine.variable.type;

/**
 * @author Ronny Bräunlich
 * @since 7.4
 */
public interface FileValueType extends ValueType {

  /**
   * Identifies the file's name as specified on value creation.
   */
  String VALUE_INFO_FILE_NAME = "filename";

  /**
   * Identifies the file's mime type as specified on value creation.
   */
  String VALUE_INFO_FILE_MIME_TYPE = "mimeType";

  /**
   * Identifies the file's encoding as specified on value creation.
   */
  String VALUE_INFO_FILE_ENCODING = "encoding";

}
