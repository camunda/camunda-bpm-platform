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

import static com.camunda.fox.platform.subsystem.impl.extension.ModelConstants.ATTR_DATASOURCE;
import static com.camunda.fox.platform.subsystem.impl.extension.ModelConstants.ATTR_HISTORY_LEVEL;
import static com.camunda.fox.platform.subsystem.impl.extension.ModelConstants.ATTR_NAME;
import static com.camunda.fox.platform.subsystem.impl.extension.ModelConstants.ELEMENT_PROCESS_ENGINES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CHILDREN;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEFAULT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HEAD_COMMENT_ALLOWED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MAX_OCCURS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MIN_OCCURS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MODEL_DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAMESPACE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUIRED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TAIL_COMMENT_ALLOWED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;

import java.util.Locale;

import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * 
 * @author Daniel Meyer
 */
public class ModelDescriptionProviders {

  /**
   * Used to create the description of the subsystem
   */
  public static DescriptionProvider SUBSYSTEM = new DescriptionProvider() {

    public ModelNode getModelDescription(Locale locale) {

      final ModelNode subsystem = new ModelNode();
      subsystem.get(DESCRIPTION).set("This subsystem manages fox engine instances and tracks process archives");
      subsystem.get(HEAD_COMMENT_ALLOWED).set(true);
      subsystem.get(TAIL_COMMENT_ALLOWED).set(true);
      subsystem.get(NAMESPACE).set(FoxPlatformExtension.NAMESPACE);

      subsystem.get(CHILDREN, ELEMENT_PROCESS_ENGINES, DESCRIPTION).set("Allows to define a set of process eingine services");
      subsystem.get(CHILDREN, ELEMENT_PROCESS_ENGINES, MIN_OCCURS).set(1);
      subsystem.get(CHILDREN, ELEMENT_PROCESS_ENGINES, MAX_OCCURS).set(1);
      subsystem.get(CHILDREN, ELEMENT_PROCESS_ENGINES, MODEL_DESCRIPTION);

      return subsystem;
    }
  };
  
  public static DescriptionProvider SUBSYSTEM_ADD = new DescriptionProvider() {
    public ModelNode getModelDescription(Locale locale) {      
        final ModelNode node = new ModelNode();
        node.get(DESCRIPTION).set("Adds the fox process engine subsystem");
        return node;
    }
  };
  
  public static DescriptionProvider PROCESS_ENGINE_DEC = new DescriptionProvider() {

    public ModelNode getModelDescription(Locale locale) {
      ModelNode node = new ModelNode();
      node.get(DESCRIPTION).set("The configuration of a process enginge service");
      
      node.get(ATTRIBUTES, ATTR_NAME, DESCRIPTION).set("The name of the process engine - must be unique.");
      node.get(ATTRIBUTES, ATTR_NAME, TYPE).set(ModelType.STRING);
      node.get(ATTRIBUTES, ATTR_NAME, REQUIRED).set(true);
      node.get(ATTRIBUTES, ATTR_NAME, DEFAULT).set("default");
      
      node.get(ATTRIBUTES, ATTR_DATASOURCE, DESCRIPTION).set("The name of the datasource to use.");
      node.get(ATTRIBUTES, ATTR_DATASOURCE, TYPE).set(ModelType.STRING);
      node.get(ATTRIBUTES, ATTR_DATASOURCE, REQUIRED).set(true);
      node.get(ATTRIBUTES, ATTR_DATASOURCE, DEFAULT).set("java:jboss/datasources/ExampleDS");
      
      node.get(ATTRIBUTES, ATTR_HISTORY_LEVEL, DESCRIPTION).set("The history level to use (none | activity | audit* | full).");
      node.get(ATTRIBUTES, ATTR_HISTORY_LEVEL, TYPE).set(ModelType.STRING);
      node.get(ATTRIBUTES, ATTR_HISTORY_LEVEL, REQUIRED).set(false);
      node.get(ATTRIBUTES, ATTR_HISTORY_LEVEL, DEFAULT).set("audit");
      
      return node;
    }
  };

  public static DescriptionProvider PROCESS_ENGINE_ADD = new DescriptionProvider() {
    public ModelNode getModelDescription(Locale locale) {      
        final ModelNode node = new ModelNode();
        node.get(DESCRIPTION).set("Adds a new process engine");
        return node;
    }
  };
  
}
