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
package org.camunda.bpm.engine.impl.interceptor;

import org.apache.ibatis.exceptions.PersistenceException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;

/**
 * In contrast to {@link CommandContext}, this context holds resources that are only valid
 * during execution of a single command (i.e. the current command or an exception that was thrown
 * during its execution).
 *
 * @author Thorben Lindhauer
 */
public class CommandInvocationContext {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected Throwable throwable;
  protected Command< ? > command;

  public CommandInvocationContext(Command<?> command) {
    this.command = command;
  }

  public Throwable getThrowable() {
    return throwable;
  }

  public Command<?> getCommand() {
    return command;
  }

  public void trySetThrowable(Throwable t) {
    if (this.throwable == null) {
      this.throwable = t;
    }
    else {
      LOG.maskedExceptionInCommandContext(throwable);
    }
  }

  public void rethrow() {
    if (throwable != null) {
      if (throwable instanceof Error) {
        throw (Error) throwable;
      } else if (throwable instanceof PersistenceException) {
        throw new ProcessEngineException("Process engine persistence exception", throwable);
      } else if (throwable instanceof RuntimeException) {
        throw (RuntimeException) throwable;
      } else {
        throw new ProcessEngineException("exception while executing command " + command, throwable);
      }
    }
  }
}
