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
package org.camunda.bpm.engine.cdi.impl.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandContextListener;

/**
 * {@link CommandContextCloseListener} which releases a CDI Creational Context when the command context is closed.
 * This is necessary to ensure that {@link Dependent} scoped beans are properly destroyed.
 *
 * @author Daniel Meyer
 *
 */
public class CreationalContextReleaseListener implements CommandContextListener {

  protected final static Logger LOG = Logger.getLogger(CreationalContextReleaseListener.class.getName());

  protected CreationalContext<?> context;

  public CreationalContextReleaseListener(CreationalContext<?> ctx) {
    context = ctx;
  }

  public void onCommandContextClose(CommandContext commandContext) {
    release(context);
  }

  public void onCommandFailed(CommandContext commandContext, Throwable t) {
    // ignore
  }

  protected void release(CreationalContext<?> creationalContext) {
    try {
      creationalContext.release();
    } catch(Exception e) {
      LOG.log(Level.WARNING, "Exception while releasing CDI creational context "+e.getMessage(), e);
    }
  }

}
