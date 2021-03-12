package org.camunda.bpm.client.spring;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DisableDefaultExternalTaskRegistration {

}
