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

import org.camunda.bpm.spring.boot.starter.util.SpringBootStarterException;

public class ActuatorProperty {

    private Integer interval = -1; // Default value

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        if (interval == null || interval < -1 || interval == 0) {
            throw new SpringBootStarterException("Invalid value for camunda.bpm.metrics.actuator.interval: " + interval +
                    ". Value must be -1 or greater than 0.");
        }
        this.interval = interval;
    }

    @Override
    public String toString() {
        return "ActuatorProperty[interval=" + interval + "]";
    }
}
