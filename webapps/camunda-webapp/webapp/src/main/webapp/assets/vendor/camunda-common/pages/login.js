ngDefine('camunda.common.pages.login', [ 'angular', 'require', 'module:camunda.common.services.authentication:camunda-common/services/Authentication' ], 
  function(module, angular, require) {

  var Controller = ['$scope', 'Authentication', 'Notifications', '$location',
           function ($scope, Authentication, Notifications, $location) {
    
    (function ($) {
      $.extend($.fn, {
        placeholder: function (options) {
          var defaults = {placeholderClass: 'placeholder'};

          options = $.extend(defaults, options);

          return this.each(function () {
            var input = $(this).addClass(options.placeholderClass);
            var form  = input.parents('form:first');
            var text  = input.val() || input.attr('placeholder');

            if (text) {
              input.val(text);

              input.focus(function () {
                clearInput();
              }).blur(function () {
                unclearInput();
              });

              form.submit(function() {
                if (input.hasClass(options.placeholderClass)) {
                  input.val('');
                }
              });

              input.blur();
            }

            function clearInput() {
              if (input.val() === text) {
                input.val('');
              }

              input.removeClass(options.placeholderClass);
            }

            function unclearInput() {
              if (input.val() === '') {
                input.addClass(options.placeholderClass).val(text);
              }
            }
          });
        }
      });
    })(jQuery);

   jQuery(document).ready(function ($) {
      $('[placeholder]:not([type="password"])').placeholder();
    });

    if (Authentication.username()) {
      $location.path("/");
    }

    $scope.login = function () {
      Authentication
        .login($scope.username, $scope.password)
        .then(function(success) {
          Notifications.clearAll();
          
          if (success) {
            $location.path("/");
          } else {
            Notifications.addError({ status: "Login Failed", message: "Username / password are incorrect" });
          }
        });
    }
  }];

  var RouteConfig = [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/login', {
      templateUrl: require.toUrl('./login.html'),
      controller: Controller
    });
  }];

  module
    .config(RouteConfig);

});