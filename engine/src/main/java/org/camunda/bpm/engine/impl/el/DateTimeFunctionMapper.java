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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.javax.el.FunctionMapper;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.joda.time.DateTime;

/**
 * @author Sebastian Menski
 */
public class DateTimeFunctionMapper extends FunctionMapper {

  public static Map<String, Method> DATE_TIME_FUNCTION_MAP = null;

  public Method resolveFunction(String prefix, String localName) {
    // Context functions are used un-prefixed
    ensureContextFunctionMapInitialized();
    return DATE_TIME_FUNCTION_MAP.get(localName);
  }

  protected void ensureContextFunctionMapInitialized() {
    if (DATE_TIME_FUNCTION_MAP == null) {
      synchronized (CommandContextFunctionMapper.class) {
        if (DATE_TIME_FUNCTION_MAP == null) {
          DATE_TIME_FUNCTION_MAP = new HashMap<String, Method>();
          createMethodBindings();
        }
      }
    }
  }

  protected void createMethodBindings() {
    Class<?> mapperClass = getClass();
    DATE_TIME_FUNCTION_MAP.put("now", ReflectUtil.getMethod(mapperClass, "now"));
    DATE_TIME_FUNCTION_MAP.put("dateTime", ReflectUtil.getMethod(mapperClass, "dateTime"));
  }

  public static Date now() {
    return ClockUtil.getCurrentTime();
  }

  public static DateTime dateTime() {
    return new DateTime(now());
  }

}
