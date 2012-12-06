//>>built
define("dojox/calendar/time",["dojo/_base/lang","dojo/date","dojo/cldr/supplemental"],function(_1,_2,_3){
var _4={};
_4.newDate=function(_5,_6){
_6=_6||Date;
if(typeof (_5)=="number"){
return new _6(_4);
}else{
if(_5.getTime){
return new _6(_5.getTime());
}else{
return new _6(_5);
}
}
};
_4.floorToDay=function(d,_7,_8){
_8=_8||Date;
if(!_7){
d=_4.newDate(d,_8);
}
d.setHours(0,0,0,0);
return d;
};
_4.floorToMonth=function(d,_9,_a){
_a=_a||Date;
if(!_9){
d=_4.newDate(d,_a);
}
d.setDate(1);
d.setHours(0,0,0,0);
return d;
};
_4.floorToWeek=function(d,_b,_c,_d,_e){
_b=_b||Date;
_c=_c||_2;
var fd=_d==undefined||_d<0?_3.getFirstDayOfWeek(_e):_d;
var _f=d.getDay();
if(_f==fd){
return d;
}
return _4.floorToDay(_c.add(d,"day",_f>fd?-_f+fd:fd-_f),true,_b);
};
_4.floor=function(_10,_11,_12,_13,_14){
var d=_4.floorToDay(_10,_13,_14);
switch(_11){
case "week":
return _4.floorToWeek(d,firstDayOfWeek,dateModule,locale);
case "minute":
d.setHours(_10.getHours());
d.setMinutes(Math.floor(_10.getMinutes()/_12)*_12);
break;
case "hour":
d.setHours(Math.floor(_10.getHours()/_12)*_12);
break;
}
return d;
};
_4.isStartOfDay=function(d,_15,_16){
_16=_16||_2;
return _16.compare(this.floorToDay(d,false,_15),d)==0;
};
_4.isToday=function(d,_17){
_17=_17||Date;
var _18=new _17();
return d.getFullYear()==_18.getFullYear()&&d.getMonth()==_18.getMonth()&&d.getDate()==_18.getDate();
};
return _4;
});
