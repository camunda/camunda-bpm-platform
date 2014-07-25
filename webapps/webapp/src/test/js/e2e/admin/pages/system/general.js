'use strict';

var Page = require('./system-base');

var groupsSection = element(by.id('groups'));

module.exports = Page.extend({

  url: '/camunda/app/admin/default/#/system?section=system-settings-general'

});
