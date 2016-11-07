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
package org.camunda.bpm.application;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate a user-provided {@link AbstractProcessApplication} class and specify
 * the unique name of the process application.
 *
 * @author Daniel Meyer
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProcessApplication {

  String DEFAULT_META_INF_PROCESSES_XML = "META-INF/processes.xml";

  /**
   * Allows specifying the name of the process application.
   * Overrides the {@code name} property.
   */
  String value() default "";

  /**
   * Allows specifying the name of the process application.
   * Only applies if the {@code value} property is not set.
   */
  String name() default "";

  /**
   * Returns the location(s) of the <code>processes.xml</code> deployment descriptors.
   * The default value is<code>{META-INF/processes.xml}</code>. The provided path(s)
   * must be resolvable through the {@link ClassLoader#getResourceAsStream(String)}-Method
   * of the classloader returned  by the {@link AbstractProcessApplication#getProcessApplicationClassloader()}
   * method provided by the process application.
   *
   * @return the location of the <code>processes.xml</code> file.
   */
  String[] deploymentDescriptors() default {DEFAULT_META_INF_PROCESSES_XML};

}
