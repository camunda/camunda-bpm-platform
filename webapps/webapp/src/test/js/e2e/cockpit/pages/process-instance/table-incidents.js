'use strict';

var Base = require('./../table');
var repeater = 'tabProvider in processInstanceTabs';
var tabIndex = 1;

module.exports = Base.extend({

  selectIncidentsTab: function() {
    this.selectTab(repeater, tabIndex);
  },

  incidentsTabName: function() {
    return this.tabName(repeater, tabIndex);
  },

  isIncidentsTabSelected: function() {
    expect(this.isTabSelected(repeater, tabIndex)).toMatch('ng-scope active');
  },

  isIncidentsTabNotSelected: function() {
    expect(this.isTabSelected(repeater, tabIndex)).not.toMatch('ng-scope active');
  },

  incidentsTable: function() {
    this.selectIncidentsTab();
    return element.all(by.repeater('incident in incidents'));
  },

  selectIncident: function(item) {
    this.incidentsTable().get(item).element(by.binding('incident.name')).click();
  }

});