ngDefine('camunda.common.directives.modal', [ 'module:ui.bootstrap:angular-ui' ], function(module) {

  /**
   * Configure dialog with our defaults
   */
  module.config([ '$dialogProvider', function($dialogProvider) {
    
    $dialogProvider.options({ 
      backdropClick: false,
      backdropFade: true
    });

  }]);
});