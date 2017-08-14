package org.camunda.bpm.spring.boot.starter;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.spring.ProcessEngineFactoryBean;
import org.camunda.bpm.engine.spring.SpringProcessEngineServicesConfiguration;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.spring.boot.starter.event.ProcessApplicationEventPublisher;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.camunda.bpm.spring.boot.starter.property.ManagementProperties;
import org.camunda.bpm.spring.boot.starter.util.CamundaBpmVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@EnableConfigurationProperties({
    CamundaBpmProperties.class,
    ManagementProperties.class
})
@Import({
    CamundaBpmConfiguration.class,
    CamundaBpmActuatorConfiguration.class,
    CamundaBpmPluginConfiguration.class,
    SpringProcessEngineServicesConfiguration.class
})
@ConditionalOnProperty(prefix = CamundaBpmProperties.PREFIX, name = "enabled", matchIfMissing = true)
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
public class CamundaBpmAutoConfiguration {

  @Configuration
  class ProcessEngineConfigurationImplDependingConfiguration {

    @Autowired
    protected ProcessEngineConfigurationImpl processEngineConfigurationImpl;

    @Bean
    public ProcessEngineFactoryBean processEngineFactoryBean() {
      final ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
      factoryBean.setProcessEngineConfiguration(processEngineConfigurationImpl);

      return factoryBean;
    }

    @Bean
    @Primary
    public CommandExecutor commandExecutorTxRequired() {
      return processEngineConfigurationImpl.getCommandExecutorTxRequired();
    }

    @Bean
    public CommandExecutor commandExecutorTxRequiresNew() {
      return processEngineConfigurationImpl.getCommandExecutorTxRequiresNew();
    }

    @Bean
    public CommandExecutor commandExecutorSchemaOperations() {
      return processEngineConfigurationImpl.getCommandExecutorSchemaOperations();
    }
  }

  @Bean
  public CamundaBpmVersion camundaBpmVersion() {
    return new CamundaBpmVersion();
  }

  @Bean
  public ProcessApplicationEventPublisher processApplicationEventPublisher(ApplicationEventPublisher publisher) {
    return new ProcessApplicationEventPublisher(publisher);
  }

}
