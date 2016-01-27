'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  formElement: function() {
    return element(by.css('.filters'));
  },

  instanceFilterInput: function(inputValue) {
    var inputField = this.formElement().element(by.model('name'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  instanceSelectionLabel: function() {
    return this.formElement().all(by.css('ng-pluralize')).get(0);
  },

  instanceSelectionRemoveIcon: function() {
    return this.formElement().element(by.css('[ng-click="clearSelection()"]'));
  },

  clearInstanceSelection: function() {
    this.instanceSelectionRemoveIcon().click();
  },

  selectInstance: function(activityName) {
    this.formElement().element(by.cssContainingText('.tree-node-label', activityName)).click();
  },

  deselectInstance: function(activityName) {
    this.formElement().element(by.cssContainingText('.tree-node-label', activityName))
      .element(by.css('[ng-click="deselect($event)"]'))
      .click();
  },

  isInstanceSelected: function(activityName) {
    return this.formElement().element(by.cssContainingText('.tree-node-label', activityName))
      .getAttribute('class')
      .then(function(classes) {
        return classes.indexOf('selected') !== -1;
      });
  }

});
