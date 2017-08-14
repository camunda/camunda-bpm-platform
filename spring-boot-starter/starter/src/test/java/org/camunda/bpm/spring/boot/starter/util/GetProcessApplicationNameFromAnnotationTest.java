package org.camunda.bpm.spring.boot.starter.util;

import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.camunda.bpm.spring.boot.starter.util.GetProcessApplicationNameFromAnnotation.AnnotatedBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetProcessApplicationNameFromAnnotationTest {

  @EnableProcessApplication("withNameApplication")
  public static class WithName {
  }

  @EnableProcessApplication
  public static class NoName {
  }

  private final ApplicationContext applicationContext = mock(ApplicationContext.class);
  private final GetProcessApplicationNameFromAnnotation function = GetProcessApplicationNameFromAnnotation.processApplicationNameFromAnnotation(applicationContext);

  @Test
  public void getBean_WithName() throws Exception {
    WithName w = new WithName();
    when(applicationContext.getBeansWithAnnotation(EnableProcessApplication.class)).thenReturn(Variables.putValue("withName", w));
    assertThat(GetProcessApplicationNameFromAnnotation.getAnnotatedBean.apply(applicationContext).get()).isEqualTo(AnnotatedBean.of("withName", w));
  }

  @Test
  public void getBean_NoName() throws Exception {
    NoName n = new NoName();
    when(applicationContext.getBeansWithAnnotation(EnableProcessApplication.class)).thenReturn(Variables.putValue("noName", n));
    assertThat(GetProcessApplicationNameFromAnnotation.getAnnotatedBean.apply(applicationContext).get()).isEqualTo(AnnotatedBean.of("noName", n.getClass().getAnnotation(EnableProcessApplication.class)));
  }


  @Test
  public void getAnnotation() throws Exception {

    assertThat(WithName.class.getAnnotation(EnableProcessApplication.class)).isNotNull();
    assertThat(NoName.class.getAnnotation(EnableProcessApplication.class)).isNotNull();
  }

  @Test
  public void findProcessEngineNameForValue() throws Exception {
    when(applicationContext.getBeansWithAnnotation(EnableProcessApplication.class)).thenReturn(Variables.putValue("withName", new WithName()));
    assertThat(GetProcessApplicationNameFromAnnotation.getProcessApplicationName.apply(applicationContext).get()).isEqualTo("withNameApplication");
  }

  @Test
  public void returnEmptyWhenNoNameIsGiven() throws Exception {
    when(applicationContext.getBeansWithAnnotation(EnableProcessApplication.class)).thenReturn(Variables.putValue("noName", new NoName()));
    assertThat(GetProcessApplicationNameFromAnnotation.getProcessApplicationName.apply(applicationContext).get()).isEqualTo("noName");
  }

  @Test
  public void getProcessApplicationNameFromContext_valuePresent() throws Exception {
    assumeAnnotatedBeans(Variables.putValue("app", new WithName()));

    assertThat(function.apply(Optional.empty())).isEqualTo(Optional.of("withNameApplication"));
  }

  @Test
  public void getProcessApplicationNameFromContext_beanName() throws Exception {
    assumeAnnotatedBeans(Variables.putValue("app2", new NoName()));

    assertThat(function.apply(Optional.empty())).isEqualTo(Optional.of("app2"));
  }

  @Test
  public void getProcessApplicationNameFromContext_annotationNotPresent() throws Exception {
    assumeAnnotatedBeans(Variables.createVariables());

    assertThat(function.apply(Optional.empty())).isEqualTo(Optional.empty());
  }

  @Test
  public void getProcessApplicationNameFromContext_annotationNotPresent_fallbackProperty() throws Exception {
    assumeAnnotatedBeans(Variables.createVariables());

    assertThat(function.apply(Optional.of("foo"))).isEqualTo(Optional.of("foo"));
  }

  private void assumeAnnotatedBeans(VariableMap beans) {
    when(applicationContext.getBeansWithAnnotation(EnableProcessApplication.class)).thenReturn(beans);
  }
}
