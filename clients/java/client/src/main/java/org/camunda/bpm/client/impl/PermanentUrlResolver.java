package org.camunda.bpm.client.impl;

import org.camunda.bpm.client.UrlResolver;

/**
 * urlResolver with permanent address
 */
public class PermanentUrlResolver implements UrlResolver {


    protected String baseUrl;

    public PermanentUrlResolver(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }
}
