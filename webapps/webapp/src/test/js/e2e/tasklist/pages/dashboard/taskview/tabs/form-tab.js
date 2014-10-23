'use strict';

var Tab = require('./tab');

module.exports = Tab.extend({

  tabIndex: 0,

  completeButton: function () {
    return element(by.css('[ng-click="completeTask()"]'));
  }

});