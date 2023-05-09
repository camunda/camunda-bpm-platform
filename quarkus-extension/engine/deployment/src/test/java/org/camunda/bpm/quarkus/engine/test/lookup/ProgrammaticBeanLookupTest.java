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
package org.camunda.bpm.quarkus.engine.test.lookup;

import io.quarkus.test.QuarkusUnitTest;
import org.camunda.bpm.quarkus.engine.test.helper.ProcessEngineAwareExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Specializes;
import jakarta.inject.Named;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test is copied and adjusted from the engine-cdi module to work with Quarkus.
 * See https://jira.camunda.com/browse/CAM-13747 for the reasoning.
 */
public class ProgrammaticBeanLookupTest {

  @RegisterExtension
  static final QuarkusUnitTest unitTest = new ProcessEngineAwareExtension()
      .withConfigurationResource("application.properties")
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
          .addClass(TestBean.class)
          .addClass(OtherTestBean.class)
          .addClass(AlternativeTestBean.class)
          .addClass(SpecializedTestBean.class)
          .addClass(BeanWithProducerMethods.class));

  @Test
  public void shouldLookupBean() {
    Object lookup = ProgrammaticBeanLookup.lookup("testBean");
    assertThat(lookup).isInstanceOf(TestBean.class);
  }

  @Test
  public void shouldFindAlternative() {
    Object lookup = ProgrammaticBeanLookup.lookup("otherTestBean");
    assertThat(lookup).isInstanceOf(AlternativeTestBean.class);
  }

  @Test
  @Disabled("specialization not supported")
  public void shouldFindSpecialization() {
    Object lookup = ProgrammaticBeanLookup.lookup("specializedTestBean");
    assertThat(lookup).isInstanceOf(SpecializedTestBean.class);
  }

  @Test
  public void shouldSupportProducerMethods() {
    assertThat(ProgrammaticBeanLookup.lookup("producedString")).isEqualTo("exampleString");
  }

  @Named
  @Dependent
  public static class TestBean {
  }

  @Named("otherTestBean")
  @Dependent
  public static class OtherTestBean extends TestBean {
  }

  @Alternative
  @Priority(1)
  @Named("otherTestBean")
  @Dependent
  public static class AlternativeTestBean extends OtherTestBean {
  }

  @Dependent
  @Specializes
  public static class SpecializedTestBean extends TestBean {
  }

}
