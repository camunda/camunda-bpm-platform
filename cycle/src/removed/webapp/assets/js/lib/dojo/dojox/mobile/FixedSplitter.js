//>>built
define("dojox/mobile/FixedSplitter",["dojo/_base/array","dojo/_base/declare","dojo/_base/window","dojo/dom-class","dojo/dom-geometry","dijit/_Contained","dijit/_Container","dijit/_WidgetBase"],function(_1,_2,_3,_4,_5,_6,_7,_8){
return _2("dojox.mobile.FixedSplitter",[_8,_7,_6],{orientation:"H",variablePane:-1,screenSizeAware:false,screenSizeAwareClass:"dojox/mobile/ScreenSizeAware",baseClass:"mblFixedSplitter",startup:function(){
if(this._started){
return;
}
_4.add(this.domNode,this.baseClass+this.orientation);
var _9=this.getParent(),f;
if(!_9||!_9.resize){
var _a=this;
f=function(){
setTimeout(function(){
_a.resize();
},0);
};
}
if(this.screenSizeAware){
require([this.screenSizeAwareClass],function(_b){
_b.getInstance();
f&&f();
});
}else{
f&&f();
}
this.inherited(arguments);
},resize:function(){
var wh=this.orientation==="H"?"w":"h",tl=this.orientation==="H"?"l":"t",_c={},_d={},i,c,h,a=[],_e=0,_f=0,_10=_1.filter(this.domNode.childNodes,function(_11){
return _11.nodeType==1;
}),idx=this.variablePane==-1?_10.length-1:this.variablePane;
for(i=0;i<_10.length;i++){
if(i!=idx){
a[i]=_5.getMarginBox(_10[i])[wh];
_f+=a[i];
}
}
if(this.orientation=="V"){
if(this.domNode.parentNode.tagName=="BODY"){
if(_1.filter(_3.body().childNodes,function(_12){
return _12.nodeType==1;
}).length==1){
h=(_3.global.innerHeight||_3.doc.documentElement.clientHeight);
}
}
}
var l=(h||_5.getMarginBox(this.domNode)[wh])-_f;
_d[wh]=a[idx]=l;
c=_10[idx];
_5.setMarginBox(c,_d);
c.style[this.orientation==="H"?"height":"width"]="";
for(i=0;i<_10.length;i++){
c=_10[i];
_c[tl]=_e;
_5.setMarginBox(c,_c);
c.style[this.orientation==="H"?"top":"left"]="";
_e+=a[i];
}
_1.forEach(this.getChildren(),function(_13){
if(_13.resize){
_13.resize();
}
});
},_setOrientationAttr:function(_14){
var s=this.baseClass;
_4.replace(this.domNode,s+_14,s+this.orientation);
this.orientation=_14;
if(this._started){
this.resize();
}
}});
});
