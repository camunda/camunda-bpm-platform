'use strict';

var ActionBar = require('./../action-bar');

module.exports = ActionBar.extend({

  barRepeater: 'tabProvider in processInstanceActions',

  cancelButton: function() {
    return this.getActionButton(0);
  },

  retryButton: function() {
    return this.getActionButton(1);
  }

});
