package com.cdo.business.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * 
 * @author KenelLiu
 *
 */
public class CharacterEncodingFilter implements Filter
{

	// 静态对象,所有static在此声明并初始化------------------------------------------------------------------------

	// 内部对象,所有在本类中创建并使用的对象在此声明--------------------------------------------------------------
	protected String strEncoding;

	// 属性对象,所有在本类中创建，并允许外部访问的对象在此声明并提供get/set方法-----------------------------------

	// 引用对象,所有在外部创建并传入使用的对象在此声明并提供set方法-----------------------------------------------

	// 内部方法,所有仅在本类或派生类中使用的函数在此定义为protected方法-------------------------------------------

	// 公共方法,所有可提供外部使用的函数在此定义为public方法------------------------------------------------------

	// 接口实现,所有实现接口函数的实现在此定义--------------------------------------------------------------------

	public void destroy()
	{
	}

	public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain) throws IOException,
					ServletException
	{
		// Conditionally select and set the character encoding to be used
		if(strEncoding!=null)
		{
			request.setCharacterEncoding(strEncoding);
		}

		chain.doFilter(request,response);
	}

	public void init(FilterConfig filterConfig) throws ServletException
	{
		this.strEncoding=filterConfig.getInitParameter("encoding");
	}

	// 事件处理,所有重载派生类的事件类方法(一般为on...ed)在此定义-------------------------------------------------

	// 事件定义,所有在本类中定义并调用，由派生类实现或重载的事件类方法(一般为on...ed)在此定义---------------------

	// 构造函数,所有构造函数在此定义------------------------------------------------------------------------------

	public CharacterEncodingFilter()
	{

		// 请在此加入初始化代码,内部对象和属性对象负责创建或赋初值,引用对象初始化为null，初始化完成后在设置各对象之间的关系
		strEncoding=null;
	}

}
