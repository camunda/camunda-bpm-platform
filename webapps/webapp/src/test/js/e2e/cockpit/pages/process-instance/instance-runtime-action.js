'use strict';

var ActionBar = require('./../action-bar');

module.exports = ActionBar.extend({

  barRepeater: 'tabProvider in processInstanceActions',

  cancelButton: function() {
    return this.getActionButton(0);
  },

  clickCancelButton: function() {
    this.clickActionButton(0);
  },

  retryButton: function() {
    return this.getActionButton(1);
  },

  clickRetryButton: function() {
    this.clickActionButton(1);
  },

  addVariableButton: function() {
    return this.getActionButton(2);
  },

  clickAddVariableButton: function() {
    this.clickActionButton(2);
  },

  suspendButton: function() {
    return this.getActionButton(3);
  },

  clickSuspendButton: function() {
    this.clickActionButton(3);
  }

});