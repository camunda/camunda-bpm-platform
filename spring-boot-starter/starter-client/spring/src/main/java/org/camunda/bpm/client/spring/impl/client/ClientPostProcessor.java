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
package org.camunda.bpm.client.spring.impl.client;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.spring.annotation.EnableExternalTaskClient;
import org.camunda.bpm.client.spring.impl.client.util.ClientLoggerUtil;
import org.camunda.bpm.client.spring.impl.util.AnnotationUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ClientPostProcessor implements BeanDefinitionRegistryPostProcessor {

  protected static final ClientLoggerUtil LOG = ClientLoggerUtil.CLIENT_LOGGER;

  protected static final String BEAN_NAME = "externalTaskClient";

  protected Class<? extends ClientFactory> externalTaskClientFactoryClass;

  public ClientPostProcessor() {
    this(ClientFactory.class);
  }

  public ClientPostProcessor(Class<? extends ClientFactory> externalTaskClientFactoryClass) {
    this.externalTaskClientFactoryClass = externalTaskClientFactoryClass;
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    String clientBeanName = getClientBeanName((ListableBeanFactory) registry);
    if (clientBeanName != null) {
      BeanDefinition beanDefinition = registry.getBeanDefinition(clientBeanName);
      String beanClassName = beanDefinition.getBeanClassName();
      LOG.beanCreationSkipped(clientBeanName, beanClassName);
      return;
    }

    String classBeanName = getClassBeanName((ListableBeanFactory) registry);
    ClientConfiguration clientConfiguration = new ClientConfiguration();
    if (classBeanName != null) {
      EnableExternalTaskClient annotation = getAnnotation(registry, classBeanName);
      if (annotation != null) {
        clientConfiguration.fromAnnotation(annotation);
      }
    }

    BeanDefinitionBuilder beanDefinitionBuilder =
        BeanDefinitionBuilder.genericBeanDefinition(externalTaskClientFactoryClass)
            .setDestroyMethodName("close");
    beanDefinitionBuilder.addPropertyValue("clientConfiguration", clientConfiguration);

    BeanDefinition clientBeanDefinition = beanDefinitionBuilder.getBeanDefinition();
    registry.registerBeanDefinition(BEAN_NAME, clientBeanDefinition);

    LOG.registered(BEAN_NAME);
  }

  protected String getClassBeanName(ListableBeanFactory listableBeanFactory) {
    String[] beanNames = listableBeanFactory.getBeanNamesForAnnotation(EnableExternalTaskClient.class);

    List<String> classBeanNames = Arrays.stream(beanNames)
        .filter(isClassAnnotation(listableBeanFactory))
        .collect(Collectors.toList());

    int classBeanNameCount = classBeanNames.size();
    if (classBeanNameCount > 1) {
      throw LOG.noUniqueAnnotation();
    } else if (classBeanNameCount == 1) {
      return classBeanNames.get(0);
    } else {
      // no client annotation in spring boot
      return null;
    }
  }

  Predicate<String> isClassAnnotation(ListableBeanFactory listableBeanFactory) {
    return beanName -> ((BeanDefinitionRegistry) listableBeanFactory)
        .getBeanDefinition(beanName).getSource() == null;
  }

  protected EnableExternalTaskClient getAnnotation(BeanDefinitionRegistry registry, String classBeanName) {
    BeanDefinition beanDefinitionConfig = registry.getBeanDefinition(classBeanName);
    AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinitionConfig;
    AnnotationMetadata metadata = annotatedBeanDefinition.getMetadata();
    return AnnotationUtil.get(EnableExternalTaskClient.class, metadata);
  }

  protected String getClientBeanName(ListableBeanFactory listableBeanFactory) {
    String[] beanNamesForType = listableBeanFactory.getBeanNamesForType(ExternalTaskClient.class);

    if (beanNamesForType.length > 1) {
      throw LOG.noUniqueClientException();
    } else if (beanNamesForType.length == 1) {
      return beanNamesForType[0];
    } else {
      return null;
    }
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
  }

}
