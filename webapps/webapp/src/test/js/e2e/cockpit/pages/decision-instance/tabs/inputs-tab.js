'use strict';

var Table = require('./../../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in decisionInstanceTabs',
  tabIndex: 0,
  tabLabel: 'Inputs',

  variableName: function(idx) {
    return element.all(by.repeater('variable in variables')).get(idx).element(by.css('.name')).getText();
  },

  variableValue: function(idx) {
    return element.all(by.repeater('variable in variables')).get(idx).element(by.css('.value')).getText();
  }


});
