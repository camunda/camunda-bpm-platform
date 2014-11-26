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
package org.camunda.bpm.container.impl.jboss.config;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.FoxFailedJobParseListener;
import org.camunda.bpm.engine.impl.cfg.JtaProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.jobexecutor.FoxFailedJobCommandFactory;
import org.camunda.bpm.engine.impl.persistence.StrongUuidGenerator;

/**
 * 
 * @author Daniel Meyer
 * 
 */
public class ManagedJtaProcessEngineConfiguration extends JtaProcessEngineConfiguration {

  @Override
  protected void init() {
    initCustomJobRetryStrategy();
    super.init();
  }

  protected void initCustomJobRetryStrategy() {
    // hook custom Failed Job Support
    List<BpmnParseListener> customPostBPMNParseListeners = getCustomPostBPMNParseListeners();
    if(customPostBPMNParseListeners==null) {
      customPostBPMNParseListeners = new ArrayList<BpmnParseListener>();
      setCustomPostBPMNParseListeners(customPostBPMNParseListeners);
    }    
    customPostBPMNParseListeners.add(new FoxFailedJobParseListener());
    
    setFailedJobCommandFactory(new FoxFailedJobCommandFactory());
  }

  protected void initIdGenerator() {
    if (idGenerator == null) {
      idGenerator = new StrongUuidGenerator();
    }
  }

}
