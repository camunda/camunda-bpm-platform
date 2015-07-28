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
package org.camunda.bpm.engine.impl.oplog;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Provides information about user operations.</p>
 *
 * <p>One context object can contain many entries. An entry represents one operation on a set of
 * resources of the same type. One such operation can change multiple properties on these entities.
 * For example, more than one entry is needed when a cascading command is logged. Then there is an entry
 * for the changes performed on the addressed resource type as well as entries for those resource types that
 * are affected by the cascading behavior.</p>
 *
 * @author Roman Smirnov
 * @author Thorben Lindhauer
 */
public class UserOperationLogContext {

  protected String operationId;
  protected String userId;
  protected List<UserOperationLogContextEntry> entries;

  public UserOperationLogContext() {
    this.entries = new ArrayList<UserOperationLogContextEntry>();
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getOperationId() {
    return operationId;
  }

  public void setOperationId(String operationId) {
    this.operationId = operationId;
  }

  public void addEntry(UserOperationLogContextEntry entry) {
    entries.add(entry);
  }

  public List<UserOperationLogContextEntry> getEntries() {
    return entries;
  }



}
