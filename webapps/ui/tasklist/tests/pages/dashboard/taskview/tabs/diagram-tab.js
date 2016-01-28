'use strict';

var Tab = require('./tab');

module.exports = Tab.extend({

  tabIndex: 2,

  diagramFormElement: function() {
    return element(by.css('[cam-widget-bpmn-viewer]'));
  },

  diagramActivity: function(activityName) {
    return element(by.css('*[data-element-id=' + '"' + activityName + '"' + ']'));
  },

  isActivitySelected: function(activityName) {
    var diagramElement = element(by.css('.djs-container'));
    this.waitForElementToBeVisible(diagramElement, 5000);

    return this.diagramActivity(activityName)
            .getAttribute('class')
            .then(function(classes) {
              return classes.indexOf('highlight') !== -1;
            });
  }

});
