'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/cockpit/default/#/process-instance/:instance/runtime',

  pageHeader: function() {
    return element(by.binding('{{ processInstance.id }}'));
  },

  fullPageHeaderProcessInstanceName: function() {
    return this.pageHeader().getText();
  },

  pageHeaderProcessInstanceName: function() {
    return element(by.binding('{{ processInstance.id }}')).getText().then(function(fullString) {
      return fullString.replace('<', '').replace('>', '');
    });
  },

  processName: function() {
    return element(by.binding('{{ processDefinition.name || processDefinition.key }}')).getText()
  },

  instanceId: function() {
    return element(by.binding('{{ processInstance.id }}')).getText().then(function(fullString) {
      return fullString.replace('<', '').replace('>', '');
    });
  },

  businessKey: function() {
    return element(by.binding('{{ processInstance.businessKey}}')).getText().then(function(fullString) {
      return fullString.replace('<', '').replace('>', '');
    });
  },

  isInstanceSuspended: function() {
    return element(by.css('.cam-breadcrumb .active .badge-suspended')).isPresent();
  },

  sidebarTabClick: function (name) {
    return element(by.cssContainingText('.ctn-sidebar .nav-tabs a', name)).click();
  }
});
