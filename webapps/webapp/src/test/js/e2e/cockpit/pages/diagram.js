'use strict';

var Base = require('./base');

module.exports = Base.extend({

  diagramActivitiy: function(activityName) {
    return element(by.css('.process-diagram *[data-activity-id=' + '"' + activityName + '"' + ']'));
  },

  selectActivitiy: function(activityName) {
    this.diagramActivitiy(activityName).click();
  },

  deselectActivity: function(activityName) {
    var selectedActivity = this.isActivitySelected(activityName);

    protractor.getInstance().actions()
        .keyDown(protractor.Key.CONTROL)
        .click(selectedActivity)
        .perform();
  },

  isActivitySelected: function(activityName) {
    var selectedElement = this.diagramActivitiy(activityName);

    expect(selectedElement.getAttribute('class')).toMatch('activity-highlight');
    return selectedElement;
  }

});