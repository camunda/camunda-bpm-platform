package org.camunda.bpm.client.spring.context;

import org.camunda.bpm.client.spring.BaseUrlSupplier;
import org.camunda.bpm.client.spring.DefaultBaseUrlSupplier;
import org.camunda.bpm.client.spring.EnableTaskSubscription;
import org.camunda.bpm.client.spring.ExternalTaskClientFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientRegistrar implements ImportBeanDefinitionRegistrar {

  public static final Class<? extends ExternalTaskClientFactory> DEFAULT_EXTERNAL_TASK_CLIENT_FACTORY_CLASS = ExternalTaskClientFactory.class;
  public static final Class<? extends BaseUrlSupplier> DEFAULT_ID_AWARE_BASE_URL_SUPPLIER_CLASS = DefaultBaseUrlSupplier.class;

  private final Class<? extends ExternalTaskClientFactory> externalTaskClientFactoryClass;
  private final Class<? extends BaseUrlSupplier> idAwareBaseUrlSupplierClass;

  public ClientRegistrar() {
    this(DEFAULT_EXTERNAL_TASK_CLIENT_FACTORY_CLASS, DEFAULT_ID_AWARE_BASE_URL_SUPPLIER_CLASS);
  }

  public ClientRegistrar(Class<? extends ExternalTaskClientFactory> externalTaskClientFactoryClass,
      Class<? extends BaseUrlSupplier> idAwareBaseUrlSupplierClass) {
    Assert.notNull(externalTaskClientFactoryClass, "externalTaskClientFactoryClass must not be 'null'");
    Assert.notNull(idAwareBaseUrlSupplierClass, "idAwareBaseUrlSupplierClass must not be 'null'");
    this.externalTaskClientFactoryClass = externalTaskClientFactoryClass;
    this.idAwareBaseUrlSupplierClass = idAwareBaseUrlSupplierClass;
  }

  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    AnnotationAttributes enableTaskSubscription = getEnableTaskSubscription(importingClassMetadata);
    String baseUrlSupplier = registerBaseUrlSupplier(enableTaskSubscription, importingClassMetadata, registry);
    registerExternalTaskClient(enableTaskSubscription, baseUrlSupplier, importingClassMetadata, registry);
  }

  protected void registerExternalTaskClient(AnnotationAttributes enableTaskSubscription, String baseUrlSupplier,
      AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    String id = getId(enableTaskSubscription);
    String beanName = "externalTaskClient" + id;
    if (!isUniqueBean(beanName, importingClassMetadata, registry)) {
      return;
    }
    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(externalTaskClientFactoryClass)
        .addPropertyValue("id", id).addPropertyReference("baseUrlSupplier", baseUrlSupplier);
    if (!StringUtils.isEmpty(id)) {
      AutowireCandidateQualifier qualifierMetadata = new AutowireCandidateQualifier(Qualifier.class);
      qualifierMetadata.setAttribute(AutowireCandidateQualifier.VALUE_KEY, id);
      builder.getRawBeanDefinition().addQualifier(qualifierMetadata);
    }
    registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    log.debug("registered external task client with beanName '{}'", beanName);
  }

  protected String registerBaseUrlSupplier(AnnotationAttributes enableTaskSubscription,
      AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    String id = getId(enableTaskSubscription);
    String beanName = "baseUrlSupplier" + id;
    if (!isUniqueBean(beanName, importingClassMetadata, registry)) {
      return beanName;
    }
    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(idAwareBaseUrlSupplierClass)
        .addPropertyValue("baseUrl", getBaseUrl(enableTaskSubscription)).addPropertyValue("id", id);
    AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
    registry.registerBeanDefinition(beanName, beanDefinition);
    log.debug("registered baseUrlSupplier with beanName '{}'", beanName);
    return beanName;
  }

  protected boolean isUniqueBean(String beanName, AnnotationMetadata importingClassMetadata,
      BeanDefinitionRegistry registry) {
    if (registry.containsBeanDefinition(beanName)) {
      BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
      log.debug("skipping creation of bean for factory '{}'. A bean with name '{}' created by '{}' already exists",
          importingClassMetadata.getClassName(), beanName, beanDefinition.getFactoryBeanName());
      return false;
    }
    return true;
  }

  public static String getId(AnnotationAttributes annotationAttributes) {
    return annotationAttributes.getString("id");
  }

  public static String getBaseUrl(AnnotationAttributes annotationAttributes) {
    return annotationAttributes.getString("baseUrl");
  }

  public static AnnotationAttributes getEnableTaskSubscription(AnnotationMetadata annotationMetadata) {
    return AnnotationAttributes
        .fromMap(annotationMetadata.getAnnotationAttributes(EnableTaskSubscription.class.getName(), false));
  }
}