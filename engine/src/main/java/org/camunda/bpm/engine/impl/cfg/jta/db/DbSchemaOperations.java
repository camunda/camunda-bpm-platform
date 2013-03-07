/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.cfg.jta.db;

import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;

/**
 * <p>This is a process engine configuration allowing to perform schema operations on 
 * XA-Datasources using jdbc transactions.</p>
 *  
 * @author Daniel Meyer
 */
public class DbSchemaOperations extends StandaloneProcessEngineConfiguration {
  
  protected boolean isInitialized;
     
  public void update() {    
    ensureInitialization(); 
    commandExecutorTxRequired.execute(new SchemaUpdateCmd());    
  }
  
  public void drop() {
    ensureInitialization(); 
    commandExecutorTxRequired.execute(new SchemaDropCmd());
  }

  protected void ensureInitialization() {
    if(!isInitialized) {
      init();
      isInitialized = true;
    }
  }

}
