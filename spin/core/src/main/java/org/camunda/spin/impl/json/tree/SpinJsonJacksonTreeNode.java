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
package org.camunda.spin.impl.json.tree;

import java.io.OutputStream;
import java.io.Writer;

import org.camunda.spin.json.SpinJsonNode;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Wrapper for a Jackson Json Tree Node. 
 * 
 * @author Thorben Lindhauer
 */
public class SpinJsonJacksonTreeNode extends SpinJsonNode {

  protected final JsonNode jsonNode;
  
  public SpinJsonJacksonTreeNode(JsonNode jsonNode) {
    this.jsonNode = jsonNode;
  }
  
  public String getDataFormatName() {
    // TODO Auto-generated method stub
    return null;
  }

  public JsonNode unwrap() {
    return jsonNode;
  }

  public String toString() {
    // TODO Auto-generated method stub
    return null;
  }

  public OutputStream toStream() {
    // TODO Auto-generated method stub
    return null;
  }

  public <S extends OutputStream> S writeToStream(S outputStream) {
    // TODO Auto-generated method stub
    return null;
  }

  public <W extends Writer> W writeToWriter(W writer) {
    // TODO Auto-generated method stub
    return null;
  }

}
