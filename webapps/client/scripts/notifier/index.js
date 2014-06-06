'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define(['angular', 'angular-animate', 'text!camunda-tasklist-ui/notifier/notifier.html'],
function(angular) {
  var notifierModule = angular.module('cam.tasklist.notifier', [
    'ngAnimate'
  ]);

  var each = angular.forEach;

  var _id = 0;

  function Notifier(scope) {
    this.scope = scope;
    this._messages = [];
  }

  Notifier.prototype.scope = null;

  Notifier.prototype._messages = [];

  Notifier.prototype.get = function(type) {
    if (!type) {
      return this._messages;
    }

    var msgs = [];

    each(this._messages, function(message) {
      if (message.type === 'type') {
        msgs.push(message);
      }
    });

    return msgs;
  };

  Notifier.prototype.add = function(msgs) {
    msgs = angular.isArray(msgs) ? msgs : [msgs];
    var self = this;

    each(msgs, function(msg) {
      _id++;
      var text = '';
      var type = 'info';


      if (angular.isString(msg)) {
        text = msg;
      }
      else if (msg instanceof Error) {
        text = msg.message;
        type = 'error';
      }
      else if (msg.text) {
        type = msg.type || type;
        text = msg.text || text;
      }

      self._messages.push({
        id: _id,
        text: text,
        type: type
      });
    });

    self.scope.$broadcast('notifier:message', self._messages);

    return self;
  };

  Notifier.prototype.dismiss = function(id) {
    var self = this;

    if (!id) {
      self._messages = [];
    }
    else {
      var msgs = [];

      each(self._messages, function(message, i) {
        if (message.id !== id) {
          msgs.push(message);
        }
      });

      self._messages = msgs;
    }

    self.scope.$broadcast('notifier:message', self._messages);

    return self;
  };

  notifierModule.factory('camTasklistNotifier', [
          '$rootScope',
  function($rootScope) {
    return new Notifier($rootScope);
  }]);



  notifierModule.directive('camTasklistNotifier', function() {
    return {
      scope: {},
      controller: [
              '$rootScope', '$scope', '$timeout', 'camTasklistNotifier',
      function($rootScope,   $scope,   $timeout,   camTasklistNotifier) {
        $scope.remove = function(id) {
          camTasklistNotifier.dismiss(id);
        };

        $rootScope.$on('notifier:message', function(ev, msgs) {
          each(msgs, function(msg) {
            if (msg.duration !== false) {
              $timeout(function() {
                $scope.remove(msg.id);
              }, msg.duration || 4000);
            }
          });

          $scope.messages = msgs;
        });

        $scope.messages = camTasklistNotifier.get();
      }],
      template: require('text!camunda-tasklist-ui/notifier/notifier.html')
    };
  });

  return notifierModule;
});
