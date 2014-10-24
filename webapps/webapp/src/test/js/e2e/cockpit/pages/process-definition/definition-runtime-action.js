'use strict';

var ActionBar = require('./../action-bar');

module.exports = ActionBar.extend({

  barRepeater: 'actionProvider in processDefinitionActions',

  suspendButton: function() {
    return this.getActionButton(0);
  },

  clickSuspendButton: function() {
    this.clickActionButton(0);
  }

});