/**
 * A notifications module to be used in client side applications.
 *
 */
ngDefine('camunda.common.services.notifications', [ 'angular' ], function(module, angular) {

  var ServiceProducer = function ServiceProducer($filter, $timeout) {
    return {
      notifications : [],
      consumers : [],

      addError: function(error) {
        if (!error.type) {
          error.type = 'error';
        }

        this.add(error);
      },

      addMessage: function(message) {
        if (!message.type) {
          message.type = 'information';
        }

        this.add(message);
      },

      /**
       *
       * Notification object may specify the following fields:
       *   type: type of the notification (information, warning, error, success)
       *   status: main status line
       *   message: detail message
       *   duration: time duration in ms the notification should be shown to the user
       *   exclusive: boolean || array of attribute names that notification should be exclusive with
       *
       * @param notification {notification}
       * @returns {undefined}
       */
      add: function(notification) {

        var self = this,
            notifications = this.notifications,
            consumers = this.consumers,
            exclusive = notification.exclusive;

        if (exclusive) {
          if (typeof exclusive == 'boolean') {
            this.clearAll();
          } else {
            var filter = {};
            angular.forEach(exclusive, function(key) {
              filter[key] = notification[key];
            });

            self.clear(filter);
          }
        }

        notifications.push(notification);

        for (var i = consumers.length - 1, c; !!(c = consumers[i]); i--) {

          // add to first interested consumer only
          if (c.add(notification)) {
            break;
          }
        }

        if (notification.duration) {
          $timeout(function() {
            self.clear(notification);
          }, notification.duration);
        }
      },

      clear: function(notification) {
        var notifications = this.notifications,
            consumers = this.consumers,
            removeCandidates = [];

        if (typeof notification == 'string') {
          notification = { status: notification };
        }

        var removeCandidates = $filter('filter')(notifications, notification);
        removeCandidates.push(notification);

        angular.forEach(removeCandidates, function(e) {
          var idx = notifications.indexOf(e);
          if (idx != -1) {
            notifications.splice(idx, 1);
          }

          angular.forEach(consumers, function(consumer) {
            consumer.remove(e);
          });
        });
      },
      clearAll: function() {
        var notifications = this.notifications;

        while (notifications.length) {
          var notification = notifications.pop();
          this.clear(notification);
        }
      },

      registerConsumer: function(consumer) {
        this.consumers.push(consumer);
      },

      unregisterConsumer: function(consumer) {
        var consumers = this.consumers,
            idx = consumers.indexOf(consumer);

        if (idx != -1) {
          consumers.splice(idx, 1);
        }
      }
    };
  };

  ServiceProducer.$inject = [ '$filter', '$timeout' ];

  module.service('Notifications', ServiceProducer);
});
