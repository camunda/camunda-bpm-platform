'use strict';

var Page = require('./dashboard-view');

module.exports = Page.extend({

  formElement: function() {
    return element(by.css('form[name="processStart"]'));
  },

  startButton: function() {
    return this.formElement().element(by.css('[ng-click="startProcess()"]'));
  },

  backButton: function() {
    return this.formElement().element(by.css('[ng-click="showList())"]'));
  },

  cancelButton: function() {
    return this.formElement().element(by.css('[ng-click="close()"]'));
  },

  searchProcessInput: function() {
    return this.formElement().element(by.model('searchProcess'));
  },

  foundProcesses: function() {
    return element.all(by.repeater('match in matches track by $index'));
  },

  selectProcessFromSearchResult: function(item) {
    this.foundProcesses().get(item).element(by.binding('match.label')).click();
  },

  processList: function() {
    return element.all(by.repeater('process in processes'));
  },

  selectProcess: function(item) {
   this.processList().get(item).element(by.binding('process.name')).click();
  }

});