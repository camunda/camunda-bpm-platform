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
package org.camunda.bpm.cockpit.impl.plugin.base.dto;

import java.util.List;

public class ProcessDefinitionDto {
  
  protected String id;
  protected String name;
  protected String key;
  protected long version;
  protected List<String> calledFromActivityIds;
  
  protected int failedJobs;
  
  public ProcessDefinitionDto() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }
  
  public long getVersion() {
    return version;
  }
  
  public void setVersion(long version) {
    this.version = version;
  }

  public List<String> getCalledFromActivityIds() {
    return calledFromActivityIds;
  }

  public void setCalledFromActivityIds(List<String> calledFromActivityIds) {
    this.calledFromActivityIds = calledFromActivityIds;
  }
}
