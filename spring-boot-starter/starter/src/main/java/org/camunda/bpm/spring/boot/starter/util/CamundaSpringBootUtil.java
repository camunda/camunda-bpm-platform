package org.camunda.bpm.spring.boot.starter.util;


import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public final class CamundaSpringBootUtil {

  /**
   * @param obj  that should be casted
   * @param type to cast
   * @return optional casted object
   */
  @SuppressWarnings("unchecked")
  public static <T> Optional<T> cast(final Object obj, Class<T> type) {
    return Optional.ofNullable(obj)
      .filter(type::isInstance)
      .map(type::cast);
  }

  public static SpringProcessEngineConfiguration springProcessEngineConfiguration() {
    return initCustomFields(new SpringProcessEngineConfiguration());
  }

  public static Optional<ProcessEngineImpl> processEngineImpl(ProcessEngine processEngine) {
    return cast(processEngine, ProcessEngineImpl.class);
  }

  public static Optional<SpringProcessEngineConfiguration> springProcessEngineConfiguration(ProcessEngineConfiguration configuration) {
    return cast(configuration, SpringProcessEngineConfiguration.class);
  }

  public static SpringProcessEngineConfiguration get(ProcessEngine processEngine) {
    return (SpringProcessEngineConfiguration) processEngine.getProcessEngineConfiguration();
  }

  /**
   * @param existing the current values (may be null or empty)
   * @param add      the additional values (may be null or empty)
   * @param <T>      type of elements
   * @return new non-null list containing all elements of existing and add.
   */
  public static <T> List<T> join(final List<? extends T> existing, final List<? extends T> add) {
    final List<T> target = new ArrayList<T>();
    if (!CollectionUtils.isEmpty(existing)) {
      target.addAll(existing);
    }
    if (!CollectionUtils.isEmpty(add)) {
      target.addAll(add);
    }
    return target;
  }

  public static SpringProcessEngineConfiguration initCustomFields(SpringProcessEngineConfiguration configuration) {
    // CommandInterceptorsTxRequired
    if (configuration.getCustomPostCommandInterceptorsTxRequired() == null) {
      configuration.setCustomPostCommandInterceptorsTxRequired(new ArrayList<>());
    }
    if (configuration.getCustomPreCommandInterceptorsTxRequired() == null) {
      configuration.setCustomPreCommandInterceptorsTxRequired(new ArrayList<>());
    }

    // CommandInterceptorsTxRequiresNew
    if (configuration.getCustomPreCommandInterceptorsTxRequiresNew() == null) {
      configuration.setCustomPreCommandInterceptorsTxRequiresNew(new ArrayList<>());
    }
    if (configuration.getCustomPostCommandInterceptorsTxRequiresNew() == null) {
      configuration.setCustomPostCommandInterceptorsTxRequiresNew(new ArrayList<>());
    }

    // SessionFactories
    if (configuration.getCustomSessionFactories() == null) {
      configuration.setCustomSessionFactories(new ArrayList<>());
    }

    // Deployers
    if (configuration.getCustomPreDeployers() == null) {
      configuration.setCustomPreDeployers(new ArrayList<>());
    }
    if (configuration.getCustomPostDeployers() == null) {
      configuration.setCustomPostDeployers(new ArrayList<>());
    }

    // JobHandlers
    if (configuration.getCustomJobHandlers() == null) {
      configuration.setCustomJobHandlers(new ArrayList<>());
    }

    // IncidentHandlers
    if (configuration.getCustomIncidentHandlers() == null) {
      configuration.setCustomIncidentHandlers(new ArrayList<>());
    }

    // BatchJobHandlers
    if (configuration.getCustomBatchJobHandlers() == null) {
      configuration.setCustomBatchJobHandlers(new ArrayList<>());
    }

    // Forms
    if (configuration.getCustomFormEngines() == null) {
      configuration.setCustomFormEngines(new ArrayList<>());
    }
    if (configuration.getCustomFormFieldValidators() == null) {
      configuration.setCustomFormFieldValidators(new HashMap<>());
    }
    if (configuration.getCustomFormTypes() == null) {
      configuration.setCustomFormTypes(new ArrayList<>());
    }

    // VariableSerializers
    if (configuration.getCustomPreVariableSerializers() == null) {
      configuration.setCustomPreVariableSerializers(new ArrayList<>());
    }
    if (configuration.getCustomPostVariableSerializers() == null) {
      configuration.setCustomPostVariableSerializers(new ArrayList<>());
    }

    // HistoryLevels
    if (configuration.getCustomHistoryLevels() == null) {
      configuration.setCustomHistoryLevels(new ArrayList<>());
    }

    // Cmmn Transform Listeners
    if (configuration.getCustomPreCmmnTransformListeners() == null) {
      configuration.setCustomPreCmmnTransformListeners(new ArrayList<>());
    }
    if (configuration.getCustomPostCmmnTransformListeners() == null) {
      configuration.setCustomPostCmmnTransformListeners(new ArrayList<>());
    }

    // BPMNParseListeners
    if (configuration.getCustomPreBPMNParseListeners() == null) {
      configuration.setCustomPreBPMNParseListeners(new ArrayList<>());
    }
    if (configuration.getCustomPostBPMNParseListeners() == null) {
      configuration.setCustomPostBPMNParseListeners(new ArrayList<>());
    }

    // Event Handlers
    if (configuration.getCustomEventHandlers() == null) {
      configuration.setCustomEventHandlers(new ArrayList<>());
    }

    // MigrationActivityValidator
    if (configuration.getCustomPreMigrationActivityValidators() == null) {
      configuration.setCustomPreMigrationActivityValidators(new ArrayList<>());
    }
    if (configuration.getCustomPostMigrationActivityValidators() == null) {
      configuration.setCustomPostMigrationActivityValidators(new ArrayList<>());
    }

    // MigrationInstructionValidator
    if (configuration.getCustomPreMigrationInstructionValidators() == null) {
      configuration.setCustomPreMigrationInstructionValidators(new ArrayList<>());
    }
    if (configuration.getCustomPostMigrationInstructionValidators() == null) {
      configuration.setCustomPostMigrationInstructionValidators(new ArrayList<>());
    }

    // MigratingActivityInstanceValidator
    if (configuration.getCustomPreMigratingActivityInstanceValidators() == null) {
      configuration.setCustomPreMigratingActivityInstanceValidators(new ArrayList<>());
    }
    if (configuration.getCustomPostMigratingActivityInstanceValidators() == null) {
      configuration.setCustomPostMigratingActivityInstanceValidators(new ArrayList<>());
    }

    return configuration;
  }

  private CamundaSpringBootUtil() {
  }
}
