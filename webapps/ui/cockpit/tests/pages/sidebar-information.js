'use strict';

var Base = require('./base');

module.exports = Base.extend({
  listElement: function () {
    return element(by.css('.ctn-sidebar dl'));
  },

  instanceId: function () {
    return this.listElement().element(by.css('.instance-id')).getText();
  },

  definitionId: function () {
    return this.listElement().element(by.css('.definition-id')).getText();
  },

  definitionName: function () {
    return this.listElement().element(by.css('.definition-name')).getText();
  },

  definitionKey: function () {
    return this.listElement().element(by.css('.definition-key')).getText();
  },

  definitionVersion: function () {
    return this.listElement().element(by.css('.definition-version')).getText();
  },

  definitionInstancesCurrent: function () {
    return this.listElement().element(by.css('.current-version')).getText();
  },

  definitionInstancesAll: function () {
    return this.listElement().element(by.css('.all-versions')).getText();
  },

  deploymentId: function () {
    return this.listElement().element(by.css('.deployment-id')).getText();
  }
});
