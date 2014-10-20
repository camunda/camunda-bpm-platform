'use strict';

var Page = require('./../start-process');

module.exports = Page.extend({

  processName: function() {
    return this.formElement().element(by.binding('startingProcess.name')).getText();
  },

  helpText: function() {
    return this.formElement().element(by.css('.text-help')).getText();
  },

  variableListElement: function() {
    return element(by.css('.process-form-fields')).element(by.css('[ng-repeat="(delta, variable) in variables"]'));
  },

  addVariableButton: function() {
    return this.formElement().element(by.css('[ng-click = "addVariable()"]'));
  },

  removeVariableButton: function() {
    return this.formElement().element(by.css('[ng-click="removeVariable(delta)"]'));
  }

});