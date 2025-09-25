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
package org.camunda.bpm.spring.boot.starter.actuator.micrometer.metrics;

import org.camunda.bpm.spring.boot.starter.test.nonpa.TestApplication;
import org.camunda.bpm.spring.boot.starter.util.SpringBootStarterException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import java.util.Map;

public class ActuatorMetricsPropertyInvalidIT {

    @Test
    public void shouldThrowSpringBootStarterExceptionWhenIntervalLessThanNegative1() {
        startApplicationWithInvalidIntervalProperty(-2);
    }

    @Test
    public void shouldThrowSpringBootStarterExceptionWhenIntervalIsZero() {
        startApplicationWithInvalidIntervalProperty(0);
    }

    private void startApplicationWithInvalidIntervalProperty(int interval){
        SpringApplication app = new SpringApplication(TestApplication.class);
        app.setDefaultProperties(Map.of("camunda.bpm.metrics.actuator.interval", interval));
        Exception exception = Assert.assertThrows(Exception.class, app::run);
        Throwable rootCause = findRootCause(exception);
        Assert.assertTrue(rootCause instanceof SpringBootStarterException);
        Assert.assertEquals("Invalid value for camunda.bpm.metrics.actuator.interval: " + interval + ". Value must be -1 or greater than 0.",
                rootCause.getMessage());
    }
    private Throwable findRootCause(Throwable e) {
        while (e.getCause() != null) {
            e = e.getCause();
        }
        return e;
    }
}
