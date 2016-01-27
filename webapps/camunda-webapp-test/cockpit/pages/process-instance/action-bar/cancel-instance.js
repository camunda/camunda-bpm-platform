'use strict';

var ActionBar = require('./../../action-bar');

module.exports = ActionBar.extend({

  barRepeater: 'tabProvider in processInstanceActions',

  cancelInstanceButton: function() {
    return this.getActionButton(0);
  },

  cancelInstance: function() {
    var modal = this.modal;
    this.cancelInstanceButton().click().then(function() {
      modal.cancelButton().click().then(function() {
        modal.okButton().click();
      });
    });
  }

});
