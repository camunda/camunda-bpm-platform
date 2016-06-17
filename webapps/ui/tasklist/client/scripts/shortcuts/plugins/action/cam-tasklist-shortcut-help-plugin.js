'use strict';
require('mousetrap');

var fs = require('fs');

var helpLinkTemplate = fs.readFileSync(__dirname + '/cam-tasklist-shortcut-help-plugin.html', 'utf8');
var showHelpTemplate = fs.readFileSync(__dirname + '/modals/cam-tasklist-shortcut-help.html', 'utf8');

var Controller = [
  '$scope',
  '$modal',
  function(
  $scope,
  $modal
) {

    var mousetrap = require('mousetrap');


    if (typeof window.camTasklistConf !== 'undefined' && window.camTasklistConf.shortcuts) {

      $scope.shortcuts = window.camTasklistConf.shortcuts;

      for(var key in window.camTasklistConf.shortcuts) {
        var shortcut = window.camTasklistConf.shortcuts[key];
        mousetrap.bind(shortcut.key, (function(key) {
          return function() {
            $scope.$root.$broadcast('shortcut:' + key);
          };
        })(key));
      }
    }

    $scope.showHelp = function() {
      var modalInstance = $modal.open({
      // creates a child scope of a provided scope
        scope: $scope,
        windowClass: 'shortcut-modal',
        size: 'lg',
        template: showHelpTemplate
      });

      modalInstance.result.then(function() {
        document.querySelector('a.showShortcutHelp').focus();
      }, function() {
        document.querySelector('a.showShortcutHelp').focus();
      });

    };

  }];

var Configuration = function PluginConfiguration(ViewsProvider) {

  ViewsProvider.registerDefaultView('tasklist.navbar.action', {
    id: 'shortcut-help',
    template: helpLinkTemplate,
    controller: Controller,
    priority: 300
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
