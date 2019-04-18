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
package org.camunda.bpm.engine.impl.pvm.process;

import java.util.ArrayList;
import java.util.List;


/**
 * A BPMN 2.0 LaneSet, containg {@link Lane}s, currently only used for
 * rendering the DI info.
 * 
 * @author Frederik Heremans
 */
public class LaneSet {

  protected String id;
  protected List<Lane> lanes;
  protected String name;
    
  public void setId(String id) {
    this.id = id;
  }
  
  public String getId() {
    return id;
  }
  
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }

  
  public List<Lane> getLanes() {
    if(lanes == null) {
      lanes = new ArrayList<Lane>();
    }
    return lanes;
  }
  
  public void addLane(Lane laneToAdd) {
    getLanes().add(laneToAdd);
  }
  
  public Lane getLaneForId(String id) {
    if(lanes != null && lanes.size() > 0) {
      for(Lane lane : lanes) {
        if(id.equals(lane.getId())) {
          return lane;
        }
      }
    }
    return null;
  }
}