function Field(){
	this.hmItem ={};	
	this.exists=function(strFieldId) {
		if(this.hmItem.hasOwnProperty(strFieldId)){
			return true;
		}
		return false;
	};	
	this.getType=function (strFieldId) {
		if(this.exists(strFieldId)){
			return this.hmItem[strFieldId];			
		}
		return null;
	};	
	this.setType=function(strFieldId,strType) {
		var rules="^[a-zA-Z][0-9A-Za-z\\-_]{0,15}$"
		var exp = new RegExp(rules);
		var bFlag=exp.test(strFieldId);
		if(!bFlag){
			console.warn("Invalid FieldId " + strFieldId+",strFieldId rule is "+rules);
			return;
		}
		var fieldTypes=["string","byte","short","int","integer","long","float","double","date","datetime","time","boolean","cdo"];	
		var objType=$.type(strType);
		if(objType!="string"){
			console.error("setType occur error, Invalid FieldId " + strFieldId+",value  must  be one of them "+JSON.stringify(fieldTypes));
			return;
		}				
		for(var i=0;i<fieldTypes.length;i++){
			if(strType.toLowerCase()==fieldTypes[i]){
				this.hmItem[strFieldId]=fieldTypes[i];
				return;
			}
		}
		console.error("setType occur error, Invalid FieldId " + strFieldId+",value  must  be one of them "+JSON.stringify(fieldTypes));
	};			
}
var CDOUtil={
	//yyyy-MM-dd 或 yyyy-MM-dd hh:mm:ss
	dateFormat:function(date,fmt){
		  var o = {   
			"M+" : date.getMonth()+1,                 //月份   
			"d+" : date.getDate(),                    //日   
			"h+" : date.getHours(),                   //小时   
			"m+" : date.getMinutes(),                 //分   
			"s+" : date.getSeconds(),                 //秒   
			"q+" : Math.floor((date.getMonth()+3)/3), //季度   
			"S"  : date.getMilliseconds()             //毫秒   
		  };   
		  if(/(y+)/.test(fmt))   
			fmt=fmt.replace(RegExp.$1,(date.getFullYear()+"").substr(4 - RegExp.$1.length));   
		  for(var k in o)   
			if(new RegExp("("+ k +")").test(fmt))   
				fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));   
		  return fmt;  
	},
	CDOArray2String:function(cdosValue){
		var objType=$.type(cdosValue);
		if(objType!="array"){
			throw "Invalid FieldId " + strFieldId+",value is not CDOArray,value="+cdosValue;
		}	
		var strJSON="[";
		for(var i=0;i<cdosValue.length;i++){
			if(cdosValue[i] instanceof CDO){
				if(i>0){
					strJSON=strJSON+",";
				}
				strJSON=strJSON+cdosValue[i].toString();
				continue;
			}
			throw "Invalid FieldId " + strFieldId+",value is not CDOArray,value="+JSON.stringify(cdosValue);
		}
		strJSON=strJSON+"]"
	   return strJSON;		
	},
	str2CDO:function(_strJSON,_clsField,_defaultType){
		 var JSONObject=JSON.parse(_strJSON);	
		 return CDOUtil.json2CDO(JSONObject,_clsField,_defaultType);
	},
	json2CDO:function(JSONObject,_clsField,_defaultType){
		 if(JSONObject==null)
			 return null;    
		 if(_clsField==null || _clsField==undefined)
			 _clsField=new Field();
		 if(_defaultType==null || _defaultType==undefined)
			 _defaultType="string";	
		 if(!(_clsField instanceof Field)){
			 console.error("json2CDO occur error,param[_clsField] is not Field class");
			 return null;
		 }
		 var cdo=new  CDO();
		 CDOUtil.parse(cdo,JSONObject,_clsField,_defaultType)	
		 return cdo;
	},
	parse:function(cdo,JSONObject,clsField,defaultType){
			for(var key in JSONObject){
				var value=JSONObject[key];
				var objType=$.type(value);			
				if(objType=="object"){
						var subCDO=new CDO();
						CDOUtil.parse(subCDO,value,clsField,defaultType);
						cdo.setCDOValue(key,subCDO);
				}else if(objType=="array"){				
					CDOUtil.parseArray(cdo,clsField,defaultType,key,value);
				}else{				
					var fieldType=defaultType.toLowerCase();
					if(clsField.exists(key)){
						fieldType=clsField.getType(key).toLowerCase();
					}			
				  CDOUtil.setFieldValue(cdo,fieldType,key,value);
				}			
			}			
	},
  parseArray:function(cdo,clsField,defaultType,_key,_array){
		 var fieldType=defaultType.toLowerCase();
		 if(clsField.exists(_key)){
			 fieldType=clsField.getType(_key).toLowerCase();
		  }			
		if(_array.length==0){
			CDOUtil.setFieldValue(cdo,fieldType,_key,_array);
		}else{
			var value=_array[0];			
			var objType=$.type(value);
			if(objType=="object"){
				var subArr=new Array();
				for(var i=0;i<_array.length;i++){
					var subCDO=new CDO();					
					CDOUtil.parse(subCDO,_array[i],clsField,defaultType)
					subArr.push(subCDO);
				}								
				cdo.setCDOArrayValue(_key,subArr);
			}else{
				CDOUtil.setFieldValue(cdo,fieldType,_key,_array);
			}
		}
	},
	setFieldValue:function(cdo,fieldType,key,value){
	switch (fieldType)
		{		
		 case "byte":
			var objType=$.type(value);
			if(objType=="array"){
				cdo.setByteArrayValue(key,value);
			}else{
				cdo.setByteValue(key,value);
			}			
			break;
		 case "short":
		 	var objType=$.type(value);
			if(objType=="array"){
				cdo.setShortArrayValue(key,value);
			}else{
				cdo.setShortValue(key,value);
			}
			break;
		 case "int":
		 case "integer":
			var objType=$.type(value);
			if(objType=="array"){
				cdo.setIntegerArrayValue(key,value);
			}else{		 
				cdo.setIntegerValue(key,value);
			}
			break;
		 case "long":
			var objType=$.type(value);
			if(objType=="array"){
				cdo.setLongArrayValue(key,value);
			}else{		 
				cdo.setLongValue(key,value);
			}
			break;
		 case "float":
			var objType=$.type(value);
			if(objType=="array"){
				cdo.setFloatArrayValue(key,value);
			}else{		 
				cdo.setFloatValue(key,value);
			}
			break;
		 case "double":
			var objType=$.type(value);
			if(objType=="array"){
				cdo.setDoubleArrayValue(key,value);
			}else{		 
				cdo.setDoubleValue(key,value);
			}
		   break;
		 case "string":
			var objType=$.type(value);
			if(objType=="array"){
				cdo.setStringArrayValue(key,value);
			}else{		 
				cdo.setStringValue(key,value);
			}
			break;
		 case "date":
			var objType=$.type(value);
			if(objType=="array"){
				cdo.setDateArrayValue(key,value);
			}else{		 
				cdo.setDateValue(key,value);
			}
			break;
		 case "datetime":
			var objType=$.type(value);
			if(objType=="array"){
				cdo.setDateTimeArrayValue(key,value);
			}else{		 
				cdo.setDateTimeValue(key,value);
			}
			break;
		  case "time":
			var objType=$.type(value);
			if(objType=="array"){
				cdo.setTimeArrayValue(key,value);
			}else{		  
				cdo.setTimeValue(key,value);
			}
			break;
		  case "boolean":
			var objType=$.type(value);
			if(objType=="array"){
				cdo.setBooleanArrayValue(key,value);
			}else{		  
				cdo.setBooleanValue(key,value);
			}
			break;				
		  case "cdo":
			var objType=$.type(value);
			if(objType=="array"){
				cdo.setCDOArrayValue(key,value);
			}else{		  
				cdo.setCDOValue(key,value);
			}
			break;				
		}				
	}	
}