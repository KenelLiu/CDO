/**
 * www.cdoforum.com 2007版权所有
 *
 * $Header: /CVSData/Frank/CVSROOT/CDOForum/CDOLib/Source/com/cdoframework/cdolib/data/cdo/StringField.java,v 1.4 2008/03/12 10:30:56 Frank Exp $
 *
 * $Log: StringField.java,v $
 * Revision 1.4  2008/03/12 10:30:56  Frank
 * *** empty log message ***
 *
 * Revision 1.3  2008/03/10 14:54:16  Frank
 * *** empty log message ***
 *
 * Revision 1.2  2008/03/08 12:10:54  Frank
 * *** empty log message ***
 *
 * Revision 1.1  2008/03/07 11:20:19  Frank
 * *** empty log message ***
 *
 * Revision 1.4  2007/11/03 02:25:41  Frank
 * *** empty log message ***
 *
 * Revision 1.3  2007/10/11 13:55:05  Frank
 * *** empty log message ***
 *
 * Revision 1.2  2007/10/11 13:41:24  Frank
 * *** empty log message ***
 *
 * Revision 1.1  2007/10/11 01:10:57  Frank
 * *** empty log message ***
 *
 *
 */

package com.cdoframework.cdolib.data.cdo;

import com.cdoframework.cdolib.base.ObjectExt;
import com.cdoframework.cdolib.base.Utility;
import com.cdoframework.cdolib.util.Function;

/**
 * @author Frank
 * modify by @author KenelLiu
 */
public class StringField extends ValueFieldImpl
{

	//内部类,所有内部类在此声明----------------------------------------------------------------------------------

	//静态对象,所有static在此声明并初始化------------------------------------------------------------------------

	//内部对象,所有在本类中创建并使用的对象在此声明--------------------------------------------------------------

	/**
	 * 
	 */
	private static final long serialVersionUID = 5971309765452721210L;
	//属性对象,所有在本类中创建，并允许外部访问的对象在此声明并提供get/set方法-----------------------------------
	private String strValue;
	public void setValue(String strValue)
	{
		if(strValue==null)
		{
			strValue="";
		}
		this.strValue=strValue;
	}
	public String getValue()
	{
		return this.strValue;
	}

	//引用对象,所有在外部创建并传入使用的对象在此声明并提供set方法-----------------------------------------------

	//内部方法,所有仅在本类或派生类中使用的函数在此定义为protected方法-------------------------------------------

	//公共方法,所有可提供外部使用的函数在此定义为public方法------------------------------------------------------
	public void toXML(StringBuilder strbXML)
	{
		strbXML.append("<STRF N=\"").append(this.getName()).append("\"");
		strbXML.append(" V=\"").append(Function.FormatTextForXML(this.strValue)).append("\"/>");
	}
	
	public void toXMLWithIndent(int nIndentSize,StringBuilder strbXML)
	{
		String strIndent=Utility.makeSameCharString('\t',nIndentSize);
		
		strbXML.append(strIndent).append("<STRF N=\"").append(this.getName()).append("\"");
		strbXML.append(" V=\"").append(Function.FormatTextForXML(this.strValue)).append("\"/>\r\n");
	}
	@Override
	public void toXMLLog(StringBuilder strbXML) {
		String value=this.strValue;
		if(value.length()>100){
			value=value.substring(0, 100)+"......";
		}
		strbXML.append("<STRF N=\"").append(this.getName()).append("\"");		
		strbXML.append(" V=\"").append(Function.FormatTextForXML(value)).append("\"/>");
		
	}	
	
	public Object getObjectValue()
	{
		return strValue;
	}

	public ObjectExt getObject()
	{
		return new ObjectExt(this.getType(),strValue);
	}

	//接口实现,所有实现接口函数的实现在此定义--------------------------------------------------------------------

	//事件处理,所有重载派生类的事件类方法(一般为on...ed)在此定义-------------------------------------------------

	//事件定义,所有在本类中定义并调用，由派生类实现或重载的事件类方法(一般为on...ed)在此定义---------------------

	//构造函数,所有构造函数在此定义------------------------------------------------------------------------------

	public StringField(String strFieldName)
	{

		//请在此加入初始化代码,内部对象和属性对象负责创建或赋初值,引用对象初始化为null，初始化完成后在设置各对象之间的关系
		super(strFieldName);
		
		setType(ValueField.STRING_TYPE);
		
		this.strValue	="";
	}

	public StringField(String strFieldName,String strValue)
	{

		//请在此加入初始化代码,内部对象和属性对象负责创建或赋初值,引用对象初始化为null，初始化完成后在设置各对象之间的关系
		super(strFieldName);
		
		setType(ValueField.STRING_TYPE);
		
		if(strValue==null)
		{
			strValue="";
		}
		this.strValue	=strValue;
	}
	public String toJSON()
	{
		StringBuffer str_JSON=new StringBuffer();
		str_JSON.append("\"").append(this.getName()).append("\"").append(":\"").append(Function.FormatTextForJson(this.strValue)).append("\",");
		return str_JSON.toString();
	}

	public String toJSONString()
	{
		StringBuffer str_JSON=new StringBuffer();
		str_JSON.append("\\\"").append(this.getName()).append("\\\"").append(":\\\"").append(Function.FormatTextForJson(this.strValue)).append(
						"\\\",");
		return str_JSON.toString();
	}
	
	public String toString()
	{
		String value=this.strValue;
		if(value.length()>100){
			value=value.substring(0, 100)+"......";
		}
		StringBuffer str_JSON=new StringBuffer();
		str_JSON.append("\\\"").append(this.getName()).append("\\\"").append(":\\\"").append(Function.FormatTextForJson(value)).append(
						"\\\",");
		return str_JSON.toString();
	}
}
