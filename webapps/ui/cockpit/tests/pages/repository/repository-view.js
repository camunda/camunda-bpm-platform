'use strict';

var Page = require('../base');

module.exports = Page.extend({

  url: '/camunda/app/cockpit/default/#/repository',

  tabContent: function () {
    return element(by.css('.ctn-tabbed-content .tab-content'));
  }
});
