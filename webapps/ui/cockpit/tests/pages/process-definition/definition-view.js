'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/cockpit/default/#/process-definition/:process/runtime',

  pageHeader: function() {
    return element(by.binding('processDefinition.key'));
  },

  fullPageHeaderProcessDefinitionName: function() {
    return this.pageHeader().getText();
  },

  pageHeaderProcessDefinitionName: function() {
    return this.breadcrumb.activeCrumb().getText();
  },

  isDefinitionSuspended: function() {
    return element(by.css('.cam-breadcrumb .active .badge-suspended')).isPresent();
  },

  getReportLink: function() {
    return element(by.css('a.report-link'));
  }
});
