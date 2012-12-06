//>>built
define("dojox/mobile/View",["dojo/_base/array","dojo/_base/config","dojo/_base/connect","dojo/_base/declare","dojo/_base/lang","dojo/_base/sniff","dojo/_base/window","dojo/_base/Deferred","dojo/dom","dojo/dom-class","dojo/dom-construct","dojo/dom-geometry","dojo/dom-style","dijit/registry","dijit/_Contained","dijit/_Container","dijit/_WidgetBase","./ViewController","./common","./transition","./viewRegistry"],function(_1,_2,_3,_4,_5,_6,_7,_8,_9,_a,_b,_c,_d,_e,_f,_10,_11,_12,_13,_14,_15){
var dm=_5.getObject("dojox.mobile",true);
return _4("dojox.mobile.View",[_11,_10,_f],{selected:false,keepScrollPos:true,tag:"div",baseClass:"mblView",constructor:function(_16,_17){
if(_17){
_9.byId(_17).style.visibility="hidden";
}
},destroy:function(){
_15.remove(this.id);
this.inherited(arguments);
},buildRendering:function(){
this.domNode=this.containerNode=this.srcNodeRef||_b.create(this.tag);
this._animEndHandle=this.connect(this.domNode,"webkitAnimationEnd","onAnimationEnd");
this._animStartHandle=this.connect(this.domNode,"webkitAnimationStart","onAnimationStart");
if(!_2["mblCSS3Transition"]){
this._transEndHandle=this.connect(this.domNode,"webkitTransitionEnd","onAnimationEnd");
}
if(_6("mblAndroid3Workaround")){
_d.set(this.domNode,"webkitTransformStyle","preserve-3d");
}
_15.add(this);
this.inherited(arguments);
},startup:function(){
if(this._started){
return;
}
if(this._visible===undefined){
var _18=this.getSiblingViews();
var ids=location.hash&&location.hash.substring(1).split(/,/);
var _19,_1a,_1b;
_1.forEach(_18,function(v,i){
if(_1.indexOf(ids,v.id)!==-1){
_19=v;
}
if(i==0){
_1b=v;
}
if(v.selected){
_1a=v;
}
v._visible=false;
},this);
(_19||_1a||_1b)._visible=true;
}
if(this._visible){
this.show(true,true);
this.onStartView();
_3.publish("/dojox/mobile/startView",[this]);
}
if(this.domNode.style.visibility!="visible"){
this.domNode.style.visibility="visible";
}
this.inherited(arguments);
var _1c=this.getParent();
if(!_1c||!_1c.resize){
this.resize();
}
if(!this._visible){
this.hide();
}
},resize:function(){
_1.forEach(this.getChildren(),function(_1d){
if(_1d.resize){
_1d.resize();
}
});
},onStartView:function(){
},onBeforeTransitionIn:function(_1e,dir,_1f,_20,_21){
},onAfterTransitionIn:function(_22,dir,_23,_24,_25){
},onBeforeTransitionOut:function(_26,dir,_27,_28,_29){
},onAfterTransitionOut:function(_2a,dir,_2b,_2c,_2d){
},_clearClasses:function(_2e){
if(!_2e){
return;
}
var _2f=[];
_1.forEach(_5.trim(_2e.className||"").split(/\s+/),function(c){
if(c.match(/^mbl\w*View$/)||c.indexOf("mbl")===-1){
_2f.push(c);
}
},this);
_2e.className=_2f.join(" ");
},_fixViewState:function(_30){
var _31=this.domNode.parentNode.childNodes;
for(var i=0;i<_31.length;i++){
var n=_31[i];
if(n.nodeType===1&&_a.contains(n,"mblView")){
this._clearClasses(n);
}
}
this._clearClasses(_30);
},convertToId:function(_32){
if(typeof (_32)=="string"){
return _32.replace(/^#?([^&?]+).*/,"$1");
}
return _32;
},_isBookmarkable:function(_33){
return _33.moveTo&&(_2["mblForceBookmarkable"]||_33.moveTo.charAt(0)==="#")&&!_33.hashchange;
},performTransition:function(_34,_35,_36,_37,_38){
var _39,_3a;
if(_34&&typeof (_34)==="object"){
_39=_34;
_3a=_35;
}else{
_39={moveTo:_34,transitionDir:_35,transition:_36,context:_37,method:_38};
_3a=[];
for(var i=5;i<arguments.length;i++){
_3a.push(arguments[i]);
}
}
this._detail=_39;
this._optArgs=_3a;
this._arguments=[_39.moveTo,_39.transitionDir,_39.transition,_39.context,_39.method];
if(_39.moveTo==="#"){
return;
}
var _3b;
if(_39.moveTo){
_3b=this.convertToId(_39.moveTo);
}else{
if(!this._dummyNode){
this._dummyNode=_7.doc.createElement("div");
_7.body().appendChild(this._dummyNode);
}
_3b=this._dummyNode;
}
if(this.addTransitionInfo&&typeof (_39.moveTo)=="string"&&this._isBookmarkable(_39)){
this.addTransitionInfo(this.id,_39.moveTo,{transitionDir:_39.transitionDir,transition:_39.transition});
}
var _3c=this.domNode;
var _3d=_3c.offsetTop;
_3b=this.toNode=_9.byId(_3b);
if(!_3b){
return;
}
_3b.style.visibility="hidden";
_3b.style.display="";
this._fixViewState(_3b);
var _3e=_e.byNode(_3b);
if(_3e){
if(_2["mblAlwaysResizeOnTransition"]||!_3e._resized){
_13.resizeAll(null,_3e);
_3e._resized=true;
}
if(_39.transition&&_39.transition!="none"){
_3e.containerNode.style.paddingTop=_3d+"px";
}
_3e.load&&_3e.load();
_3e.movedFrom=_3c.id;
}
if(_6("mblAndroidWorkaround")&&!_2["mblCSS3Transition"]&&_39.transition&&_39.transition!="none"){
_d.set(_3b,"webkitTransformStyle","preserve-3d");
_d.set(_3c,"webkitTransformStyle","preserve-3d");
_a.add(_3b,"mblAndroidWorkaround");
}
this.onBeforeTransitionOut.apply(this,this._arguments);
_3.publish("/dojox/mobile/beforeTransitionOut",[this].concat(_5._toArray(this._arguments)));
if(_3e){
if(this.keepScrollPos&&!this.getParent()){
var _3f=_7.body().scrollTop||_7.doc.documentElement.scrollTop||_7.global.pageYOffset||0;
_3c._scrollTop=_3f;
var _40=(_39.transitionDir==1)?0:(_3b._scrollTop||0);
_3b.style.top="0px";
if(_3f>1||_40!==0){
_3c.style.top=_40-_3f+"px";
if(_2["mblHideAddressBar"]!==false){
setTimeout(function(){
_7.global.scrollTo(0,(_40||1));
},0);
}
}
}else{
_3b.style.top="0px";
}
_3e.onBeforeTransitionIn.apply(_3e,this._arguments);
_3.publish("/dojox/mobile/beforeTransitionIn",[_3e].concat(_5._toArray(this._arguments)));
}
_3b.style.display="none";
_3b.style.visibility="visible";
_13.fromView=this;
_13.toView=_3e;
this._doTransition(_3c,_3b,_39.transition,_39.transitionDir);
},_toCls:function(s){
return "mbl"+s.charAt(0).toUpperCase()+s.substring(1);
},_doTransition:function(_41,_42,_43,_44){
var rev=(_44==-1)?" mblReverse":"";
_42.style.display="";
if(!_43||_43=="none"){
this.domNode.style.display="none";
this.invokeCallback();
}else{
if(_2["mblCSS3Transition"]){
_8.when(_14,_5.hitch(this,function(_45){
var _46=_d.get(_42,"position");
_d.set(_42,"position","absolute");
_8.when(_45(_41,_42,{transition:_43,reverse:(_44===-1)?true:false}),_5.hitch(this,function(){
_d.set(_42,"position",_46);
this.invokeCallback();
}));
}));
}else{
if(_43.indexOf("cube")!=-1){
if(_6("ipad")){
_d.set(_42.parentNode,{webkitPerspective:1600});
}else{
if(_6("iphone")){
_d.set(_42.parentNode,{webkitPerspective:800});
}
}
}
var s=this._toCls(_43);
if(_6("mblAndroidWorkaround")){
setTimeout(function(){
_a.add(_41,s+" mblOut"+rev);
_a.add(_42,s+" mblIn"+rev);
_a.remove(_42,"mblAndroidWorkaround");
setTimeout(function(){
_a.add(_41,"mblTransition");
_a.add(_42,"mblTransition");
},30);
},70);
}else{
_a.add(_41,s+" mblOut"+rev);
_a.add(_42,s+" mblIn"+rev);
setTimeout(function(){
_a.add(_41,"mblTransition");
_a.add(_42,"mblTransition");
},100);
}
var _47="50% 50%";
var _48="50% 50%";
var _49,_4a,_4b;
if(_43.indexOf("swirl")!=-1||_43.indexOf("zoom")!=-1){
if(this.keepScrollPos&&!this.getParent()){
_49=_7.body().scrollTop||_7.doc.documentElement.scrollTop||_7.global.pageYOffset||0;
}else{
_49=-_c.position(_41,true).y;
}
_4b=_7.global.innerHeight/2+_49;
_47="50% "+_4b+"px";
_48="50% "+_4b+"px";
}else{
if(_43.indexOf("scale")!=-1){
var _4c=_c.position(_41,true);
_4a=((this.clickedPosX!==undefined)?this.clickedPosX:_7.global.innerWidth/2)-_4c.x;
if(this.keepScrollPos&&!this.getParent()){
_49=_7.body().scrollTop||_7.doc.documentElement.scrollTop||_7.global.pageYOffset||0;
}else{
_49=-_4c.y;
}
_4b=((this.clickedPosY!==undefined)?this.clickedPosY:_7.global.innerHeight/2)+_49;
_47=_4a+"px "+_4b+"px";
_48=_4a+"px "+_4b+"px";
}
}
_d.set(_41,{webkitTransformOrigin:_47});
_d.set(_42,{webkitTransformOrigin:_48});
}
}
},onAnimationStart:function(e){
},onAnimationEnd:function(e){
var _4d=e.animationName||e.target.className;
if(_4d.indexOf("Out")===-1&&_4d.indexOf("In")===-1&&_4d.indexOf("Shrink")===-1){
return;
}
var _4e=false;
if(_a.contains(this.domNode,"mblOut")){
_4e=true;
this.domNode.style.display="none";
_a.remove(this.domNode,[this._toCls(this._detail.transition),"mblIn","mblOut","mblReverse"]);
}else{
this.containerNode.style.paddingTop="";
}
_d.set(this.domNode,{webkitTransformOrigin:""});
if(_4d.indexOf("Shrink")!==-1){
var li=e.target;
li.style.display="none";
_a.remove(li,"mblCloseContent");
var p=_15.getEnclosingScrollable(this.domNode);
p&&p.onTouchEnd();
}
if(_4e){
this.invokeCallback();
}
this._clearClasses(this.domNode);
this.clickedPosX=this.clickedPosY=undefined;
if(_4d.indexOf("Cube")!==-1&&_4d.indexOf("In")!==-1&&_6("iphone")){
this.domNode.parentNode.style.webkitPerspective="";
}
},invokeCallback:function(){
this.onAfterTransitionOut.apply(this,this._arguments);
_3.publish("/dojox/mobile/afterTransitionOut",[this].concat(this._arguments));
var _4f=_e.byNode(this.toNode);
if(_4f){
_4f.onAfterTransitionIn.apply(_4f,this._arguments);
_3.publish("/dojox/mobile/afterTransitionIn",[_4f].concat(this._arguments));
_4f.movedFrom=undefined;
if(this.setFragIds&&this._isBookmarkable(this._detail)){
this.setFragIds(_4f);
}
}
if(_6("mblAndroidWorkaround")){
setTimeout(_5.hitch(this,function(){
if(_4f){
_d.set(this.toNode,"webkitTransformStyle","");
}
_d.set(this.domNode,"webkitTransformStyle","");
}),0);
}
var c=this._detail.context,m=this._detail.method;
if(!c&&!m){
return;
}
if(!m){
m=c;
c=null;
}
c=c||_7.global;
if(typeof (m)=="string"){
c[m].apply(c,this._optArgs);
}else{
if(typeof (m)=="function"){
m.apply(c,this._optArgs);
}
}
},isVisible:function(_50){
var _51=function(_52){
return _d.get(_52,"display")!=="none";
};
if(_50){
for(var n=this.domNode;n.tagName!=="BODY";n=n.parentNode){
if(!_51(n)){
return false;
}
}
return true;
}else{
return _51(this.domNode);
}
},getShowingView:function(){
var _53=this.domNode.parentNode.childNodes;
for(var i=0;i<_53.length;i++){
var n=_53[i];
if(n.nodeType===1&&_a.contains(n,"mblView")&&n.style.display!=="none"){
return _e.byNode(n);
}
}
return null;
},getSiblingViews:function(){
if(!this.domNode.parentNode){
return [this];
}
return _1.map(_1.filter(this.domNode.parentNode.childNodes,function(n){
return n.nodeType===1&&_a.contains(n,"mblView");
}),function(n){
return _e.byNode(n);
});
},show:function(_54,_55){
var out=this.getShowingView();
if(!_54){
if(out){
out.onBeforeTransitionOut(out.id);
_3.publish("/dojox/mobile/beforeTransitionOut",[out,out.id]);
}
this.onBeforeTransitionIn(this.id);
_3.publish("/dojox/mobile/beforeTransitionIn",[this,this.id]);
}
if(_55){
this.domNode.style.display="";
}else{
_1.forEach(this.getSiblingViews(),function(v){
v.domNode.style.display=(v===this)?"":"none";
},this);
}
this.load&&this.load();
if(!_54){
if(out){
out.onAfterTransitionOut(out.id);
_3.publish("/dojox/mobile/afterTransitionOut",[out,out.id]);
}
this.onAfterTransitionIn(this.id);
_3.publish("/dojox/mobile/afterTransitionIn",[this,this.id]);
}
},hide:function(){
this.domNode.style.display="none";
}});
});
