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

package org.camunda.bpm.quarkus.engine.extension;

import io.quarkus.arc.runtime.BeanContainer;

/**
 * Factory for retrieving beans from the BeanContainer.
 */
public class ContainerBeanFactory {

  /**
   * Retrieves a bean of the given class from the bean container.
   *
   * @param beanClass     the class of the desired bean to fetch from the container
   * @param beanContainer the bean container
   * @param <T>           the type of the bean to fetch
   * @return the bean
   */
  public static <T> T getBeanFromContainer(Class<T> beanClass, BeanContainer beanContainer) {
    try (BeanContainer.Instance<T> beanManager = beanContainer.beanInstanceFactory(beanClass).create()) {
      return beanManager.get();
    }
  }
}
