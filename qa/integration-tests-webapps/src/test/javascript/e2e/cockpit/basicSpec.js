describe('cockpit dashboard', function() {

  function login(username, password, valid) {    
    element(by.model('username')).clear();
    element(by.model('password')).clear();
    element(by.model('username')).sendKeys(username);
    element(by.model('password')).sendKeys(password);

    var submitButton = element(by.css('.btn-primary.btn-large'));
    submitButton.click();
    if ((valid == true) || (valid == undefined) ) {
        expectLoginSuccessful(username);
    } else {
        expectLoginFailed();
    }
  }

  function expectLoginSuccessful(username) {
    var loggedInUserMenu = element(by.binding('authentication.user.name'));
    expect(loggedInUserMenu.getText()).toEqual(username);
  }

  function expectLoginFailed() {    
    var notification = element(by.binding('notification.message'))
    expect(notification.getText()).toEqual('Wrong credentials or missing access rights to application');    
  }

  function fullView() {
    var items = element.all(by.repeater('provider in providers'));

    expect(items.get(0).getText()).toEqual('Full');
    items.get(0).click();
    expect(items.get(0).getAttribute('class')).toMatch('active');
    expect(items.get(1).getAttribute('class')).not.toMatch('active');
  }

  function liveView() {
    var items = element.all(by.repeater('provider in providers'));

    expect(items.get(1).getText()).toEqual('Live');
    items.get(1).click();
    expect(items.get(1).getAttribute('class')).toMatch('active');
    expect(items.get(0).getAttribute('class')).not.toMatch('active');    
  }  

  function selectActivityInDiagram(activityName) {
    var activity = element(by.css('.process-diagram *[data-activity-id=' + '"' + activityName + '"' + ']'));    
    activity.click();        
  }

  function deselectActivityInInstanceTree(activityName) {
    var treeNodeButton = element(by.css('.instance-tree *[id^=' + '"' + activityName + ':"' + '] button'));
    treeNodeButton.click();
  }
 
// ---- start page ----
  xit('should validate start page', function() {
    //TODO
  })

// ---- user login ----
  it('should validate user credentials', function() {
    //set preconditions
    browser.driver.manage().window().maximize();

    browser.get('http://localhost:8080/camunda');

    login('jonny1', 'jonny3', false);
    login('jonny1', 'jonny1', true);
  });

// ---- start page plugin view ----
  it('should check existence of plugins', function() {
    var plugins = element.all(by.repeater('dashboardProvider in dashboardProviders'));

    expect(plugins.count()).toBe(2);    
    expect(plugins.get(0).findElement(by.css('.page-header')).getText()).toEqual('Deployed Processes (List)');
    expect(plugins.get(1).findElement(by.css('.page-header')).getText()).toEqual('Deployed Processes (Icons)');
  });

  it('should find process in Deployed Processes (List)', function() {
    var items = element(by.repeater('statistic in statistics').row(0).column('definition.name'));

    expect(items.getText()).toEqual('Another Failing Process');
  });    

  it('should count running instances of single process', function() {
    var runningInstances = element(by.css('.table'))
                                  .findElement(by.repeater('statistic in statistics').row(0).column('.instances'));

    expect(runningInstances.getText()).toEqual('10');
  });  

  xit('should count deployed processes', function() {
    //TODO
  });

// ---- process defintions view
  it('it should select process in Deployed Process (List)', function() {
    var items = element(by.repeater('statistic in statistics').row(4).column('definition.name'));

    expect(items.getText()).toEqual('Cornercases Process');

    items.click();


    var processDefintionName = element(by.binding('processDefinition.name'));

    expect(processDefintionName.getText()).toEqual('Cornercases Process');
  });

  it('should validate action buttons', function() {
    var items = element.all(by.repeater('actionProvider in processDefinitionActions'));

    expect(items.count()).toBe(1);
  });

  it('it should select process instance in Process Instances Table', function() {
    var instance = element(by.repeater('processInstance in processInstances').row(1).column('id'));  
    var instanceId = instance.getAttribute('title');   //instance.getText();

    instance.click();
    expect(element(by.binding('processInstance.id')).getText()).toBe(instanceId);
  });  

// ---- instance detail view
  it('should select activity in diagram', function() {
    selectActivityInDiagram('UserTask_2');   
    expect(element(by.css('.process-diagram *[data-activity-id="UserTask_2"]'))
                .getAttribute('class')).toMatch('activity-highlight');
  });

  it('should switch tab in instance details view', function() {
    element(by.repeater('tabProvider in processInstanceTabs').row(3).column('label')).click();

    var tabContent = element(by.css('view[provider=selectedTab]'))
                          .findElement(by.repeater('userTask in userTasks')
                          .row(0).column('name'));    
    expect(tabContent.getText()).toEqual('Inner Task');      
  });

  it('should dispaly variables of selected activity only', function() {
    element(by.css('view[provider=selectedTab]'))
          .findElement(by.repeater('userTask in userTasks').row(0).column('name')).click();

    var items = element.all(by.repeater('userTask in userTasks'));
    expect(items.count()).toBe(1);
  });

  it('should deselect activity in instance tree', function() {
    deselectActivityInInstanceTree('UserTask_2');
    expect(element(by.css('.process-diagram *[data-activity-id="UserTask_2"]'))
                .getAttribute('class')).not.toMatch('activity-highlight');
  });

  it('should select Activity in instance details view and highlight element in renderer', function() {
    element(by.css('view[provider=selectedTab]'))
          .findElement(by.repeater('userTask in userTasks').row(5).column('name')).click();

    expect(element(by.css('.process-diagram *[data-activity-id="UserTask_5"]'))
                .getAttribute('class')).toMatch('activity-highlight');

    var items = element.all(by.repeater('userTask in userTasks'));
    expect(items.count()).toBe(1);
  });

  it('should select Activity in diagram and and count tasks in details view', function() {
    selectActivityInDiagram('UserTask_5');
    expect(element(by.css('.process-diagram *[data-activity-id="UserTask_5"]'))
              .getAttribute('class')).toMatch('activity-highlight');

    var items = element.all(by.repeater('userTask in userTasks'));    
    expect(items.count()).toBe(5);
    
    var tabContent = element(by.css('view[provider=selectedTab]'))
                          .findElement(by.repeater('userTask in userTasks').row(0).column('name'));    
    expect(tabContent.getText()).toEqual('Run some service');    
  }); 

  xit('should manage groups for user task', function() {
    //TODO
  });

// ---- history view ----
  it('should switch to full view', function() {
    var items = element.all(by.repeater('tabProvider in processInstanceActions'));

    expect(items.count()).toBe(4);    
    fullView();
    expect(items.count()).toBe(0);
  });

  it('should switch to live view', function() {
    var items = element.all(by.repeater('tabProvider in processInstanceActions'));
    
    expect(items.count()).toBe(0);    
    liveView();
    expect(items.count()).toBe(4);
  }); 

  // ---- instance tree view ----
  it('should validate activity instance tree filter - sidebar', function() {  
    var filterSidebar = element(by.css('.filters'));

    expect(filterSidebar.findElement(by.tagName('h5')).getText()).toBe('Filter');
  });

  it('should validate activity instance tree filter - clear button', function() {
    var filterSidebar = element(by.css('.filters'));

    filterSidebar.findElement(by.model('name')).clear();
    filterSidebar.findElement(by.model('name')).sendKeys('some task');

    //filterSidebar.findElement(by.css('.icon-search')).click(); --> movement of vertical divider bar needed!!!
    filterSidebar.findElement(by.model('name')).clear();
    expect(filterSidebar.findElement(by.model('name')).getText()).not.toBe('some task');
  });

  it('should validate activity instance tree filter - filter activities', function() {
    var filterSidebar = element(by.css('.filters'));
    var filterResult = filterSidebar.findElement(by.css('.filter'));

    filterSidebar.findElement(by.model('name')).clear();
    filterSidebar.findElement(by.model('name')).sendKeys('some task');

    expect(filterResult.getText()).toBe('Nothing selected');

    selectActivityInDiagram('UserTask_2');
    expect(filterResult.getText()).toBe('1 activity instance selected'); 

    deselectActivityInInstanceTree('UserTask_2');
    expect(filterResult.getText()).toBe('Nothing selected');
  });

});

