/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.impl.el;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.javax.el.FunctionMapper;
import org.camunda.bpm.engine.impl.util.ReflectUtil;

/**
 * @author Sebastian Menski
 */
public class CommandContextFunctionMapper extends FunctionMapper {

  public static Map<String, Method> COMMAND_CONTEXT_FUNCTION_MAP = null;

  public Method resolveFunction(String prefix, String localName) {
    // Context functions are used un-prefixed
    ensureContextFunctionMapInitialized();
    return COMMAND_CONTEXT_FUNCTION_MAP.get(localName);
  }

  protected void ensureContextFunctionMapInitialized() {
    if (COMMAND_CONTEXT_FUNCTION_MAP == null) {
      synchronized (CommandContextFunctionMapper.class) {
        if (COMMAND_CONTEXT_FUNCTION_MAP == null) {
          COMMAND_CONTEXT_FUNCTION_MAP = new HashMap<String, Method>();
          createMethodBindings();
        }
      }
    }
  }

  protected void createMethodBindings() {
    Class<?> mapperClass = getClass();
    COMMAND_CONTEXT_FUNCTION_MAP.put("currentUser", ReflectUtil.getMethod(mapperClass, "currentUser"));
    COMMAND_CONTEXT_FUNCTION_MAP.put("currentUserGroups", ReflectUtil.getMethod(mapperClass, "currentUserGroups"));
  }

  public static String currentUser() {
    CommandContext commandContext = Context.getCommandContext();
    if (commandContext != null) {
      return commandContext.getAuthenticatedUserId();
    }
    else {
      return null;
    }
  }

  public static List<String> currentUserGroups() {
    CommandContext commandContext = Context.getCommandContext();
    if (commandContext != null) {
      return commandContext.getAuthenticatedGroupIds();
    }
    else {
      return null;
    }
  }

}
