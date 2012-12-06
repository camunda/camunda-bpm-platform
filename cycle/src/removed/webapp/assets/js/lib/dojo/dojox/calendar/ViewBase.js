//>>built
define("dojox/calendar/ViewBase",["dojo/_base/declare","dojo/_base/lang","dojo/_base/array","dojo/_base/window","dojo/_base/event","dojo/_base/html","dojo/_base/sniff","dojo/query","dojo/dom","dojo/dom-style","dojo/dom-construct","dojo/on","dojo/date","dojo/date/locale","dijit/_WidgetBase","dojox/widget/_Invalidating","dojox/widget/Selection","dojox/calendar/time","./StoreMixin"],function(_1,_2,_3,_4,_5,_6,_7,_8,_9,_a,_b,on,_c,_d,_e,_f,_10,_11,_12){
return _1("dojox.calendar.ViewBase",[_e,_12,_f,_10],{datePackage:_c,_calendar:"gregorian",viewKind:null,_layoutStep:1,_layoutUnit:"day",resizeCursor:"n-resize",formatItemTimeFunc:null,_getFormatItemTimeFuncAttr:function(){
if(this.owner!=null){
return this.owner.get("formatItemTimeFunc");
}else{
return this.formatItemTimeFunc;
}
},_viewHandles:null,doubleTapDelay:300,constructor:function(_13){
_13=_13||{};
this._calendar=_13.datePackage?_13.datePackage.substr(_13.datePackage.lastIndexOf(".")+1):this._calendar;
this.dateModule=_13.datePackage?_2.getObject(_13.datePackage,false):_c;
this.dateClassObj=this.dateModule.Date||Date;
this.dateLocaleModule=_13.datePackage?_2.getObject(_13.datePackage+".locale",false):_d;
this.rendererPool=[];
this.rendererList=[];
this.itemToRenderer={};
this._viewHandles=[];
},destroy:function(_14){
while(this.rendererList.length>0){
this._destroyRenderer(this.rendererList.pop());
}
for(kind in this._rendererPool){
var _15=this._rendererPool[kind];
if(_15){
while(_15.length>0){
this._destroyRenderer(_15.pop());
}
}
}
while(this._viewHandles.length>0){
this._viewHandles.pop().remove();
}
this.inherited(arguments);
},_createRenderData:function(){
},_validateProperties:function(){
},_setText:function(_16,_17,_18){
if(_17!=null){
if(!_18&&_16.hasChildNodes()){
_16.childNodes[0].childNodes[0].nodeValue=_17;
}else{
while(_16.hasChildNodes()){
_16.removeChild(_16.lastChild);
}
var _19=_4.doc.createElement("span");
this.applyTextDir(_19,_17);
if(_18){
_19.innerHTML=_17;
}else{
_19.appendChild(_4.doc.createTextNode(_17));
}
_16.appendChild(_19);
}
}
},isAscendantHasClass:function(_1a,_1b,_1c){
while(_1a!=_1b&&_1a!=document){
if(dojo.hasClass(_1a,_1c)){
return true;
}
_1a=_1a.parentNode;
}
return false;
},isWeekEnd:function(_1d){
return _d.isWeekend(_1d);
},getWeekNumberLabel:function(_1e){
if(_1e.toGregorian){
_1e=_1e.toGregorian();
}
return _d.format(_1e,{selector:"date",datePattern:"w"});
},floorToDay:function(_1f,_20){
return _11.floorToDay(_1f,_20,this.dateClassObj);
},floorToMonth:function(_21,_22){
return _11.floorToMonth(_21,_22,this.dateClassObj);
},floorDate:function(_23,_24,_25,_26){
return _11.floor(_23,_24,_25,_26,this.dateClassObj);
},isToday:function(_27){
return _11.isToday(_27,this.dateClassObj);
},isStartOfDay:function(d){
return _11.isStartOfDay(d,this.dateClassObj,this.dateModule);
},isOverlapping:function(_28,_29,_2a,_2b,_2c,_2d){
if(_29==null||_2b==null||_2a==null||_2c==null){
return false;
}
var cal=_28.dateModule;
if(_2d){
if(cal.compare(_29,_2c)==1||cal.compare(_2b,_2a)==1){
return false;
}
}else{
if(cal.compare(_29,_2c)!=-1||cal.compare(_2b,_2a)!=-1){
return false;
}
}
return true;
},computeRangeOverlap:function(_2e,_2f,_30,_31,_32,_33){
var cal=_2e.dateModule;
if(_2f==null||_31==null||_30==null||_32==null){
return null;
}
var _34=cal.compare(_2f,_32);
var _35=cal.compare(_31,_30);
if(_33){
if(_34==0||_34==1||_35==0||_35==1){
return null;
}
}else{
if(_34==1||_35==1){
return null;
}
}
return [this.newDate(cal.compare(_2f,_31)>0?_2f:_31,_2e),this.newDate(cal.compare(_30,_32)>0?_32:_30,_2e)];
},isSameDay:function(_36,_37){
if(_36==null||_37==null){
return false;
}
return _36.getFullYear()==_37.getFullYear()&&_36.getMonth()==_37.getMonth()&&_36.getDate()==_37.getDate();
},computeProjectionOnDate:function(_38,_39,_3a,max){
var cal=_38.dateModule;
if(max<=0||cal.compare(_3a,_39)==-1){
return 0;
}
var _3b=this.floorToDay(_39,false,_38);
if(_3a.getDate()!=_3b.getDate()){
if(_3a.getMonth()==_3b.getMonth()){
if(_3a.getDate()<_3b.getDate()){
return 0;
}else{
if(_3a.getDate()>_3b.getDate()){
return max;
}
}
}else{
if(_3a.getFullYear()==_3b.getFullYear()){
if(_3a.getMonth()<_3b.getMonth()){
return 0;
}else{
if(_3a.getMonth()>_3b.getMonth()){
return max;
}
}
}else{
if(_3a.getFullYear()<_3b.getFullYear()){
return 0;
}else{
if(_3a.getFullYear()>_3b.getFullYear()){
return max;
}
}
}
}
}
var res;
if(this.isSameDay(_39,_3a)){
var d=_2.clone(_39);
var _3c=0;
if(_38.minHours!=null&&_38.minHours!=0){
d.setHours(_38.minHours);
_3c=d.getHours()*3600+d.getMinutes()*60+d.getSeconds();
}
d=_2.clone(_39);
var _3d;
if(_38.maxHours==null||_38.maxHours==24){
_3d=86400;
}else{
d.setHours(_38.maxHours);
_3d=d.getHours()*3600+d.getMinutes()*60+d.getSeconds();
}
var _3e=_3a.getHours()*3600+_3a.getMinutes()*60+_3a.getSeconds()-_3c;
if(_3e<0){
return 0;
}
if(_3e>_3d){
return max;
}
res=(max*_3e)/(_3d-_3c);
}else{
if(_3a.getDate()<_39.getDate()&&_3a.getMonth()==_39.getMonth()){
return 0;
}
var d2=this.floorToDay(_3a);
var dp1=_38.dateModule.add(_39,"day",1);
dp1=this.floorToDay(dp1,false,_38);
if(cal.compare(d2,_39)==1&&cal.compare(d2,dp1)==0||cal.compare(d2,dp1)==1){
res=max;
}else{
res=0;
}
}
return res;
},getTime:function(e,x,y,_3f){
return null;
},newDate:function(obj){
return _11.newDate(obj,this.dateClassObj);
},_isItemInView:function(_40){
var rd=this.renderData;
var cal=rd.dateModule;
if(cal.compare(_40.startTime,rd.startTime)==-1){
return false;
}
if(cal.compare(_40.endTime,rd.endTime)==1){
return false;
}
return true;
},_ensureItemInView:function(_41){
var rd=this.renderData;
var cal=rd.dateModule;
var _42=Math.abs(cal.difference(_41.startTime,_41.endTime,"millisecond"));
var _43=false;
if(cal.compare(_41.startTime,rd.startTime)==-1){
_41.startTime=rd.startTime;
_41.endTime=cal.add(_41.startTime,"millisecond",_42);
_43=true;
}else{
if(cal.compare(_41.endTime,rd.endTime)==1){
_41.endTime=rd.endTime;
_41.startTime=cal.add(_41.endTime,"millisecond",-_42);
_43=true;
}
}
return _43;
},scrollable:true,autoScroll:true,_autoScroll:function(gx,gy,_44){
return false;
},scrollMethod:"auto",_setScrollMethodAttr:function(_45){
if(this.scrollMethod!=_45){
this.scrollMethod=_45;
if(this._domScroll!==undefined){
if(this._domScroll){
_a.set(this.sheetContainer,this._cssPrefix+"transform","translateY(-"+pos+"px)");
}else{
this.scrollContainer.scrollTop=0;
}
}
delete this._domScroll;
var pos=this._getScrollPosition();
delete this._scrollPos;
this._setScrollPosition(pos);
}
},_startAutoScroll:function(_46){
var sp=this._scrollProps;
if(!sp){
sp=this._scrollProps={};
}
sp.scrollStep=_46;
if(!sp.isScrolling){
sp.isScrolling=true;
sp.scrollTimer=setInterval(_2.hitch(this,this._onScrollTimer_tick),10);
}
},_stopAutoScroll:function(){
var sp=this._scrollProps;
if(sp&&sp.isScrolling){
clearInterval(sp.scrollTimer);
sp.scrollTimer=null;
}
this._scrollProps=null;
},_onScrollTimer_tick:function(pos){
},_scrollPos:0,getCSSPrefix:function(){
if(_7("ie")){
return "-ms-";
}
if(_7("webkit")){
return "-webkit-";
}
if(_7("mozilla")){
return "-moz-";
}
if(_7("opera")){
return "-o-";
}
},_setScrollPosition:function(pos){
if(this._scrollPos==pos){
return;
}
if(this._domScroll===undefined){
var sm=this.get("scrollMethod");
if(sm==="auto"){
this._domScroll=!_7("ios")&&!_7("android")&&!_7("webkit");
}else{
this._domScroll=sm==="dom";
}
}
this._scrollPos=pos;
if(this._domScroll){
this.scrollContainer.scrollTop=pos;
}else{
if(!this._cssPrefix){
this._cssPrefix=this.getCSSPrefix();
}
_a.set(this.sheetContainer,this._cssPrefix+"transform","translateY(-"+pos+"px)");
}
},_getScrollPosition:function(){
return this._scrollPos;
},scrollView:function(dir){
},ensureVisibility:function(_47,end,_48,_49,_4a){
},_getStoreAttr:function(){
if(this.owner){
return this.owner.get("store");
}
return this.store;
},_setItemsAttr:function(_4b){
this._set("items",_4b);
this.displayedItemsInvalidated=true;
},_refreshItemsRendering:function(){
var rd=this.renderData;
this._computeVisibleItems(rd);
this._layoutRenderers(rd);
},invalidateLayout:function(){
this._layoutRenderers(this.renderData);
},resize:function(){
},computeOverlapping:function(_4c,_4d){
if(_4c.length==0){
return {numLanes:0,addedPassRes:[1]};
}
var _4e;
var _4f=[];
for(var i=0;i<_4c.length;i++){
var _50=_4c[i];
this._layoutPass1(_50,_4f);
}
var _51;
if(_4d){
_51=_2.hitch(this,_4d)(_4f);
}
return {numLanes:_4f.length,addedPassRes:_51};
},_layoutPass1:function(_52,_53){
var _54=true;
for(var i=0;i<_53.length;i++){
var _55=_53[i];
_54=false;
for(var j=0;j<_55.length&&!_54;j++){
if(_55[j].start<_52.end&&_52.start<_55[j].end){
_54=true;
_55[j].extent=1;
}
}
if(!_54){
_52.lane=i;
_52.extent=-1;
_55.push(_52);
return;
}
}
_53.push([_52]);
_52.lane=_53.length-1;
_52.extent=-1;
},_layoutInterval:function(_56,_57,_58,end,_59){
},layoutPriorityFunction:null,_sortItemsFunction:function(a,b){
var res=this.dateModule.compare(a.startTime,b.startTime);
if(res==0){
res=-1*this.dateModule.compare(a.endTime,b.endTime);
}
return res;
},_layoutRenderers:function(_5a){
if(!_5a.items){
return;
}
this._recycleItemRenderers();
var cal=_5a.dateModule;
var _5b=this.newDate(_5a.startTime);
var _5c=_2.clone(_5b);
var _5d;
var _5e=_5a.items.concat();
var _5f=[],_60;
var _61=0;
while(cal.compare(_5b,_5a.endTime)==-1&&_5e.length>0){
_5d=cal.add(_5b,this._layoutUnit,this._layoutStep);
_5d=this.floorToDay(_5d,true,_5a);
var _62=_2.clone(_5d);
if(_5a.minHours){
_5c.setHours(_5a.minHours);
}
if(_5a.maxHours&&_5a.maxHours!=24){
_62=cal.add(_5d,"day",-1);
_62=this.floorToDay(_62,true,_5a);
_62.setHours(_5a.maxHours);
}
_60=_3.filter(_5e,function(_63){
var r=this.isOverlapping(_5a,_63.startTime,_63.endTime,_5c,_62);
if(r){
if(cal.compare(_63.endTime,_62)==1){
_5f.push(_63);
}
}else{
_5f.push(_63);
}
return r;
},this);
_5e=_5f;
_5f=[];
if(_60.length>0){
_60.sort(_2.hitch(this,this.layoutPriorityFunction?this.layoutPriorityFunction:this._sortItemsFunction));
this._layoutInterval(_5a,_61,_5c,_62,_60);
}
_5b=_5d;
_5c=_2.clone(_5b);
_61++;
}
this._onRenderersLayoutDone(this);
},_recycleItemRenderers:function(_64){
while(this.rendererList.length>0){
this._recycleRenderer(this.rendererList.pop(),_64);
}
this.itemToRenderer={};
},rendererPool:null,rendererList:null,itemToRenderer:null,getRenderers:function(_65){
if(_65==null||_65.id==null){
return null;
}
var _66=this.itemToRenderer[_65.id];
return _66==null?null:_66.concat();
},_rendererHandles:{},itemToRendererKindFunc:null,_itemToRendererKind:function(_67){
if(this.itemToRendererKindFunc){
return this.itemToRendererKindFunc(_67);
}
return this._defaultItemToRendererKindFunc(_67);
},_defaultItemToRendererKindFunc:function(_68){
return null;
},_createRenderer:function(_69,_6a,_6b,_6c){
if(_69!=null&&_6a!=null&&_6b!=null){
var res,_6d;
var _6e=this.rendererPool[_6a];
if(_6e!=null){
res=_6e.shift();
}
if(res==null){
_6d=new _6b;
var _6f=_b.create("div");
_6f.className="dojoxCalendarEventContainer "+_6c;
_6f.appendChild(_6d.domNode);
res={renderer:_6d,container:_6d.domNode,kind:_6a};
this.onRendererCreated(res);
}else{
_6d=res.renderer;
this.onRendererReused(_6d);
}
_6d.owner=this;
_6d.set("rendererKind",_6a);
_6d.set("item",_69);
var _70=this.itemToRenderer[_69.id];
if(_70==null){
this.itemToRenderer[_69.id]=_70=[];
}
_70.push(res);
this.rendererList.push(res);
return res;
}
return null;
},onRendererCreated:function(_71){
},onRendererRecycled:function(_72){
},onRendererReused:function(_73){
},onRendererDestroyed:function(_74){
},_onRenderersLayoutDone:function(_75){
this.onRenderersLayoutDone(_75);
if(this.owner!=null){
this.owner.onRenderersLayoutDone(_75);
}
},onRenderersLayoutDone:function(_76){
},_recycleRenderer:function(_77,_78){
this.onRendererRecycled(_77);
var _79=this.rendererPool[_77.kind];
if(_79==null){
this.rendererPool[_77.kind]=[_77];
}else{
_79.push(_77);
}
if(_78){
_77.container.parentNode.removeChild(_77.container);
}
_a.set(_77.container,"display","none");
_77.renderer.owner=null;
_77.renderer.set("item",null);
},_destroyRenderer:function(_7a){
this.onRendererDestroyed(_7a);
var ir=_7a.renderer;
_3.forEach(ir.__handles,function(_7b){
_7b.remove();
});
if(ir["destroy"]){
ir.destroy();
}
_6.destroy(_7a.container);
},_destroyRenderersByKind:function(_7c){
var _7d=[];
for(var i=0;i<this.rendererList.length;i++){
var ir=this.rendererList[i];
if(ir.kind==_7c){
this._destroyRenderer(ir);
}else{
_7d.push(ir);
}
}
this.rendererList=_7d;
var _7e=this.rendererPool[_7c];
if(_7e){
while(_7e.length>0){
this._destroyRenderer(_7e.pop());
}
}
},_updateEditingCapabilities:function(_7f,_80){
var _81=this.isItemMoveEnabled(_7f,_80.rendererKind);
var _82=this.isItemResizeEnabled(_7f,_80.rendererKind);
var _83=false;
if(_81!=_80.get("moveEnabled")){
_80.set("moveEnabled",_81);
_83=true;
}
if(_82!=_80.get("resizeEnabled")){
_80.set("resizeEnabled",_82);
_83=true;
}
if(_83){
_80.updateRendering();
}
},updateRenderers:function(obj,_84){
if(obj==null){
return;
}
var _85=_2.isArray(obj)?obj:[obj];
for(var i=0;i<_85.length;i++){
var _86=_85[i];
if(_86==null||_86.id==null){
continue;
}
var _87=this.itemToRenderer[_86.id];
if(_87==null){
continue;
}
var _88=this.isItemSelected(_86);
var _89=this.isItemHovered(_86);
var _8a=this.isItemBeingEdited(_86);
var _8b=this.showFocus?this.isItemFocused(_86):false;
for(var j=0;j<_87.length;j++){
var _8c=_87[j].renderer;
_8c.set("hovered",_89);
_8c.set("selected",_88);
_8c.set("edited",_8a);
_8c.set("focused",_8b);
this.applyRendererZIndex(_86,_87[j],_89,_88,_8a,_8b);
if(!_84){
_8c.set("item",_86);
if(_8c.updateRendering){
_8c.updateRendering();
}
}
}
}
},applyRendererZIndex:function(_8d,_8e,_8f,_90,_91,_92){
_a.set(_8e.container,{"zIndex":_91||_90?20:_8d.lane==undefined?0:_8d.lane});
},getIdentity:function(_93){
return this.owner?this.owner.getIdentity(_93):_93.id;
},_setHoveredItem:function(_94,_95){
if(this.owner){
this.owner._setHoveredItem(_94,_95);
return;
}
if(this.hoveredItem&&_94&&this.hoveredItem.id!=_94.id||_94==null||this.hoveredItem==null){
var old=this.hoveredItem;
this.hoveredItem=_94;
this.updateRenderers([old,this.hoveredItem],true);
if(_94&&_95){
this._updateEditingCapabilities(_94,_95);
}
}
},hoveredItem:null,isItemHovered:function(_96){
if(this._isEditing&&this._edProps){
return _96.id==this._edProps.editedItem.id;
}else{
return this.owner?this.owner.isItemHovered(_96):this.hoveredItem!=null&&this.hoveredItem.id==_96.id;
}
},isItemFocused:function(_97){
return this._isItemFocused?this._isItemFocused(_97):false;
},_setSelectionModeAttr:function(_98){
if(this.owner){
this.owner.set("selectionMode",_98);
}else{
this.inherited(arguments);
}
},_getSelectionModeAttr:function(_99){
if(this.owner){
return this.owner.get("selectionMode");
}else{
return this.inherited(arguments);
}
},_setSelectedItemAttr:function(_9a){
if(this.owner){
this.owner.set("selectedItem",_9a);
}else{
this.inherited(arguments);
}
},_getSelectedItemAttr:function(_9b){
if(this.owner){
return this.owner.get("selectedItem");
}else{
return this.selectedItem;
}
},_setSelectedItemsAttr:function(_9c){
if(this.owner){
this.owner.set("selectedItems",_9c);
}else{
this.inherited(arguments);
}
},_getSelectedItemsAttr:function(){
if(this.owner){
return this.owner.get("selectedItems");
}else{
return this.inherited(arguments);
}
},isItemSelected:function(_9d){
if(this.owner){
return this.owner.isItemSelected(_9d);
}else{
return this.inherited(arguments);
}
},selectFromEvent:function(e,_9e,_9f,_a0){
if(this.owner){
this.owner.selectFromEvent(e,_9e,_9f,_a0);
}else{
this.inherited(arguments);
}
},setItemSelected:function(_a1,_a2){
if(this.owner){
this.owner.setItemSelected(_a1,_a2);
}else{
this.inherited(arguments);
}
},createItemFunc:null,_getCreateItemFuncAttr:function(){
if(this.owner){
return this.owner.get("createItemFunc");
}else{
return this.createItemFunc;
}
},createOnGridClick:false,_getCreateOnGridClickAttr:function(){
if(this.owner){
return this.owner.get("createOnGridClick");
}else{
return this.createOnGridClick;
}
},_gridMouseDown:false,_onGridMouseDown:function(e){
this._gridMouseDown=true;
this.showFocus=false;
if(this._isEditing){
this._endItemEditing("mouse",false);
}
this._doEndItemEditing(this.owner,"mouse");
this.set("focusedItem",null);
this.selectFromEvent(e,null,null,true);
if(this._setTabIndexAttr){
this[this._setTabIndexAttr].focus();
}
if(this._onRendererHandleMouseDown){
var f=this.get("createItemFunc");
if(!f){
return;
}
var _a3=f(this,this.getTime(e),e);
var _a4=this.get("store");
if(!_a3||_a4==null){
return;
}
_a4.put(_a3);
var _a5=this.getRenderers(_a3)[0];
if(!_a5){
return;
}
this._onRendererHandleMouseDown(e,_a5.renderer,"resizeEnd");
}
},_onGridMouseMove:function(e){
},_onGridMouseUp:function(e){
},_onGridTouchStart:function(e){
var p=this._edProps;
this._gridProps={event:e,fromItem:this.isAscendantHasClass(e.target,this.eventContainer,"dojoxCalendarEventContainer")};
if(this._isEditing){
if(this._gridProps){
this._gridProps.editingOnStart=true;
}
_2.mixin(p,this._getTouchesOnRenderers(e,p.editedItem));
if(p.touchesLen==0){
if(p&&p.endEditingTimer){
clearTimeout(p.endEditingTimer);
p.endEditingTimer=null;
}
this._endItemEditing("touch",false);
}
}
this._doEndItemEditing(this.owner,"touch");
_5.stop(e);
},_doEndItemEditing:function(obj,_a6){
if(obj&&obj._isEditing){
p=obj._edProps;
if(p&&p.endEditingTimer){
clearTimeout(p.endEditingTimer);
p.endEditingTimer=null;
}
obj._endItemEditing(_a6,false);
}
},_onGridTouchEnd:function(e){
},_onGridTouchMove:function(e){
},__fixEvt:function(e){
return e;
},_dispatchCalendarEvt:function(e,_a7){
e=this.__fixEvt(e);
this[_a7](e);
if(this.owner){
this.owner[_a7](e);
}
return e;
},_onGridClick:function(e){
if(!e.triggerEvent){
e={date:this.getTime(e),triggerEvent:e};
}
this._dispatchCalendarEvt(e,"onGridClick");
},onGridClick:function(e){
},_onGridDoubleClick:function(e){
if(!e.triggerEvent){
e={date:this.getTime(e),triggerEvent:e};
}
this._dispatchCalendarEvt(e,"onGridDoubleClick");
},onGridDoubleClick:function(e){
},_onItemClick:function(e){
this._dispatchCalendarEvt(e,"onItemClick");
},onItemClick:function(e){
},_onItemDoubleClick:function(e){
this._dispatchCalendarEvt(e,"onItemDoubleClick");
},onItemDoubleClick:function(e){
},_onItemContextMenu:function(e){
this._dispatchCalendarEvt(e,"onItemContextMenu");
},onItemContextMenu:function(e){
},_getStartEndRenderers:function(_a8){
var _a9=this.itemToRenderer[_a8.id];
if(_a9==null){
return;
}
if(_a9.length==1){
var _aa=_a9[0].renderer;
return [_aa,_aa];
}
var rd=this.renderData;
var _ab=false;
var _ac=false;
var res=[];
for(var i=0;i<_a9.length;i++){
var ir=_a9[i].renderer;
if(!_ab){
_ab=rd.dateModule.compare(ir.item.range[0],ir.item.startTime)==0;
res[0]=ir;
}
if(!_ac){
_ac=rd.dateModule.compare(ir.item.range[1],ir.item.endTime)==0;
res[1]=ir;
}
if(_ab&&_ac){
break;
}
}
return res;
},editable:true,moveEnabled:true,resizeEnabled:true,isItemEditable:function(_ad,_ae){
return this.editable&&(this.owner?this.owner.isItemEditable():true);
},isItemMoveEnabled:function(_af,_b0){
return this.isItemEditable(_af,_b0)&&this.moveEnabled&&(this.owner?this.owner.isItemMoveEnabled(_af,_b0):true);
},isItemResizeEnabled:function(_b1,_b2){
return this.isItemEditable(_b1,_b2)&&this.resizeEnabled&&(this.owner?this.owner.isItemResizeEnabled(_b1,_b2):true);
},_isEditing:false,isItemBeingEdited:function(_b3){
return this._isEditing&&this._edProps&&this._edProps.editedItem&&this._edProps.editedItem.id==_b3.id;
},_setEditingProperties:function(_b4){
this._edProps=_b4;
},_startItemEditing:function(_b5,_b6){
this._isEditing=true;
var p=this._edProps;
p.editedItem=_b5;
p.eventSource=_b6;
p.secItem=this._secondarySheet?this._findRenderItem(_b5.id,this._secondarySheet.renderData.items):null;
p.ownerItem=this.owner?this._findRenderItem(_b5.id,this.items):null;
if(!p.liveLayout){
p.editSaveStartTime=_b5.startTime;
p.editSaveEndTime=_b5.endTime;
p.editItemToRenderer=this.itemToRenderer;
p.editItems=this.renderData.items;
p.editRendererList=this.rendererList;
this.renderData.items=[p.editedItem];
var id=p.editedItem.id;
this.itemToRenderer={};
this.rendererList=[];
var _b7=p.editItemToRenderer[id];
p.editRendererIndices=[];
_3.forEach(_b7,_2.hitch(this,function(ir,i){
if(this.itemToRenderer[id]==null){
this.itemToRenderer[id]=[ir];
}else{
this.itemToRenderer[id].push(ir);
}
this.rendererList.push(ir);
}));
p.editRendererList=_3.filter(p.editRendererList,function(ir){
return ir!=null&&ir.renderer.item.id!=id;
});
delete p.editItemToRenderer[id];
}
this._layoutRenderers(this.renderData);
this._onItemEditBegin({item:_b5,eventSource:_b6});
},_onItemEditBegin:function(e){
this._editStartTimeSave=this.newDate(e.item.startTime);
this._editEndTimeSave=this.newDate(e.item.endTime);
this._dispatchCalendarEvt(e,"onItemEditBegin");
},onItemEditBegin:function(e){
},_endItemEditing:function(_b8,_b9){
this._isEditing=false;
var p=this._edProps;
_3.forEach(p.handles,function(_ba){
_ba.remove();
});
if(!p.liveLayout){
this.renderData.items=p.editItems;
this.rendererList=p.editRendererList.concat(this.rendererList);
_2.mixin(this.itemToRenderer,p.editItemToRenderer);
}
var _bb=this.get("store");
this._onItemEditEnd(_2.mixin(this._createItemEditEvent(),{item:this.renderItemToItem(p.editedItem,_bb),eventSource:_b8,completed:!_b9}));
this._layoutRenderers(this.renderData);
this._edProps=null;
},_onItemEditEnd:function(e){
this._dispatchCalendarEvt(e,"onItemEditEnd");
if(!e.isDefaultPrevented()){
if(e.completed){
var _bc=this.get("store");
_bc.put(e.item,_bc);
}else{
e.item.startTime=this._editStartTimeSave;
e.item.endTime=this._editEndTimeSave;
}
}
},onItemEditEnd:function(e){
},_createItemEditEvent:function(){
var e={cancelable:true,bubbles:false,__defaultPrevent:false};
e.preventDefault=function(){
this.__defaultPrevented=true;
};
e.isDefaultPrevented=function(){
return this.__defaultPrevented;
};
return e;
},_startItemEditingGesture:function(_bd,_be,_bf,e){
var p=this._edProps;
if(!p||p.editedItem==null){
return;
}
this._editingGesture=true;
var _c0=p.editedItem;
p.editKind=_be;
this._onItemEditBeginGesture(this.__fixEvt(_2.mixin(this._createItemEditEvent(),{item:_c0,startTime:_c0.startTime,endTime:_c0.endTime,editKind:_be,rendererKind:p.rendererKind,triggerEvent:e,dates:_bd,eventSource:_bf})));
p.itemBeginDispatched=true;
},_onItemEditBeginGesture:function(e){
var p=this._edProps;
var _c1=p.editedItem;
var _c2=e.dates;
p.editingTimeFrom=[];
p.editingTimeFrom[0]=_c2[0];
p.editingItemRefTime=[];
p.editingItemRefTime[0]=this.newDate(p.editKind=="resizeEnd"?_c1.endTime:_c1.startTime);
if(p.editKind=="resizeBoth"){
p.editingTimeFrom[1]=_c2[1];
p.editingItemRefTime[1]=this.newDate(_c1.endTime);
}
var cal=this.renderData.dateModule;
p.inViewOnce=this._isItemInView(_c1);
if(p.rendererKind=="label"||this.roundToDay){
p._itemEditBeginSave=this.newDate(_c1.startTime);
p._itemEditEndSave=this.newDate(_c1.endTime);
}
p._initDuration=cal.difference(_c1.startTime,_c1.endTime,_c1.allDay?"day":"millisecond");
this._dispatchCalendarEvt(e,"onItemEditBeginGesture");
if(!e.isDefaultPrevented()){
if(e.eventSource=="mouse"){
var _c3=e.editKind=="move"?"move":this.resizeCursor;
p.editLayer=_b.create("div",{style:"position: absolute; left:0; right:0; bottom:0; top:0; z-index:30; tabIndex:-1; background-image:url('"+this._blankGif+"'); cursor: "+_c3,onresizestart:function(e){
return false;
},onselectstart:function(e){
return false;
}},this.domNode);
p.editLayer.focus();
}
}
},onItemEditBeginGesture:function(e){
},_waDojoxAddIssue:function(d,_c4,_c5){
var cal=this.renderData.dateModule;
if(this._calendar!="gregorian"&&_c5<0){
var gd=d.toGregorian();
gd=_c.add(gd,_c4,_c5);
return new this.renderData.dateClassObj(gd);
}else{
return cal.add(d,_c4,_c5);
}
},_computeItemEditingTimes:function(_c6,_c7,_c8,_c9,_ca){
var cal=this.renderData.dateModule;
var p=this._edProps;
var _cb=cal.difference(p.editingTimeFrom[0],_c9[0],"millisecond");
_c9[0]=this._waDojoxAddIssue(p.editingItemRefTime[0],"millisecond",_cb);
if(_c7=="resizeBoth"){
_cb=cal.difference(p.editingTimeFrom[1],_c9[1],"millisecond");
_c9[1]=this._waDojoxAddIssue(p.editingItemRefTime[1],"millisecond",_cb);
}
return _c9;
},_moveOrResizeItemGesture:function(_cc,_cd,e){
if(!this._isEditing||_cc[0]==null){
return;
}
var p=this._edProps;
var _ce=p.editedItem;
var rd=this.renderData;
var cal=rd.dateModule;
var _cf=p.editKind;
var _d0=[_cc[0]];
if(_cf=="resizeBoth"){
_d0[1]=_cc[1];
}
_d0=this._computeItemEditingTimes(_ce,p.editKind,p.rendererKind,_d0,_cd);
var _d1=_d0[0];
var _d2=false;
var _d3=_2.clone(_ce.startTime);
var _d4=_2.clone(_ce.endTime);
var _d5=p.eventSource=="keyboard"?false:this.allowStartEndSwap;
if(_cf=="move"){
if(cal.compare(_ce.startTime,_d1)!=0){
var _d6=cal.difference(_ce.startTime,_ce.endTime,"millisecond");
_ce.startTime=this.newDate(_d1);
_ce.endTime=cal.add(_ce.startTime,"millisecond",_d6);
_d2=true;
}
}else{
if(_cf=="resizeStart"){
if(cal.compare(_ce.startTime,_d1)!=0){
if(cal.compare(_ce.endTime,_d1)!=-1){
_ce.startTime=this.newDate(_d1);
}else{
if(_d5){
_ce.startTime=this.newDate(_ce.endTime);
_ce.endTime=this.newDate(_d1);
p.editKind=_cf="resizeEnd";
if(_cd=="touch"){
p.resizeEndTouchIndex=p.resizeStartTouchIndex;
p.resizeStartTouchIndex=-1;
}
}else{
_ce.startTime=this.newDate(_ce.endTime);
_ce.startTime.setHours(_d1.getHours());
_ce.startTime.setMinutes(_d1.getMinutes());
_ce.startTime.setSeconds(_d1.getSeconds());
}
}
_d2=true;
}
}else{
if(_cf=="resizeEnd"){
if(cal.compare(_ce.endTime,_d1)!=0){
if(cal.compare(_ce.startTime,_d1)!=1){
_ce.endTime=this.newDate(_d1);
}else{
if(_d5){
_ce.endTime=this.newDate(_ce.startTime);
_ce.startTime=this.newDate(_d1);
p.editKind=_cf="resizeStart";
if(_cd=="touch"){
p.resizeStartTouchIndex=p.resizeEndTouchIndex;
p.resizeEndTouchIndex=-1;
}
}else{
_ce.endTime=this.newDate(_ce.startTime);
_ce.endTime.setHours(_d1.getHours());
_ce.endTime.setMinutes(_d1.getMinutes());
_ce.endTime.setSeconds(_d1.getSeconds());
}
}
_d2=true;
}
}else{
if(_cf=="resizeBoth"){
_d2=true;
var _d7=this.newDate(_d1);
var end=this.newDate(_d0[1]);
if(cal.compare(_d7,end)!=-1){
if(_d5){
var t=_d7;
_d7=end;
end=t;
}else{
_d2=false;
}
}
if(_d2){
_ce.startTime=_d7;
_ce.endTime=end;
}
}else{
return false;
}
}
}
}
if(!_d2){
return false;
}
var evt=_2.mixin(this._createItemEditEvent(),{item:_ce,startTime:_ce.startTime,endTime:_ce.endTime,editKind:_cf,rendererKind:p.rendererKind,triggerEvent:e,eventSource:_cd});
if(_cf=="move"){
this._onItemEditMoveGesture(evt);
}else{
this._onItemEditResizeGesture(evt);
}
if(cal.compare(_ce.startTime,_ce.endTime)==1){
var tmp=_ce.startTime;
_ce.startTime=_ce.startTime;
_ce.endTime=tmp;
}
_d2=cal.compare(_d3,_ce.startTime)!=0||cal.compare(_d4,_ce.endTime)!=0;
if(!_d2){
return false;
}
this._layoutRenderers(this.renderData);
if(p.liveLayout&&p.secItem!=null){
p.secItem.startTime=_ce.startTime;
p.secItem.endTime=_ce.endTime;
this._secondarySheet._layoutRenderers(this._secondarySheet.renderData);
}else{
if(p.ownerItem!=null&&this.owner.liveLayout){
p.ownerItem.startTime=_ce.startTime;
p.ownerItem.endTime=_ce.endTime;
this.owner._layoutRenderers(this.owner.renderData);
}
}
return true;
},_findRenderItem:function(id,_d8){
_d8=_d8||this.renderData.items;
for(var i=0;i<_d8.length;i++){
if(_d8[i].id==id){
return _d8[i];
}
}
return null;
},_onItemEditMoveGesture:function(e){
this._dispatchCalendarEvt(e,"onItemEditMoveGesture");
if(!e.isDefaultPrevented()){
var p=e.source._edProps;
var rd=this.renderData;
var cal=rd.dateModule;
var _d9,_da;
if(p.rendererKind=="label"||(this.roundToDay&&!e.item.allDay)){
_d9=this.floorToDay(e.item.startTime,false,rd);
_d9.setHours(p._itemEditBeginSave.getHours());
_d9.setMinutes(p._itemEditBeginSave.getMinutes());
_da=cal.add(_d9,"millisecond",p._initDuration);
}else{
if(e.item.allDay){
_d9=this.floorToDay(e.item.startTime,true);
_da=cal.add(_d9,"day",p._initDuration);
}else{
_d9=this.floorDate(e.item.startTime,this.snapUnit,this.snapSteps);
_da=cal.add(_d9,"millisecond",p._initDuration);
}
}
e.item.startTime=_d9;
e.item.endTime=_da;
if(!p.inViewOnce){
p.inViewOnce=this._isItemInView(e.item);
}
if(p.inViewOnce&&this.stayInView){
this._ensureItemInView(e.item);
}
}
},_DAY_IN_MILLISECONDS:24*60*60*1000,onItemEditMoveGesture:function(e){
},_onItemEditResizeGesture:function(e){
this._dispatchCalendarEvt(e,"onItemEditResizeGesture");
if(!e.isDefaultPrevented()){
var p=e.source._edProps;
var rd=this.renderData;
var cal=rd.dateModule;
var _db=e.item.startTime;
var _dc=e.item.endTime;
if(e.editKind=="resizeStart"){
if(e.item.allDay){
_db=this.floorToDay(e.item.startTime,false,this.renderData);
}else{
if(this.roundToDay){
_db=this.floorToDay(e.item.startTime,false,rd);
_db.setHours(p._itemEditBeginSave.getHours());
_db.setMinutes(p._itemEditBeginSave.getMinutes());
}else{
_db=this.floorDate(e.item.startTime,this.snapUnit,this.snapSteps);
}
}
}else{
if(e.editKind=="resizeEnd"){
if(e.item.allDay){
if(!this.isStartOfDay(e.item.endTime)){
_dc=this.floorToDay(e.item.endTime,false,this.renderData);
_dc=cal.add(_dc,"day",1);
}
}else{
if(this.roundToDay){
_dc=this.floorToDay(e.item.endTime,false,rd);
_dc.setHours(p._itemEditEndSave.getHours());
_dc.setMinutes(p._itemEditEndSave.getMinutes());
}else{
_dc=this.floorDate(e.item.endTime,this.snapUnit,this.snapSteps);
if(e.eventSource=="mouse"){
_dc=cal.add(_dc,this.snapUnit,this.snapSteps);
}
}
}
}else{
_db=this.floorDate(e.item.startTime,this.snapUnit,this.snapSteps);
_dc=this.floorDate(e.item.endTime,this.snapUnit,this.snapSteps);
_dc=cal.add(_dc,this.snapUnit,this.snapSteps);
}
}
e.item.startTime=_db;
e.item.endTime=_dc;
var _dd=e.item.allDay||p._initDuration>=this._DAY_IN_MILLISECONDS&&!this.allowResizeLessThan24H;
this.ensureMinimalDuration(this.renderData,e.item,_dd?"day":this.minDurationUnit,_dd?1:this.minDurationSteps,e.editKind);
if(!p.inViewOnce){
p.inViewOnce=this._isItemInView(e.item);
}
if(p.inViewOnce&&this.stayInView){
this._ensureItemInView(e.item);
}
}
},onItemEditResizeGesture:function(e){
},_endItemEditingGesture:function(_de,e){
if(!this._isEditing){
return;
}
this._editingGesture=false;
var p=this._edProps;
var _df=p.editedItem;
p.itemBeginDispatched=false;
this._onItemEditEndGesture(_2.mixin(this._createItemEditEvent(),{item:_df,startTime:_df.startTime,endTime:_df.endTime,editKind:p.editKind,rendererKind:p.rendererKind,triggerEvent:e,eventSource:_de}));
},_onItemEditEndGesture:function(e){
var p=this._edProps;
delete p._itemEditBeginSave;
delete p._itemEditEndSave;
this._dispatchCalendarEvt(e,"onItemEditEndGesture");
if(!e.isDefaultPrevented()){
if(p.editLayer){
if(_7("ie")){
p.editLayer.style.cursor="default";
}
setTimeout(_2.hitch(this,function(){
if(this.domNode){
this.domNode.focus();
p.editLayer.parentNode.removeChild(p.editLayer);
p.editLayer=null;
}
}),10);
}
}
},onItemEditEndGesture:function(e){
},ensureMinimalDuration:function(_e0,_e1,_e2,_e3,_e4){
var _e5;
var cal=_e0.dateModule;
if(_e4=="resizeStart"){
_e5=cal.add(_e1.endTime,_e2,-_e3);
if(cal.compare(_e1.startTime,_e5)==1){
_e1.startTime=_e5;
}
}else{
_e5=cal.add(_e1.startTime,_e2,_e3);
if(cal.compare(_e1.endTime,_e5)==-1){
_e1.endTime=_e5;
}
}
},doubleTapDelay:300,snapUnit:"minute",snapSteps:15,minDurationUnit:"hour",minDurationSteps:1,liveLayout:false,stayInView:true,allowStartEndSwap:true,allowResizeLessThan24H:false});
});
