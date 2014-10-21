'use strict';

var Page = require('./../current-task');

module.exports = Page.extend({

  completeButton: function () {
    return element(by.css('[ng-click="completeTask()"]'));
  }

});