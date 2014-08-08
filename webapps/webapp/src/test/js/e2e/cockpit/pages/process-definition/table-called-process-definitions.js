'use strict';

var Table = require('./../table');


module.exports = Table.extend({

  repeater: 'tabProvider in processDefinitionTabs',
  tabIndex: 1,

  selectCalledProcessDefinitionsTab: function() {
    this.selectTab();
  },

  calledProcessDefinitionsTabName: function() {
    return this.tabName();
  },

  isCalledProcessDefinitionsTabSelected: function() {
    expect(this.isTabSelected()).toMatch('ng-scope active');
  },

  isCalledProcessDefinitionsTabNotSelected: function() {
    expect(this.isTabSelected()).not.toMatch('ng-scope active');
  },

  calledProcessDefinitionsTable: function() {
    this.selectCalledProcessDefinitionsTab();
    return element.all(by.repeater('calledProcessDefinition in calledProcessDefinitions'));
  },

  selectCalledProcessDefinitions: function(item) {
    this.calledProcessDefinitionsTable().get(item).element(by.binding('calledProcessDefinition.name')).click();
  }

});
