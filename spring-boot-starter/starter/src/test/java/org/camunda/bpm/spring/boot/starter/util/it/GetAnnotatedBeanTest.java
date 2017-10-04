package org.camunda.bpm.spring.boot.starter.util.it;


import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.camunda.bpm.spring.boot.starter.util.GetProcessApplicationNameFromAnnotation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@RunWith(SpringRunner.class)
@SpringBootTest(
  classes = GetAnnotatedBeanTest.WithName.class,
  webEnvironment = NONE
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class GetAnnotatedBeanTest {

  @SpringBootApplication
  @EnableProcessApplication("withNameApplication")
  public static class WithName {

    static class Foo {}

    @Bean
    static Foo foo() {
      return new Foo();
    }

    @Bean
    static Runnable runnable() {
      return () -> {};
    }

  }

  @Autowired
  private ApplicationContext ctx;

  /**
   * see issue #187 ... this failed when static bean definitions where used inside the springBootApplication
   *
   * @throws Exception
   */
  @Test
  public void gets_annotated_bean() throws Exception {
    Optional<GetProcessApplicationNameFromAnnotation.AnnotatedBean> bean = GetProcessApplicationNameFromAnnotation.getAnnotatedBean.apply(ctx);

    assertThat(bean.isPresent()).isTrue();
  }
}
