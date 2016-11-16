'use strict';

var Table = require('./../../table');

module.exports = Table.extend({

  tabRepeater: 'resourceDetailTab in resourceDetailTabs',
  tabIndex: 0,
  tabLabel: 'Definitions',
  tableRepeater: 'definition in definitions',

  name: function(idx) {
    return this.tableItem(idx, '.name');
  },

  key: function(idx) {
    return this.tableItem(idx, '.key');
  },

  version: function(idx) {
    return this.tableItem(idx, '.version');
  },

  instanceCount: function(idx) {
    return this.tableItem(idx, '.instance-count');
  }

});
