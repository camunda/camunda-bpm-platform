package org.camunda.bpm.engine.spring.test;

import org.camunda.bpm.engine.spring.SpringArtifactFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class SpringArtifactFactoryTest implements ApplicationContextAware {

  private ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }


  @Configuration
  static class ContextConfiguration {

    @Bean
    public Foo foo() {
      return new Foo();
    }

    @Bean
    public Bar bar() {
      return new Bar();
    }
  }

  public static class Foo {

    @Autowired
    private Bar bar;
  }

  public static class Bar {
  }

  public static class Baz {

    @Autowired
    private Bar bar;
  }


  @Test
  public void createsInstanceAndInjectsDependencies() {
    Foo foo = new SpringArtifactFactory(applicationContext).getArtifact(Foo.class);

    assertNotNull("instance not created", foo);
    assertNotNull("injection did not work", foo.bar);
  }

  @Test
  public void fallsBackToConstructorWhenBeanNotAvailable() {
    Baz baz = new SpringArtifactFactory(applicationContext).getArtifact(Baz.class);

    assertNotNull("instance not created", baz);
    assertNull("should not have been injected", baz.bar);
  }

}