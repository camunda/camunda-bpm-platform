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
package org.camunda.bpm.application;

import java.util.concurrent.Callable;

import org.camunda.bpm.application.impl.ProcessApplicationContextImpl;
import org.camunda.bpm.application.impl.ProcessApplicationIdentifier;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessApplicationContext {

  // TODO: javadoc

  public static void setCurrentProcessApplication(String name) {
    ProcessApplicationContextImpl.set(new ProcessApplicationIdentifier(name));
  }

  public static void setCurrentProcessApplication(ProcessApplicationReference reference) {
    ProcessApplicationContextImpl.set(new ProcessApplicationIdentifier(reference));
  }

  public static void setCurrentProcessApplication(ProcessApplicationInterface processApplication) {
    ProcessApplicationContextImpl.set(new ProcessApplicationIdentifier(processApplication));
  }

  public static void clear() {
    ProcessApplicationContextImpl.clear();
  }

  public static <T> T executeInProcessApplication(Callable<T> callable, String name) throws Exception {
    try {
      setCurrentProcessApplication(name);
      return callable.call();
    }
    finally {
      clear();
    }
  }

  public static <T> T executeInProcessApplication(Callable<T> callable, ProcessApplicationReference reference) throws Exception {
    try {
      setCurrentProcessApplication(reference);
      return callable.call();
    }
    finally {
      clear();
    }
  }

  public static <T> T executeInProcessApplication(Callable<T> callable, ProcessApplicationInterface processApplication) throws Exception {
    try {
      setCurrentProcessApplication(processApplication);
      return callable.call();
    }
    finally {
      clear();
    }
  }
}
