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
package org.camunda.bpm.engine.rest.util;

import org.camunda.bpm.engine.rest.exception.RestException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProvidersUtil {

  public static <T> T resolveFromContext(Providers providers, Class<T> clazz) {
    return resolveFromContext(providers, clazz, null);
  }

  public static <T> T resolveFromContext(Providers providers, Class<T> clazz, Class<?> type) {
    return resolveFromContext(providers, clazz, null, type);
  }

  public static <T> T resolveFromContext(Providers providers, Class<T> clazz, MediaType mediaType, Class<?> type) {
    ContextResolver<T> contextResolver = providers.getContextResolver(clazz, mediaType);

    if (contextResolver == null) {
      throw new RestException("No context resolver found for class " + clazz.getName());
    }

    return contextResolver.getContext(type);
  }
}
