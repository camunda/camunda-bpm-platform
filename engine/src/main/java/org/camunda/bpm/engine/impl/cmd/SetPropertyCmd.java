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
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyManager;

/**
 * <p>Command which can be used for setting the value of a property</p>
 *
 * @author Daniel Meyer
 *
 */
public class SetPropertyCmd implements Command<Object> {

  protected String name;
  protected String value;

  public SetPropertyCmd(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public Object execute(CommandContext commandContext) {

    final PropertyManager propertyManager = commandContext.getPropertyManager();

    PropertyEntity property = propertyManager
      .findPropertyById(name);
    if(property != null) {
      // update
      property.setValue(value);

    } else {
      // create
      property = new PropertyEntity(name, value);
      propertyManager.insert(property);

    }

    return null;
  }

}
