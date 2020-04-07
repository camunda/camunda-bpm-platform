import  { Injector} from '@angular/core';
import  { createCustomElement } from '@angular/elements';

import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { ActivityTableComponent } from './activity-table/activity-table.component';

@NgModule({
  declarations: [
    ActivityTableComponent,
  ],
  imports: [
    BrowserModule
  ],
  entryComponents :  [
    ActivityTableComponent
 ]
})
export class AppModule {
  constructor(private injector : Injector){
    const el = createCustomElement(ActivityTableComponent, {injector : this.injector});

    customElements.define('activity-table',el);

  
  }
  ngDoBootstrap(){}
  }
