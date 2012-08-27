/**
 * Classes meant for JSON serialization and query data aggregation. 
 * 
 * This separation from the domain classes was introduced because we don't want Jackson to 
 * interfer with hibernate lazy loading. 
 */
package com.camunda.fox.cycle.web.dto;