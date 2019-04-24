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
package org.camunda.bpm.spring.boot.starter.property;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

/**
 * @author Nikola Koevski
 */
public class CsrfProperties {

  private String targetOrigin = null;
  private Integer denyStatus = null;
  private String randomClass = null;
  private List<String> entryPoints = new ArrayList<>();

  public String getTargetOrigin() {
    return targetOrigin;
  }

  public void setTargetOrigin(String targetOrigin) {
    this.targetOrigin = targetOrigin;
  }

  public Integer getDenyStatus() {
    return denyStatus;
  }

  public void setDenyStatus(Integer denyStatus) {
    this.denyStatus = denyStatus;
  }

  public String getRandomClass() {
    return randomClass;
  }

  public void setRandomClass(String randomClass) {
    this.randomClass = randomClass;
  }

  public List<String> getEntryPoints() {
    return entryPoints;
  }

  public void setEntryPoints(List<String> entryPoints) {
    this.entryPoints = entryPoints;
  }

  public Map<String, String> getInitParams() {
    Map<String, String> initParams = new HashMap<>(4);

    if (StringUtils.isNotBlank(targetOrigin)) {
      initParams.put("targetOrigin", targetOrigin);
    }

    if (denyStatus != null) {
      initParams.put("denyStatus", denyStatus.toString());
    }

    if (StringUtils.isNotBlank(randomClass)) {
      initParams.put("randomClass", randomClass);
    }

    if (!entryPoints.isEmpty()) {
      initParams.put("entryPoints", StringUtils.join(entryPoints, ","));
    }

    return initParams;
  }

  @Override
  public String toString() {
    return joinOn(this.getClass())
      .add("targetOrigin=" + targetOrigin)
      .add("denyStatus='" + denyStatus + '\'')
      .add("randomClass='" + randomClass + '\'')
      .add("entryPoints='" + entryPoints + '\'')
      .toString();
  }
}
