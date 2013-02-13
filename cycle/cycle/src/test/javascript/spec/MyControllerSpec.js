describe("MyController", function() {
  var $httpBackend, $rootScope, $controller;

  beforeEach(inject(function($injector) {
    $httpBackend = $injector.get('$httpBackend');
    
    $rootScope = $injector.get('$rootScope');
    $controller = $injector.get('$controller');
    
    $httpBackend.when('GET', 'app/name').respond({ name: 'Klaus' });
  }));

  it('Should publish name', function() {
    var scope = $rootScope.$new();
    var ctrl = $controller(MyController, {
      $scope: scope
    });
  
    $httpBackend.flush();
  
    // name should be klaus
    expect(scope.name).toBe('Klaus');
  });

  it('Should query backend for name', function() {
    $httpBackend.expectGET('app/name');

    var scope = $rootScope.$new();
    var ctrl = $controller(MyController, {
      $scope: scope
    });
  
    $httpBackend.flush();
  
    $httpBackend.verifyNoOutstandingExpectation();
    $httpBackend.verifyNoOutstandingRequest();
  });

});