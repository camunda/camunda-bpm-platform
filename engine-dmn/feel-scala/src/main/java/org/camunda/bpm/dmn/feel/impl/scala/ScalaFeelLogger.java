package org.camunda.bpm.dmn.feel.impl.scala;

import org.camunda.bpm.dmn.feel.impl.FeelException;
import org.camunda.bpm.dmn.feel.impl.scala.spin.SpinValueMapperFactory;
import org.camunda.commons.logging.BaseLogger;

public class ScalaFeelLogger extends BaseLogger {

  public static final String PROJECT_CODE = "FEEL/SCALA";
  public static final String PROJECT_LOGGER = "org.camunda.bpm.feel.impl.scala";

  public static final ScalaFeelLogger LOGGER = createLogger(ScalaFeelLogger.class,
    PROJECT_CODE, PROJECT_LOGGER, "01");

  protected void logError(String id, String messageTemplate, Throwable t) {
    super.logError(id, messageTemplate, t);
  }

  protected void logInfo(String id, String messageTemplate, Throwable t) {
    super.logInfo(id, messageTemplate, t);
  }

  public void logSpinValueMapperDetected() {
    logInfo("001", "Spin value mapper detected");
  }

  public FeelException spinValueMapperInstantiationException(Throwable cause) {
    return new FeelException(exceptionMessage(
      "002", SpinValueMapperFactory.SPIN_VALUE_MAPPER_CLASS_NAME + " class found " +
        "on class path but cannot be instantiated."), cause);
  }

  public FeelException spinValueMapperAccessException(Throwable cause) {
    return new FeelException(exceptionMessage(
      "003", SpinValueMapperFactory.SPIN_VALUE_MAPPER_CLASS_NAME + " class found " +
        "on class path but cannot be accessed."), cause);
  }

  public FeelException spinValueMapperCastException(Throwable cause, String className) {
    return new FeelException(exceptionMessage(
      "004", SpinValueMapperFactory.SPIN_VALUE_MAPPER_CLASS_NAME + " class found " +
        "on class path but cannot be cast to " + className), cause);
  }

  public FeelException spinValueMapperException(Throwable cause) {
    return new FeelException(exceptionMessage(
      "005", "Error while looking up or registering Spin value mapper", cause));
  }

}
