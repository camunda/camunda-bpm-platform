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
package org.camunda.bpm.quarkus.engine.test;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InjectableContext;
import io.quarkus.arc.InjectableInstance;
import io.quarkus.test.QuarkusUnitTest;
import org.camunda.bpm.engine.cdi.annotation.BusinessProcessScoped;
import org.camunda.bpm.quarkus.engine.test.helper.ProcessEngineAwareExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.inject.Named;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BusinessProcessContextTest {

  @RegisterExtension
  protected static final QuarkusUnitTest unitTest = new ProcessEngineAwareExtension()
      .withConfigurationResource("application.properties")
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

  @Test
  public void shouldThrowUnsupportedOperationExceptionOnInjectableContextDestroy() {
    InjectableContext businessProcessContext = Arc.container()
        .getActiveContext(BusinessProcessScoped.class);

    assertThatThrownBy(businessProcessContext::destroy)
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessage("io.quarkus.arc.InjectableContext#destroy is unsupported");
  }

  @Test
  public void shouldThrowUnsupportedOperationExceptionOnInjectableContextGetState() {
    InjectableContext businessProcessContext = Arc.container()
        .getActiveContext(BusinessProcessScoped.class);

    assertThatThrownBy(businessProcessContext::getState)
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessage("io.quarkus.arc.InjectableContext#getState is unsupported");
  }

  @Test
  public void shouldThrowUnsupportedOperationExceptionOnInjectableContextDestroyContextual() {
    Bean<?> bean = Arc.container()
        .beanManager()
        .getBeans(BusinessProcessScopedBean.class)
        .stream()
        .findFirst()
        .get();

    InjectableContext businessProcessContext = Arc.container()
        .getActiveContext(BusinessProcessScoped.class);

    assertThatThrownBy(() -> businessProcessContext.destroy(bean))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessage("io.quarkus.arc.InjectableContext#destroy(contextual) is unsupported");
  }

  @Test
  public void shouldThrowUnsupportedOperationExceptionOnDestroyInjectableInstance() {
    InjectableInstance<BusinessProcessScopedBean> instance = Arc.container()
        .select(BusinessProcessScopedBean.class);
    BusinessProcessScopedBean businessProcessScopedBean = instance.get();

    assertThatThrownBy(() -> instance.destroy(businessProcessScopedBean))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessage("io.quarkus.arc.InjectableContext#destroy(contextual) is unsupported");
  }

  @Named
  @BusinessProcessScoped
  public static class BusinessProcessScopedBean {
  }

}
