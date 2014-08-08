'use strict';

var Base = require('./../table');
var repeater = 'tabProvider in processDefinitionTabs';
var tabIndex = 1;

module.exports = Base.extend({

  selectCalledProcessDefinitionsTab: function() {
    this.selectTab(repeater, tabIndex);
  },

  calledProcessDefinitionsTabName: function() {
    return this.tabName(repeater, tabIndex);
  },

  isCalledProcessDefinitionsTabSelected: function() {
    expect(this.isTabSelected(repeater, tabIndex)).toMatch('ng-scope active');
  },

  isCalledProcessDefinitionsTabNotSelected: function() {
    expect(this.isTabSelected(repeater, tabIndex)).not.toMatch('ng-scope active');
  },

  calledProcessDefinitionsTable: function() {
    this.selectCalledProcessDefinitionsTab();
    return element.all(by.repeater('calledProcessDefinition in calledProcessDefinitions'));
  },

  selectCalledProcessDefinitions: function(item) {
    this.calledProcessDefinitionsTable().get(item).element(by.binding('calledProcessDefinition.name')).click();
  }

});
