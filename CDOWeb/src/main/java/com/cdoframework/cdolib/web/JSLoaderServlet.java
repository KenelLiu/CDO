/**
 * www.cdoforum.com 2007版权所有
 * 
 * $Header: /CVSData/Frank/CVSROOT/CDOForum/CDOWeb/Source/com/cdoframework/cdolib/web/JSLoaderServlet.java,v 1.1
 * 2008/03/05 01:35:44 Frank Exp $
 * 
 * $Log: JSLoaderServlet.java,v $ Revision 1.1 2008/03/05 01:35:44 Frank *** empty log message ***
 * 
 * Revision 1.5 2008/01/10 10:30:27 Frank *** empty log message ***
 * 
 * Revision 1.4 2008/01/10 09:51:22 Frank *** empty log message ***
 * 
 * Revision 1.3 2008/01/10 09:47:34 Frank *** empty log message ***
 * 
 * Revision 1.2 2008/01/10 09:12:10 Frank *** empty log message ***
 * 
 * Revision 1.1 2008/01/07 10:57:48 Frank *** empty log message ***
 * 
 * Revision 1.1 2007/12/25 14:11:29 Frank *** empty log message ***
 * 
 * Revision 1.8 2007/12/15 09:07:22 Frank *** empty log message ***
 * 
 * Revision 1.7 2007/12/10 12:01:03 Frank *** empty log message ***
 * 
 * Revision 1.6 2007/08/31 15:10:58 Frank *** empty log message ***
 * 
 * Revision 1.5 2007/08/31 14:58:54 Frank *** empty log message ***
 * 
 * Revision 1.4 2007/08/31 13:43:06 Frank *** empty log message ***
 * 
 * Revision 1.3 2007/08/31 02:50:54 Frank *** empty log message ***
 * 
 * Revision 1.2 2007/08/29 08:32:18 Frank *** empty log message ***
 * 
 * Revision 1.1 2007/08/22 06:34:06 Frank *** empty log message ***
 * 
 * 
 */

package com.cdoframework.cdolib.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cdoframework.cdolib.base.Utility;

/**
 * @author Frank
 */
public class JSLoaderServlet extends HttpServlet
{

	// 静态对象,所有static在此声明并初始化------------------------------------------------------------------------

	// 内部对象,所有在本类中创建并使用的对象在此声明--------------------------------------------------------------
	protected ServletConfig servletConfig;

	// 属性对象,所有在本类中创建，并允许外部访问的对象在此声明并提供get/set方法-----------------------------------

	// 引用对象,所有在外部创建并传入使用的对象在此声明并提供set方法-----------------------------------------------

	// 内部方法,所有仅在本类或派生类中使用的函数在此定义为protected方法-------------------------------------------

	// 公共方法,所有可提供外部使用的函数在此定义为public方法------------------------------------------------------

	// 接口实现,所有实现接口函数的实现在此定义--------------------------------------------------------------------

	public void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException
	{
		doPost(request,response);
	}

	public void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException
	{
		// 获取参数
		String strResource=request.getParameter("Resource");
		if(strResource==null)
		{
			throw new ServletException("Parameter(Resource) not found");
		}
		if(!(strResource.endsWith(".js") || strResource.endsWith(".JS")))
		{
			throw new ServletException("Parameter(Resource) is not a js file. ");
		}

		// 加载JS文件
		String strJS=Utility.readTextResource(strResource,"utf-8");

		// 输出结果		
		response.setContentType("application/x-javascript;charset=utf-8");
		response.setHeader("Cache-Control", "max-age=3600000");//缓存一天
		
//		java.util.Date date = new java.util.Date(); 
//		response.setDateHeader("Last-Modified",date.getTime());   
//		response.setDateHeader("Expires",date.getTime()+3600000); //不同浏览器对不同的操作有不同的处理方式
		PrintWriter out=response.getWriter();
		out.write(strJS);
		out.flush();
		out.close();
	}

	// 事件处理,所有重载派生类的事件类方法(一般为on...ed)在此定义-------------------------------------------------

	// 事件定义,所有在本类中定义并调用，由派生类实现或重载的事件类方法(一般为on...ed)在此定义---------------------

	// 构造函数,所有构造函数在此定义------------------------------------------------------------------------------

	public JSLoaderServlet()
	{
		// 请在此加入初始化代码,内部对象和属性对象负责创建或赋初值,引用对象初始化为null，初始化完成后在设置各对象之间的关系
	}

}
