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
package org.camunda.spin.plugin.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.javax.el.FunctionMapper;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.spin.Spin;

/**
 * A FunctionMapper which resolves the Spin functions for Expression Language.
 *
 * <p>Lazy loading: This implementation supports lazy loading: the Java Methods
 * are loaded upon the first request.</p>
 *
 * <p>Caching: once the methods are loaded, they are cached in a Map for efficient
 * retrieval.</p>
 *
 * @author Daniel Meyer
 *
 */
public class SpinFunctionMapper extends FunctionMapper {

  public static Map<String, Method> SPIN_FUNCTION_MAP = null;

  public Method resolveFunction(String prefix, String localName) {
    // Spin methods are used un-prefixed
    ensureSpinFunctionMapInitialized();
    return SPIN_FUNCTION_MAP.get(localName);
  }

  protected void ensureSpinFunctionMapInitialized() {
    if(SPIN_FUNCTION_MAP == null) {
      synchronized (SpinFunctionMapper.class) {
        if(SPIN_FUNCTION_MAP == null) {
          SPIN_FUNCTION_MAP = new HashMap<String, Method>();
          createMethodBindings();
        }
      }
    }
  }

  protected void createMethodBindings() {
    Class<?> spinClass =  Spin.class;
    SPIN_FUNCTION_MAP.put("S", ReflectUtil.getMethod(spinClass, "S", Object.class));
    SPIN_FUNCTION_MAP.put("XML", ReflectUtil.getMethod(spinClass, "XML", Object.class));
    SPIN_FUNCTION_MAP.put("JSON", ReflectUtil.getMethod(spinClass, "JSON", Object.class));
  }

}
