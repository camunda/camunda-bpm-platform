//>>built
define("dojox/charting/Element",["dojo/_base/lang","dojo/_base/array","dojo/dom-construct","dojo/_base/declare","dojox/gfx","dojox/gfx/shape"],function(_1,_2,_3,_4,_5,_6){
return _4("dojox.charting.Element",null,{chart:null,group:null,htmlElements:null,dirty:true,constructor:function(_7){
this.chart=_7;
this.group=null;
this.htmlElements=[];
this.dirty=true;
this.trailingSymbol="...";
this._events=[];
},purgeGroup:function(){
this.destroyHtmlElements();
if(this.group){
this.group.removeShape();
var _8=this.group.children;
for(var i=0;i<_8.length;++i){
_6.dispose(_8[i]);
}
this.group.clear();
this.group=null;
}
this.dirty=true;
if(this._events.length){
_2.forEach(this._events,function(_9){
_9.shape.disconnect(_9.handle);
});
this._events=[];
}
return this;
},cleanGroup:function(_a){
this.destroyHtmlElements();
if(!_a){
_a=this.chart.surface;
}
if(this.group){
var _b=this.group.children;
for(var i=0;i<_b.length;++i){
_6.dispose(_b[i]);
}
this.group.clear();
}else{
this.group=_a.createGroup();
}
this.dirty=true;
return this;
},destroyHtmlElements:function(){
if(this.htmlElements.length){
_2.forEach(this.htmlElements,_3.destroy);
this.htmlElements=[];
}
},destroy:function(){
this.purgeGroup();
},getTextWidth:function(s,_c){
return _5._base._getTextBox(s,{font:_c}).w||0;
},getTextWithLimitLength:function(s,_d,_e,_f){
if(!s||s.length<=0){
return {text:"",truncated:_f||false};
}
if(!_e||_e<=0){
return {text:s,truncated:_f||false};
}
var _10=2,_11=0.618,_12=s.substring(0,1)+this.trailingSymbol,_13=this.getTextWidth(_12,_d);
if(_e<=_13){
return {text:_12,truncated:true};
}
var _14=this.getTextWidth(s,_d);
if(_14<=_e){
return {text:s,truncated:_f||false};
}else{
var _15=0,end=s.length;
while(_15<end){
if(end-_15<=_10){
while(this.getTextWidth(s.substring(0,_15)+this.trailingSymbol,_d)>_e){
_15-=1;
}
return {text:(s.substring(0,_15)+this.trailingSymbol),truncated:true};
}
var _16=_15+Math.round((end-_15)*_11),_17=this.getTextWidth(s.substring(0,_16),_d);
if(_17<_e){
_15=_16;
end=end;
}else{
_15=_15;
end=_16;
}
}
}
},getTextWithLimitCharCount:function(s,_18,_19,_1a){
if(!s||s.length<=0){
return {text:"",truncated:_1a||false};
}
if(!_19||_19<=0||s.length<=_19){
return {text:s,truncated:_1a||false};
}
return {text:s.substring(0,_19)+this.trailingSymbol,truncated:true};
},_plotFill:function(_1b,dim,_1c){
if(!_1b||!_1b.type||!_1b.space){
return _1b;
}
var _1d=_1b.space,_1e;
switch(_1b.type){
case "linear":
if(_1d==="plot"||_1d==="shapeX"||_1d==="shapeY"){
_1b=_5.makeParameters(_5.defaultLinearGradient,_1b);
_1b.space=_1d;
if(_1d==="plot"||_1d==="shapeX"){
_1e=dim.height-_1c.t-_1c.b;
_1b.y1=_1c.t+_1e*_1b.y1/100;
_1b.y2=_1c.t+_1e*_1b.y2/100;
}
if(_1d==="plot"||_1d==="shapeY"){
_1e=dim.width-_1c.l-_1c.r;
_1b.x1=_1c.l+_1e*_1b.x1/100;
_1b.x2=_1c.l+_1e*_1b.x2/100;
}
}
break;
case "radial":
if(_1d==="plot"){
_1b=_5.makeParameters(_5.defaultRadialGradient,_1b);
_1b.space=_1d;
var _1f=dim.width-_1c.l-_1c.r,_20=dim.height-_1c.t-_1c.b;
_1b.cx=_1c.l+_1f*_1b.cx/100;
_1b.cy=_1c.t+_20*_1b.cy/100;
_1b.r=_1b.r*Math.sqrt(_1f*_1f+_20*_20)/200;
}
break;
case "pattern":
if(_1d==="plot"||_1d==="shapeX"||_1d==="shapeY"){
_1b=_5.makeParameters(_5.defaultPattern,_1b);
_1b.space=_1d;
if(_1d==="plot"||_1d==="shapeX"){
_1e=dim.height-_1c.t-_1c.b;
_1b.y=_1c.t+_1e*_1b.y/100;
_1b.height=_1e*_1b.height/100;
}
if(_1d==="plot"||_1d==="shapeY"){
_1e=dim.width-_1c.l-_1c.r;
_1b.x=_1c.l+_1e*_1b.x/100;
_1b.width=_1e*_1b.width/100;
}
}
break;
}
return _1b;
},_shapeFill:function(_21,_22){
if(!_21||!_21.space){
return _21;
}
var _23=_21.space,_24;
switch(_21.type){
case "linear":
if(_23==="shape"||_23==="shapeX"||_23==="shapeY"){
_21=_5.makeParameters(_5.defaultLinearGradient,_21);
_21.space=_23;
if(_23==="shape"||_23==="shapeX"){
_24=_22.width;
_21.x1=_22.x+_24*_21.x1/100;
_21.x2=_22.x+_24*_21.x2/100;
}
if(_23==="shape"||_23==="shapeY"){
_24=_22.height;
_21.y1=_22.y+_24*_21.y1/100;
_21.y2=_22.y+_24*_21.y2/100;
}
}
break;
case "radial":
if(_23==="shape"){
_21=_5.makeParameters(_5.defaultRadialGradient,_21);
_21.space=_23;
_21.cx=_22.x+_22.width/2;
_21.cy=_22.y+_22.height/2;
_21.r=_21.r*_22.width/200;
}
break;
case "pattern":
if(_23==="shape"||_23==="shapeX"||_23==="shapeY"){
_21=_5.makeParameters(_5.defaultPattern,_21);
_21.space=_23;
if(_23==="shape"||_23==="shapeX"){
_24=_22.width;
_21.x=_22.x+_24*_21.x/100;
_21.width=_24*_21.width/100;
}
if(_23==="shape"||_23==="shapeY"){
_24=_22.height;
_21.y=_22.y+_24*_21.y/100;
_21.height=_24*_21.height/100;
}
}
break;
}
return _21;
},_pseudoRadialFill:function(_25,_26,_27,_28,end){
if(!_25||_25.type!=="radial"||_25.space!=="shape"){
return _25;
}
var _29=_25.space;
_25=_5.makeParameters(_5.defaultRadialGradient,_25);
_25.space=_29;
if(arguments.length<4){
_25.cx=_26.x;
_25.cy=_26.y;
_25.r=_25.r*_27/100;
return _25;
}
var _2a=arguments.length<5?_28:(end+_28)/2;
return {type:"linear",x1:_26.x,y1:_26.y,x2:_26.x+_25.r*_27*Math.cos(_2a)/100,y2:_26.y+_25.r*_27*Math.sin(_2a)/100,colors:_25.colors};
return _25;
}});
});
