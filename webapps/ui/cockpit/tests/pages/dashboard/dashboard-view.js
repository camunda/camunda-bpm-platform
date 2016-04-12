'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/cockpit/default/',

  pluginList: function () {
    return element.all(by.css('.dashboard > .sections > section'));
  }
});
