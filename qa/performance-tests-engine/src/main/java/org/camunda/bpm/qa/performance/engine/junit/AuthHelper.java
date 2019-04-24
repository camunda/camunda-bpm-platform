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
package org.camunda.bpm.qa.performance.engine.junit;

import java.util.Arrays;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.ProcessEngine;

/**
 * @author Daniel Meyer
 *
 */
public class AuthHelper {

  public static <T> T withAuthentication(Callable<T> callable, ProcessEngine processEngine, String userId, String... groupIds) {
    try {
      processEngine.getProcessEngineConfiguration().setAuthorizationEnabled(true);
      processEngine.getIdentityService().setAuthentication(userId, Arrays.asList(groupIds));

      return callable.call();

    }
    catch (Throwable t) {

      if (t instanceof RuntimeException) {
        throw (RuntimeException) t;
      }
      else {
        throw new RuntimeException(t);
      }

    }
    finally {
      processEngine.getIdentityService().clearAuthentication();
      processEngine.getProcessEngineConfiguration().setAuthorizationEnabled(false);
    }
  }

}
