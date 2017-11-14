package org.camunda.bpm.spring.boot.starter.annotation;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.SpringBootProcessApplication;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that disables the {@link SpringProcessEngineConfiguration}
 * auto-deploy feature and instead uses the required META-INF/processes.xml
 * as an indicator for resource scanning. This also allows all processes.xml
 * configuration features described here:
 * <a href="https://docs.camunda.org/manual/latest/user-guide/process-applications/the-processes-xml-deployment-descriptor/">The processes.xml Deployment Descriptor</a>
 * 
 * <p>To use it, just add the annotation to your Spring Boot application class:</p>
 * <pre>
 * &#64;SpringBootApplication
 * &#64;EnableProcessApplication("myProcessApplicationName")
 * public class MyApplication {
 * 
 * ...
 * 
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(SpringBootProcessApplication.class)
@Documented
@Inherited
public @interface EnableProcessApplication {

  String value() default "";
}
