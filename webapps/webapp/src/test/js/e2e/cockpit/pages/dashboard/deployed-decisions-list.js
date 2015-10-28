'use strict';

var Base = require('./deployed-decisions-plugin');

module.exports = Base.extend({

  listObject: function() {
    return this.pluginObject().element(by.css('.decision-definitions-list'));
  },

  decisionsList: function() {
    return this.listObject().all(by.repeater('decision in decisions'));
  },

  selectDecision: function(item) {
    return this.decisionsList().get(item).element(by.binding('{{ decision.name || decision.key }}')).click();
  },

  decisionName: function(item) {
    return this.decisionsList().get(item).element(by.binding('{{ decision.name || decision.key }}')).getText();
  }

});
