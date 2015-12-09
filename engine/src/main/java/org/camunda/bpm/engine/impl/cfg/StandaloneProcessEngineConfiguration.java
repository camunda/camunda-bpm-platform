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

package org.camunda.bpm.engine.impl.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.impl.interceptor.CommandContextInterceptor;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.camunda.bpm.engine.impl.interceptor.LogInterceptor;
import org.camunda.bpm.engine.impl.interceptor.ProcessApplicationContextInterceptor;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class StandaloneProcessEngineConfiguration extends ProcessEngineConfigurationImpl {

  protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequired() {
    List<CommandInterceptor> defaultCommandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
    defaultCommandInterceptorsTxRequired.add(new LogInterceptor());
    defaultCommandInterceptorsTxRequired.add(new ProcessApplicationContextInterceptor(this));
    defaultCommandInterceptorsTxRequired.add(new CommandContextInterceptor(commandContextFactory, this));
    return defaultCommandInterceptorsTxRequired;
  }

  protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequiresNew() {
    List<CommandInterceptor> defaultCommandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
    defaultCommandInterceptorsTxRequired.add(new LogInterceptor());
    defaultCommandInterceptorsTxRequired.add(new ProcessApplicationContextInterceptor(this));
    defaultCommandInterceptorsTxRequired.add(new CommandContextInterceptor(commandContextFactory, this, true));
    return defaultCommandInterceptorsTxRequired;
  }

}
