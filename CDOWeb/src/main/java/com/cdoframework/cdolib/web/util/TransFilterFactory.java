/**
 * 
 */
package com.cdoframework.cdolib.web.util;

import java.util.HashMap;

/**
 * @author Administrator
 *
 */
public class TransFilterFactory
{
  	private static TransFilter defaultTransFilter;
  	private static HashMap<String,TransFilter> hmTransFilter = new HashMap<String,TransFilter>(2);
	public static TransFilter initDefaultTransFilter(String strXMLFile,String strEncoding) throws Exception
	{
		defaultTransFilter = new TransFilter("DEFAULT",strXMLFile,strEncoding);
		return defaultTransFilter;
	}
	public static TransFilter appendTransFilter(String strFilterName,String strXMLFile,String strEncoding) throws Exception
	{
		TransFilter transFilter = new TransFilter(strFilterName,strXMLFile,strEncoding);
		hmTransFilter.put(strFilterName,transFilter);
		return transFilter;
	}
	public static TransFilter getTransFilter(String filterName)
	{
		return hmTransFilter.get(filterName);
	}
	public static TransFilter getDefaultTransFilter(String filterName)
	{
		return defaultTransFilter;
	}
	/**
	 * 
	 */
	private TransFilterFactory()
	{
		
	}
}
