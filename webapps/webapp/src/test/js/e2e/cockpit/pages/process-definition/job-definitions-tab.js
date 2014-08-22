'use strict';

var Table = require('./../table');

module.exports = Table.extend({

  repeater: 'tabProvider in processDefinitionTabs',
  tabIndex: 2,

  selectJobDefinitionsTab: function() {
    this.selectTab();
  },

  jobDefinitionsTabName: function() {
    return this.tabName();
  },

  isJobDefinitionsTabSelected: function() {
    expect(this.isTabSelected()).toMatch('ng-scope active');
  },

  isJobDefinitionsTabNotSelected: function() {
    expect(this.isTabSelected()).not.toMatch('ng-scope active');
  },

  jobDefinitionsTable: function() {
    this.selectJobDefinitionsTab();
    return element.all(by.repeater('jobDefinition in jobDefinitions'));
  },

  selectJobDefinition: function(item) {
    this.jobDefinitionsTable().get(item).element(by.binding('jobDefinition.id')).click();
  },

  jobDefinitionName: function(item) {
    return this.jobDefinitionsTable().get(item).element(by.css('[title]')).getAttribute('title');
  }

});
