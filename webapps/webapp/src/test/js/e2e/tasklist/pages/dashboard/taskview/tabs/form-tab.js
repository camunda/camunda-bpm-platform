'use strict';

var Tab = require('./tab');

module.exports = Tab.extend({

  tabIndex: 0,

  formFormElement: function() {
    return element(by.css('.form-pane'));
  },

  genericAddVariableButton: function() {
    return this.formFormElement()
      .element(by.css('[ng-click="addVariable()"]'));
  },

  genericLoadVariablesButton: function() {
    return this.formFormElement()
      .element(by.css('[ng-click="loadVariables()"]'));
  },

  genericLoadVariables: function() {
    this.genericLoadVariablesButton().click();
  },

  completeButton: function() {
    return this.formFormElement()
      .element(by.css('[ng-click="complete()"]'));
  }

});
