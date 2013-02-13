'use strict';

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

describe('cycle connectors', function() {

  var newButtonSelect = 'a.btn[data-ng-click="createNew()"]';
	
  beforeEach(function() {
    browser().navigateTo('../app/secured/view/connectors');
    element(newButtonSelect).click();
  });
  
  it('should be on view/connectors page after navigation', function() {
    expect(browser().window().path()).toBe("/cycle/app/secured/view/connectors/");
  });
  
  
  it('should contain "New" button', function() {
    expect(element(newButtonSelect).count()).toBe(1);
  });
  
  
  describe('new Filesystem connector dialog', function() {
	 
    it('should show BASE_PATH required property if File System Connector is selected', function() {

      select('blueprint').option(0);
      
      // 2 -> one is visible the other one is hidden (password / text)
      expect(element('span:contains(BASE_PATH*)').count()).toBe(2);      
      expect(element('#BASE_PATH').val()).toBe('${user.home}');      
      expect(element('#BASE_PATH').attr("required")).toBe('required');
      
    });
	    
  });
  

  describe('new Signavio connector dialog', function() {

  	beforeEach(function() {
  		select('blueprint').option(1);
  	});
  
  	it('should show the signavioBaseUrl input field', function() {
  		expect(element('span:contains(signavioBaseUrl*)').count()).toBe(2);
  		expect(element('#signavioBaseUrl').val()).toBe('https://editor.signavio.com/');
  		expect(element('#signavioBaseUrl').attr("required")).toBe('required');
  	});
  
  	it('should show the proxyUrl input field', function() {
  		expect(element('span:contains(proxyUrl)').count()).toBe(2);
  		expect(element('#proxyUrl').val()).toBe('');
  		expect(element('#proxyUrl').attr("required")).toBe(undefined);
  	});
  	
  	it('should show the proxyUsername input field', function() {
  		expect(element('span:contains(proxyUsername)').count()).toBe(2);
  		expect(element('#proxyUsername').val()).toBe('');
  		expect(element('#proxyUsername').attr("required")).toBe(undefined);
  	});
  	
  	it('should show the proxyPassword input field', function() {
  		expect(element('span:contains(proxyPassword)').count()).toBe(2);
  		expect(element('#proxyPassword').val()).toBe('');
  		expect(element('#proxyPassword').attr("required")).toBe(undefined);
  	});

  });
  
  describe('new Subversion Connector dialog', function() {
		 
    it('should show required property repositoryPath if Subversion Connector is selected', function() {

      select('blueprint').option(2);
      
      // 2 -> one is visible the other one is hidden (password / text)
      expect(element('span:contains(repositoryPath*)').count()).toBe(2);      
      expect(element('#repositoryPath').val()).toBe('');      
      expect(element('#repositoryPath').attr("required")).toBe('required');
      
    });
	    
  });
	    
	   
});
