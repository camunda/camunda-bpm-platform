/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.welcome.impl.plugin;

import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.welcome.impl.plugin.resources.WelcomePluginsRootResource;
import org.camunda.bpm.welcome.plugin.spi.impl.AbstractWelcomePlugin;

/**
 *
 * @author vale
 */
public class WelcomePlugins extends AbstractWelcomePlugin {

  public static final String ID = "welcomePlugins";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public Set<Class<?>> getResourceClasses() {
    HashSet<Class<?>> classes = new HashSet<Class<?>>();

    classes.add(WelcomePluginsRootResource.class);

    return classes;
  }

  @Override
  public String getAssetDirectory() {
    return "plugin/welcome";
  }

}
