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
package org.camunda.bpm.engine.test.api.authorization.util;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.slf4j.Logger;

/**
 * @author Thorben Lindhauer
 *
 */
public class AuthorizationExceptionInterceptor extends CommandInterceptor {

private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  protected boolean isActive;
  protected AuthorizationException lastException;

  protected int count = 0;

  public <T> T execute(Command<T> command) {
    try {
      count++; // only catch exception if we are at the top of the command stack
               // (there may be multiple nested command invocations and we need
               // to prevent that this intercepter swallows an exception)
      T result = next.execute(command);
      count--;
      return result;
    }
    catch (AuthorizationException e) {
      count--;
      if (count == 0 && isActive) {
        lastException = e;
        LOG.info("Caught authorization exception; storing for assertion in test", e);
      }
      else {
        throw e;
      }
    }
    return null;
  }

  public void reset() {
    lastException = null;
    count = 0;
  }

  public AuthorizationException getLastException() {
    return lastException;
  }

  public void activate() {
    isActive = true;
  }

  public void deactivate() {
    isActive = false;
  }
}
