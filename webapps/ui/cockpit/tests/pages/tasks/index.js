'use strict';

var Base = require('./../base');
var OpenTasksStatisticsPage = require('./open-tasks-statistics');
var AuthenticationPage = require('../../../../common/tests/pages/authentication');

var Page = Base.extend({

  url: '/camunda/app/cockpit/default/#/tasks',

  pluginList: function () {
    return element.all(by.css('.dashboard'));
  }
});

module.exports = new Page();

module.exports.openTasksStatistics = new OpenTasksStatisticsPage();
module.exports.authentication = new AuthenticationPage();
