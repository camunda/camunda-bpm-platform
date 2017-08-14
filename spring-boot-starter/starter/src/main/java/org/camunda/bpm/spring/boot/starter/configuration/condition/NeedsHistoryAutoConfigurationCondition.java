package org.camunda.bpm.spring.boot.starter.configuration.condition;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class NeedsHistoryAutoConfigurationCondition extends SpringBootCondition {

  protected static final String HISTORY_AUTO = "auto";

  protected String historyAutoFieldName = "HISTORY_AUTO";

  @Override
  public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
    return needsAdditionalConfiguration(context)
      ? ConditionOutcome.match("camunda version needs additional configuration for history level auto")
      : ConditionOutcome.noMatch("camunda version supports history level auto");
  }

  protected boolean needsAdditionalConfiguration(ConditionContext context) {
    String historyLevel = context.getEnvironment().getProperty("camunda.bpm.history-level");
    if (HISTORY_AUTO.equals(historyLevel)) {
      return !isHistoryAutoSupported();
    }
    return false;
  }

  protected boolean isHistoryAutoSupported() {
    // FIXME see
    // https://github.com/camunda/camunda-bpm-spring-boot-starter/issues/30
    return false;
    // return ReflectionUtils.findField(ProcessEngineConfiguration.class,
    // historyAutoFieldName) != null;
  }
}
