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
package org.camunda.bpm.cockpit.impl.plugin.base.dto.query;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.IncidentDto;
import org.camunda.bpm.cockpit.rest.dto.AbstractRestQueryParametersDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;

/**
 * @author roman.smirnov
 */
public class IncidentQueryDto extends AbstractRestQueryParametersDto<IncidentDto> {
  
  private static final long serialVersionUID = 1L;
    
  protected String[] processInstanceIdIn;
  protected String[] activityIdIn;
  
  public IncidentQueryDto() { }
  
  public IncidentQueryDto(MultivaluedMap<String, String> queryParameters) {
    super(queryParameters);
  }
  
  public String[] getProcessInstanceIdIn() {
    return processInstanceIdIn;
  }

  @CamundaQueryParam(value="processInstanceIdIn", converter = StringArrayConverter.class)
  public void setProcessInstanceIdIn(String[] processInstanceIdIn) {
    this.processInstanceIdIn = processInstanceIdIn;
  }

  public String[] getActivityIdIn() {
    return activityIdIn;
  }

  @CamundaQueryParam(value="activityIdIn", converter = StringArrayConverter.class)
  public void setActivityIdIn(String[] activityIdIn) {
    this.activityIdIn = activityIdIn;
  }

  protected boolean isValidSortByValue(String value) {
    return false;
  }
  
  protected String getOrderByValue(String sortBy) {
    return super.getOrderBy();
  }
}
