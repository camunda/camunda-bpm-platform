'use strict';

var Base = require('./deployed-decisions-plugin');

module.exports = Base.extend({

  listObject: function() {
    return this.pluginObject().element(by.css('.decision-definitions-list'));
  },

  decisionsList: function() {
    return this.listObject().all(by.repeater('decision in decisions'));
  },

  selectDecision: function(idx) {
    return this.decisionsList().get(idx).element(by.binding('{{ decision.name || decision.key }}')).click();
  },

  selectDecisionByTenantId: function(tenantId) {
    var that = this;

    this.findElementIndexInRepeater('decision in decisions', by.css('.tenant-id'), tenantId).then(function(idx) {
      that.selectDecision(idx);
    });
  },

  decisionName: function(item) {
    return this.decisionsList().get(item).element(by.binding('{{ decision.name || decision.key }}')).getText();
  },

  tenantId: function(item) {
    return this.decisionsList().get(item).element(by.css('.tenant-id')).getText();
  }

});
