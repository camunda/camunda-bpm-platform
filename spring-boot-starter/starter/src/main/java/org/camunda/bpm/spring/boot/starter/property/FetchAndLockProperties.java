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

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import static org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

public class FetchAndLockProperties {

  protected boolean uniqueWorkerRequest = false;
  protected Integer queueCapacity = 200;

  public Map<String, String> getInitParams() {
    Map<String, String> initParams = new HashMap<>();

    if (uniqueWorkerRequest) { // default is false
      initParams.put("fetch-and-lock-unique-worker-request", String.valueOf(true));
    }

    if (queueCapacity != 200) {
      initParams.put("fetch-and-lock-queue-capacity", Integer.toString(queueCapacity));
    }

    return initParams;
  }


  public boolean isUniqueWorkerRequest() {
    return uniqueWorkerRequest;
  }

  public void setUniqueWorkerRequest(boolean uniqueWorkerRequest) {
    this.uniqueWorkerRequest = uniqueWorkerRequest;
  }

  public Integer getQueueCapacity() {
    return queueCapacity;
  }

  public void setQueueCapacity(Integer queueCapacity) {
    this.queueCapacity = queueCapacity;
  }

  @Override
  public String toString() {
    StringJoiner joinedString = joinOn(this.getClass())

            .add("uniqueWorkerRequest=" + uniqueWorkerRequest)
            .add("queueCapacity=" + queueCapacity);

    return joinedString.toString();
  }

}
