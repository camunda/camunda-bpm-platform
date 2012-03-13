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
package com.camunda.fox.platform.subsystem.impl.extension;

/**
 * Constants used in the model
 * 
 * @author Daniel Meyer
 */
public interface ModelConstants {
  
  public final static String ELEMENT_PROCESS_ENGINES = "process-engines";
  public final static String ELEMENT_PROCESS_ENGINE = "process-engine";
  
  public final static String ATTR_NAME = "name";    
  public final static String ATTR_DATASOURCE = "datasource";
  public final static String ATTR_HISTORY_LEVEL = "history-level";

}
