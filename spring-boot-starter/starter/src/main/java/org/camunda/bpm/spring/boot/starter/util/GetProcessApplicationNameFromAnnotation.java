package org.camunda.bpm.spring.boot.starter.util;

import org.apache.commons.lang.StringUtils;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.apache.catalina.manager.StatusTransformer.filter;

public class GetProcessApplicationNameFromAnnotation implements Supplier<Optional<String>>, UnaryOperator<Optional<String>> {

  public static GetProcessApplicationNameFromAnnotation processApplicationNameFromAnnotation(final ApplicationContext applicationContext) {
    return new GetProcessApplicationNameFromAnnotation(applicationContext);
  }

  private static final Predicate<Entry<String,Object>> ANNOTATED_WITH_ENABLEPROCESSAPPLICATION = e -> Optional.ofNullable(e.getValue())
    .map(Object::getClass)
    .map(c -> c.isAnnotationPresent(EnableProcessApplication.class))
    .orElse(false);

  private final ApplicationContext applicationContext;

  private GetProcessApplicationNameFromAnnotation(final ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public static class AnnotatedBean {

    String name;
    EnableProcessApplication annotation;

    public AnnotatedBean(String name, EnableProcessApplication annotation) {
      this.name = name;
      this.annotation = annotation;
    }

    public static AnnotatedBean of(String name, EnableProcessApplication annotation) {
      return new AnnotatedBean(name, annotation);
    }

    public static AnnotatedBean of(String name, Object instance) {
      return of(name, instance.getClass().getAnnotation(EnableProcessApplication.class));
    }

    public static AnnotatedBean ofEntry(Entry<String,Object> entry) {
      return of(entry.getKey(), entry.getValue());
    }

    public String getName() {
      return name;
    }

    public EnableProcessApplication getAnnotation() {
      return annotation;
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, annotation);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof AnnotatedBean)) {
        return false;
      }
      AnnotatedBean other = (AnnotatedBean) obj;
      return Objects.equals(name, other.name) && Objects.equals(annotation, other.annotation);
    }

    @Override
    public String toString() {
      return "AnnotatedBean [name=" + name + ", annotation=" + annotation + "]";
    }

  }


  /**
   * Finds all beans with annotation.
   *
   * @throws IllegalStateException if more than one bean is found
   */
  public static Function<ApplicationContext, Optional<AnnotatedBean>> getAnnotatedBean = applicationContext -> {
    final Set<Entry<String, Object>> beans = Optional.ofNullable(applicationContext.getBeansWithAnnotation(EnableProcessApplication.class))
      .map(Map::entrySet)
      .orElse(Collections.emptySet());

    return beans.stream().filter(ANNOTATED_WITH_ENABLEPROCESSAPPLICATION)
      .map(e -> AnnotatedBean.of(e.getKey(), e.getValue()))
      .reduce( (u,v) ->  {
        throw new IllegalStateException("requires exactly one bean to be annotated with @EnableProcessApplication, found: " + beans);
    });

  };


  public static Function<EnableProcessApplication, Optional<String>> getAnnotationValue = annotation ->
    Optional.of(annotation)
      .map(EnableProcessApplication::value)
      .filter(StringUtils::isNotBlank);

  public static Function<AnnotatedBean, String> getName = pair ->
    Optional.of(pair.getAnnotation()).flatMap(getAnnotationValue).orElse(pair.getName());


  public static Function<ApplicationContext, Optional<String>> getProcessApplicationName = applicationContext ->
    getAnnotatedBean.apply(applicationContext).map(getName);

  @Override
  public Optional<String> get() {
    return getProcessApplicationName.apply(applicationContext);
  }

  @Override
  public Optional<String> apply(Optional<String> springApplicationName) {
    Optional<String> processApplicationName = GetProcessApplicationNameFromAnnotation.getProcessApplicationName.apply(applicationContext);

    if (processApplicationName.isPresent()) {
      return processApplicationName;
    } else if (springApplicationName.isPresent()) {
      return springApplicationName;
    }
    return Optional.empty();
  }
}
