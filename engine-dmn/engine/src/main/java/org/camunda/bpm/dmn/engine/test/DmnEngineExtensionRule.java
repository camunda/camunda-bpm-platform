package org.camunda.bpm.dmn.engine.test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specialisation of Camunda's {@link DmnEngineRule} allowing to deploy DMN definitions in a declarative way rather than programmatic way.
 *
 * @see {@link DmnEngineRule}
 * @see {@link DmnResource}
 */
public class DmnEngineExtensionRule extends DmnEngineRule {

  private static final Logger LOG = LoggerFactory.getLogger(DmnEngineRule.class);
  /* available decisions */
  private List<DmnDecision> decisions = new ArrayList<>();
  /* the current decision */
  private DmnDecision decision;

  public DmnEngineExtensionRule() {
    this(null);
  }

  public DmnEngineExtensionRule(DmnEngineConfiguration dmnEngineConfiguration) {
    super(dmnEngineConfiguration);
  }

  public DmnDecisionResult evaluateDecision(Map<String, Object> variables) {
    return getDmnEngine().evaluateDecision(decision, variables);
  }

  public DmnDecisionResult evaluateDecision(String key, Map<String, Object> variables) {
    Optional<DmnDecision> decisionOpt = decisions.stream().filter(p -> p.getKey().equalsIgnoreCase(key.trim())).findFirst();
    if (!decisionOpt.isPresent()) {
      throw new IllegalArgumentException("decision id '" + key + "' does not exist");
    }
    decision = decisionOpt.get();
    return evaluateDecision(variables);
  }

  @Override
  public void starting(Description description) {
    super.starting(description);
    annotationDeploymentSetUp(description.getTestClass(), description.getMethodName(), description.getAnnotation(DmnResource.class), description.getAnnotation(
      DmnDecisionKey.class)
    );
  }

  @Override
  protected void finished(Description description) {
    annotationDeploymentTearDown(description.getTestClass(), description.getMethodName(), description.getAnnotation(DmnResource.class), description.getAnnotation(
      DmnDecisionKey.class)
    );
    super.finished(description);
  }

  private void annotationDeploymentTearDown(Class<?> testClass, String methodName, DmnResource deploymentAnnotation, DmnDecisionKey decisionAnnotation) {
    decisions.clear();
    decision = null;
  }

  private void annotationDeploymentSetUp(Class<?> testClass, String methodName, DmnResource deploymentAnnotation, DmnDecisionKey decisionAnnotation) {
    dmnResources(testClass, methodName, deploymentAnnotation);
    dmnDecision(testClass, methodName, decisionAnnotation);
  }

  private void dmnDecision(Class<?> testClass, String methodName, DmnDecisionKey decisionAnnotation) {
    Method method = null;
    boolean onMethod = true;

    try {
      method = getMethod(testClass, methodName);
    } catch (Exception e) {
      if (decisionAnnotation == null) {
        // we have neither the annotation, nor can look it up from the method
        return;
      }
    }

    if (decisionAnnotation == null) {
      decisionAnnotation = method.getAnnotation(DmnDecisionKey.class);
    }
    // if not found on method, try on class level
    if (decisionAnnotation == null) {
      onMethod = false;
      Class<?> lookForAnnotationClass = testClass;
      while (lookForAnnotationClass != Object.class) {
        decisionAnnotation = lookForAnnotationClass.getAnnotation(DmnDecisionKey.class);
        if (decisionAnnotation != null) {
          testClass = lookForAnnotationClass;
          break;
        }
        lookForAnnotationClass = lookForAnnotationClass.getSuperclass();
      }
    }

    if (decisionAnnotation == null) {
      LOG.debug("No annotation @Decision found");
      return;
    }

    LOG.debug("annotation @Decision for {}.{}", ClassNameUtil.getClassNameWithoutPackage(testClass), methodName);
    String key = decisionAnnotation.name();
    if (key.trim().isEmpty()) {
      throw new IllegalArgumentException("@Decision for key '" + key + "' invalid");
    }
    decision = decisions.stream().filter(p -> p.getKey().equalsIgnoreCase(key.trim())).findFirst().orElse(null);
  }

  private void dmnResources(Class<?> testClass, String methodName, DmnResource deploymentAnnotation) {
    Method method = null;
    boolean onMethod = true;

    try {
      method = getMethod(testClass, methodName);
    } catch (Exception e) {
      if (deploymentAnnotation == null) {
        // we have neither the annotation, nor can look it up from the method
        return;
      }
    }

    if (deploymentAnnotation == null) {
      deploymentAnnotation = method.getAnnotation(DmnResource.class);
    }
    // if not found on method, try on class level
    if (deploymentAnnotation == null) {
      onMethod = false;
      Class<?> lookForAnnotationClass = testClass;
      while (lookForAnnotationClass != Object.class) {
        deploymentAnnotation = lookForAnnotationClass.getAnnotation(DmnResource.class);
        if (deploymentAnnotation != null) {
          testClass = lookForAnnotationClass;
          break;
        }
        lookForAnnotationClass = lookForAnnotationClass.getSuperclass();
      }
    }

    if (deploymentAnnotation == null) {
      LOG.debug("No annotation @Deployment found");
      return;
    }

    LOG.debug("annotation @Deployment creates deployment for {}.{}", ClassNameUtil.getClassNameWithoutPackage(testClass), methodName);
    String[] resources = deploymentAnnotation.resources();
    for (String resource : resources) {
      try (InputStream is = getClass().getResourceAsStream(resource)) {
        decisions.addAll(getDmnEngine().parseDecisions(is));
      } catch (IOException e) {
        throw new IllegalArgumentException("Loading DMN resource '" + resource + "' failed", e);
      }
    }
  }

  protected static Method getMethod(Class<?> clazz, String methodName) throws SecurityException, NoSuchMethodException {
    return clazz.getMethod(methodName, (Class<?>[]) null);
  }
}
