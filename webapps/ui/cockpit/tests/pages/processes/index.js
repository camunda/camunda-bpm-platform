'use strict';

var Base = require('./../base');
var DeployedProcessesListPage = require('./deployed-processes-list');
var DeployedProcessesPreviewsPage = require('./deployed-processes-previews');
var AuthenticationPage = require('../../../../common/tests/pages/authentication');

var Page = Base.extend({

  url: '/camunda/app/cockpit/default/#/processes',

  pluginList: function () {
    return element.all(by.css('.dashboard'));
  }
});

module.exports = new Page();

module.exports.deployedProcessesList = new DeployedProcessesListPage();
module.exports.deployedProcessesPreviews = new DeployedProcessesPreviewsPage();
module.exports.authentication = new AuthenticationPage();
