'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/cockpit/default/#/dashboard',

  pluginList: function () {
    return element.all(by.css('.dashboard > .sections > section'));
  },

  plugin: function(idx) {
    return this.pluginList().get(idx);
  }

});
