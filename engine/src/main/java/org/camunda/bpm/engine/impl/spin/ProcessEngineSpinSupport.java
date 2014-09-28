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
package org.camunda.bpm.engine.impl.spin;

import java.util.List;

import org.camunda.bpm.engine.ClassLoadingException;
import org.camunda.bpm.engine.impl.javax.el.FunctionMapper;
import org.camunda.bpm.engine.impl.scripting.env.ScriptEnvResolver;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;

/**
 * <p>Entry point for the process engine spin support.</p>
 *
 * <p>Purposefully, this class has no direct Spin imports. This is to ensure that we can
 * work if spin is not on the process engine classpath. Users should use the {@link #isSpinAvailable()}
 * method for checking if spin is available prior to requesting a Spin SPI implementation.</p>
 *
 * @author Daniel Meyer
 *
 */
public class ProcessEngineSpinSupport {

  /**
   * @return the {@link FunctionMapper} which provides access to spin functions.
   */
  public static FunctionMapper getElFunctionMapper() {
    return (FunctionMapper) ReflectUtil.instantiate("org.camunda.bpm.engine.impl.spin.SpinFunctionMapper");
  }

  /**
   * @return the {@link ScriptEnvResolver} providing support for using spin in scripts
   */
  public static ScriptEnvResolver getScriptEnvResolver() {
    return (ScriptEnvResolver) ReflectUtil.instantiate("org.camunda.bpm.engine.impl.spin.SpinScriptEnvResolver");
  }

  /**
   * @return the {@link SerializationVariableTypeResolver} providing variable
   * types that use Spin's data formats for serialization
   */
  @SuppressWarnings("unchecked")
  public static List<TypedValueSerializer<?>> getSerializers() {
    Object serializers = ReflectUtil.instantiate("org.camunda.bpm.engine.impl.spin.SpinObjectValueSerializers");
    return (List<TypedValueSerializer<?>>) ReflectUtil.invoke(serializers, "getSerializers", new Object[0]);
  }

  /**
   * Allows checking whether spin is found on the classpath
   * @return true if spin is available, false otherwise.
   */
  public static boolean isSpinAvailable() {
    // check whether Spin is on the classpath:
    try {
      ReflectUtil.loadClass("org.camunda.spin.Spin");
      return true;
    } catch (ClassLoadingException e) {
      // ignore exception
      return false;
    }
  }

}
