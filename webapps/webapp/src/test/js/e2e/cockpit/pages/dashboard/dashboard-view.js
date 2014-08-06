'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/cockpit/default/#/dashboard',

  pluginList: function () {
    return element.all(by.repeater('dashboardProvider in dashboardProviders'));
  },

  pluginHeader: function(item) {
    return this.pluginList().get(item).element(by.css('.page-header')).getText();
  }

});
