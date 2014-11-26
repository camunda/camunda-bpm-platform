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
package org.camunda.bpm.engine.impl.context;

import java.util.concurrent.Callable;

import org.camunda.bpm.engine.impl.util.ClassLoaderUtil;

/**
 * Wrapps the user-provided {@link Callable} and records the
 * Thread Context Classloader after the context switch has been performed.
 * This allows detecting if the Thread Context has been manipulated by the container after that
 * (Usually due to cross application EJB invocations).
 *
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationClassloaderInterceptor<T> implements Callable<T> {

  private static ThreadLocal<ClassLoader> PA_CLASSLOADER = new ThreadLocal<ClassLoader>();

  protected Callable<T> delegate;

  public ProcessApplicationClassloaderInterceptor(Callable<T> delegate) {
    this.delegate = delegate;
  }

  public T call() throws Exception {
    try {
      // record thread context right after context switch
      PA_CLASSLOADER.set(ClassLoaderUtil.getContextClassloader());

      // proceed with delegate callable invocation
      return delegate.call();

    }
    finally {
      PA_CLASSLOADER.remove();
    }
  }

  public static ClassLoader getProcessApplicationClassLoader() {
    return PA_CLASSLOADER.get();
  }

}
