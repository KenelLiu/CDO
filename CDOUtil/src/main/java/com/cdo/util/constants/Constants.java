package com.cdo.util.constants;

import com.cdo.util.exception.EncodeException;

/**
 * 
 * @author KenelLiu
 *
 */
public class Constants {

	public static class Netty{
		//服务端 RPCServer
		public static final String THREAD_SERVER_BOSS="io.netty.eventLoopThreads";//netty主线程，默认 Runtime.getRuntime().availableProcessors()*1
		public static final String THREAD_SERVER_WORK="io.netty.thread.channel";//处理netty主线程工作线程,默认Runtime.getRuntime().availableProcessors()*2
		//服务端  使用io channel进行处理 业务
		public static final String THREAD_BUSINESS_USE_CHANNEL="io.netty.bussiness.useChannel";

		//客户端 RPCClient
		public static final String THREAD_CLIENT_WORK="io.netty.client.eventLoopThreads";//默认Runtime.getRuntime().availableProcessors()	
		//业务处理线程
		public static final String THREAD_BUSINESS_CoreSize="io.netty.thread.bussiness.coreSize";//处理业务线程默认保持线程数量   Runtime.getRuntime().availableProcessors()*2
		public static final String THREAD_BUSINESS_MaxSize="io.netty.thread.bussiness.maxSize";//处理业务最大线程默认  Runtime.getRuntime().availableProcessors()*3
		public static final String THREAD_BUSINESS_IDLE_KeepAliveTime="io.netty.thread.bussiness.idleKeepAliveTime";//空闲多长时间关闭 单位秒，默认60秒
		public static final String THREAD_BUSINESS_TASK_QueueSize="io.netty.thread.bussiness.queueSize";//队列最长是多少
		//客户端同步调用/异步调用线程
		public static final String THREAD_BUSINESS_CLIENT_ASYNC="io.netty.thread.bussiness.client.async";//默认是异步调用
				
		public static final String BUSINESS_TIME_OUT="io.netty.bussiness.timeOut";//客户端发送请求，等待服务端返回结果的超时时间,默认30分钟
		
		
	}
	//分页常量
	public static class Page{		
		public static final String PAGE_INDEX="nPageIndex";
		public static final String PAGE_SIZE="nPageSize";
		
		//-------- 每页显示最小 最大数量-----------------//
		public static final int PAGE_SIZE_MIN=10;
		public static final int PAGE_SIZE_MAX=100;
	}
	
	//--字符编码变量-------//
	public static class Encoding {
		public static final String CHARSET_UTF8="UTF-8";
		public static final String CHARSET_GBK="GBK";
		public static final String CHARSET_GBK2312="GBK2312";
		public static final String CHARSET_ISO8859="ISO-8859-1";
		
		public static final byte[]  encode(String value){
			return encode(value, CHARSET_UTF8);
		}
		
		public static final byte[] encode(String value,String charset){
			if(value==null)
				throw new EncodeException("value is null");
			try{
				return value.getBytes(charset);
			}catch(Exception ex){
				throw new EncodeException(ex);
			}
		}
	}
	//byteBuffer读取字节长度
	public static class Byte{
		public final static int maxSize=2048;
		public final static int defaultSize=1024;
		public final static int minSize=512;
	}
	
	//CDO传输数据的变量
	public static class CDO{
		public final static String HTTP_CDO_REQUEST="$$CDORequest$$";
		public final static String HTTP_CDO_UPLOADFILE_KEY="$CDOSerialFile$";
		
		public final static String HTTP_CDO_RESPONSE="$$CDOResponse$$";
		public final static String HTTP_UPLOAD_FILE_PATH="uploadPath";
		public final static String TEMP_FILE_PATH="tmpPath";
		public final static String UPLOAD_FILE_MAX_SIZE="uploadFileMaxSize";
		
	}
}
