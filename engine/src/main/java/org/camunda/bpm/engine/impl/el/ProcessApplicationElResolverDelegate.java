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
package org.camunda.bpm.engine.impl.el;

import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.javax.el.CompositeELResolver;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;

/**
 * <p>This is an {@link ELResolver} implementation that delegates to a ProcessApplication-provided
 * {@link ELResolver}. The idea is that in a multi-application setup, a shared process engine may orchestrate
 * multiple process applications. In this setting we want to delegate to the current process application
 * for performing expression resolving. This also allows individual process applications to integrate with
 * different kinds of Di Containers or other expression-context providing frameworks. For instance, a first
 * process application may use the spring application context for resolving Java Delegate implementations
 * while a second application may use CDI or even an Apache Camel Context.</p>
 *
 * <p>The behavior of this implementation is as follows: if we are not currently running in the context of
 * a process application, we are skipped. If we are, this implementation delegates to the underlying
 * application-provided {@link ELResolver} which may itself be a {@link CompositeELResolver}.</p>
 *
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationElResolverDelegate extends AbstractElResolverDelegate {

  protected ELResolver getElResolverDelegate() {

    ProcessApplicationReference processApplicationReference = Context.getCurrentProcessApplication();
    if(processApplicationReference != null) {

      try {
        ProcessApplicationInterface processApplication = processApplicationReference.getProcessApplication();
        return processApplication.getElResolver();

      } catch (ProcessApplicationUnavailableException e) {
        throw new ProcessEngineException("Cannot access process application '"+processApplicationReference.getName()+"'", e);
      }

    } else {
      return null;
    }

  }
}
