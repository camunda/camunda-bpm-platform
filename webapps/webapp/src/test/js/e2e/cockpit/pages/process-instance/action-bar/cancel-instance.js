'use strict';

var ActionBar = require('./../../action-bar');

module.exports = ActionBar.extend({

  barRepeater: 'tabProvider in processInstanceActions',

  cancelInstanceButton: function() {
    return this.getActionButton(0);
  }

});
