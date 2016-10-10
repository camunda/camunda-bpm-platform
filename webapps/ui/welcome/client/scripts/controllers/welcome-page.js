'use strict';

module.exports = [
  'configuration',
  function(configuration) {
    this.appVendor = configuration.getAppVendor();
    this.appName = configuration.getAppName();
  }];
