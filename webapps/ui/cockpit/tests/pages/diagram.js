'use strict';

var Base = require('./base');

module.exports = Base.extend({

  diagramElement: function() {
    return element(by.css('[cam-widget-bpmn-viewer]'));
  },

  instancesBadgeFor: function(activityName) {
    return element(by.css('[data-container-id="'+activityName+'"] .badge[tooltip="Running Activity Instances"]'));
  },

  incidentsBadgeFor: function(activityName) {
    return element(by.css('[data-container-id="'+activityName+'"] .badge[tooltip="Open Incidents"]'));
  },

  diagramActivity: function(activityName) {
    return element(by.css('*[data-element-id=' + '"' + activityName + '"' + ']'));
  },

  selectActivity: function(activityName) {
    this.diagramActivity(activityName).click();
  },

  deselectAll: function() {
    this.diagramElement().click();
  },

  isActivitySelected: function(activityName) {
    return this.diagramActivity(activityName)
            .getAttribute('class')
            .then(function(classes) {
              return classes.indexOf('highlight') !== -1;
            });
  },

  isActivitySuspended: function(activityName) {
    return element(by.css('[data-container-id="'+activityName+'"] .badge[tooltip="Suspended Job Definition"]'))
            .getAttribute('class')
            .then(function(classes) {
              return classes.indexOf('ng-hide') === -1;
            });
  }

});
