/**
 * 
 */
package com.cdoframework.cdolib.web.util;

import java.util.TreeSet;

import com.cdoframework.cdolib.base.Utility;
import com.cdoframework.cdolib.data.cdo.CDO;

/**
 * @author Administrator
 *
 */
public class TransFilter
{

	   private TreeSet<String> setTrans;
	   private String strFilterName;
	   private String strXMLFile;
	   /**存放需要登录验证的trans */
	   private TreeSet<String> setTransNeedLogin;
	   
	   public TransFilter(String strFilterName,String strXMLFile,String strEncoding) throws Exception
	   {
		   setTrans = new TreeSet<String>();
		   setTransNeedLogin = new TreeSet<String>();
		   this.strFilterName = strFilterName;
		   this.strXMLFile = strXMLFile;
		   String strXML = Utility.readTextResource(strXMLFile,strEncoding);
		   if(strXML==null)
		   {
			   throw new Exception("Faild to parsing file "+strXMLFile);
		   }
		   CDO cdo = CDO.fromXML(strXML);
		   if(cdo.exists("strsTrans") && cdo.getObject("strsTrans") != null){
			   String[] strsTrans = cdo.getStringArrayValue("strsTrans");
			   for(String strTrans:strsTrans)
			   {
				   setTrans.add(strTrans); 
			   }
		   }
		   
		   if(cdo.exists("strsTransNeedLogin") && cdo.getObject("strsTransNeedLogin") != null){
			   String[] strsTransNeedLogin = cdo.getStringArrayValue("strsTransNeedLogin");
			   for(String strTrans:strsTransNeedLogin)
			   {
				   setTransNeedLogin.add(strTrans); 
			   }
		   }
	   }
	   public boolean pass(String strServiceName,String strTransName)
	   {
		   String strKey = strServiceName+"."+strTransName;
		   return setTrans.contains(strKey);
	   }
	   
	   /**
	    * 检查登录用户是否可以访问对应trans
	    * @param strServiceName
	    * @param strTransName
	    * @return
	    */
	   public boolean passNeedLogin(String strServiceName,String strTransName)
	   {
		   String strKey = strServiceName+"."+strTransName;
		   // 登录用户可以访问非登录限制的trans
		   return setTrans.contains(strKey) || setTransNeedLogin.contains(strKey);
	   }
	   
	   /**
	    * 检查trans是否在登录验证列表中
	    * @param strServiceName
	    * @param strTransName
	    * @return
	    */
	   public boolean needLoginTransContains(String strServiceName,String strTransName)
	   {
		   String strKey = strServiceName+"."+strTransName;
		   // 检查trans是否在登录验证列表中
		   return setTransNeedLogin.contains(strKey);
	   }
}
