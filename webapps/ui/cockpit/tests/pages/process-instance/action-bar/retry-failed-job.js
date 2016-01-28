'use strict';

var ActionBar = require('./../../action-bar');

module.exports = ActionBar.extend({

  barRepeater: 'tabProvider in processInstanceActions',

  retryFailedJobButton: function() {
    return this.getActionButton(1);
  }

});
