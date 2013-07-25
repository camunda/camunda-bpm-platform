define([ 'angular', 'jquery', 'cockpit/directives/date' ], function(angular, $) {

  /**
   * @see http://docs.angularjs.org/guide/dev_guide.unit-testing
   *      for how to write unit tests in AngularJS
   */
  return describe('directives', function() {

    describe('directive date', function() {
      var element;

      function createElement(content) {
        return $(content).appendTo(document.body);
      }

      afterEach(function() {
        $(document.body).html('');
        dealoc(element);
      });

      beforeEach(function() {
        angular.module('testmodule', [ 'cockpit.directives' ]);
      });

      // load app that uses the directive
      beforeEach(module('testmodule'));

      it('should accept date value', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('2013-07-12T09:14:15');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(false);
        expect(element.hasClass('ng-valid')).toBe(true);
        expect($rootScope.value).toBe('2013-07-12T09:14:15');
      }));

      it('should not accept date value (1)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('a2013-07-12T09:14:15');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('a2013-07-12T09:14:15');
      }));

      it('should not accept date value (2)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('213-07-12T09:14:15');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('213-07-12T09:14:15');
      }));

      it('should not accept date value (3)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('2013-7-12T09:14:15');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('2013-7-12T09:14:15');
      }));
   
      it('should not accept date value (4)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('2013-07-2T09:14:15');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('2013-07-2T09:14:15');
      }));

      it('should not accept date value (5)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('2013-07-1209:14:15');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('2013-07-1209:14:15');
      }));

      it('should not accept date value (6)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('2013-07-1209:14:15');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('2013-07-1209:14:15');
      }));

      it('should not accept date value (7)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('2013-07-12T9:14:15');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('2013-07-12T9:14:15');
      }));

      it('should not accept date value (8)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('2013-07-12T09:4:15');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('2013-07-12T09:4:15');
      }));

      it('should not accept date value (9)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('2013-07-12T09:14:5');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('2013-07-12T09:14:5');
      }));      

      it('should not accept date value (10)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('201307-12T09:14:15');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('201307-12T09:14:15');
      }));      

      it('should not accept date value (11)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('2013-0712T09:14:15');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('2013-0712T09:14:15');
      }));  

      it('should not accept date value (12)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('2013-07-12T0914:15');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('2013-07-12T0914:15');
      }));  

      it('should not accept date value (13)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('2013-07-12T09:1415');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('2013-07-12T09:1415');
      }));  

      it('should not accept date value (13)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('2013-13-12T09:14:15');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('2013-13-12T09:14:15');
      }));  

      it('should not accept date value (14)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('2013-22-12T09:14:15');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('2013-22-12T09:14:15');
      })); 

      it('should not accept date value (14)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('2013-12-32T09:14:15');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('2013-12-32T09:14:15');
      })); 

      it('should not accept date value (15)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('2013-12-41T09:14:15');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('2013-12-41T09:14:15');
      })); 

      it('should not accept date value (16)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('2013-12-31T24:14:15');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('2013-12-31T24:14:15');
      })); 


      it('should not accept date value (17)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('2013-12-31T34:14:15');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('2013-12-31T34:14:15');
      })); 

      it('should not accept date value (17)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('2013-12-31T14:60:15');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('2013-12-31T14:60:15');
      })); 

      it('should not accept date value (18)', inject(function($rootScope, $compile, $sniffer) {
        // given
        $rootScope.value = '';

        element = $compile('<input date ng-model="value" />')($rootScope);
        $rootScope.$digest();

        // when

        element.val('2013-12-31T14:59:60');
        browserTrigger(element, ($sniffer.hasEvent('input')) ? 'input' : 'change');

        // then
        expect(element.hasClass('ng-invalid-date')).toBe(true);
        expect(element.hasClass('ng-valid')).toBe(false);
        expect($rootScope.value).toBe('2013-12-31T14:59:60');
      })); 

    });
  });
});