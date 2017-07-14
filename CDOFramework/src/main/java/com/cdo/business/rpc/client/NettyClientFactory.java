package com.cdo.business.rpc.client;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.cdo.business.rpc.route.RouteManager;
import com.cdo.google.handle.CDOProtobufDecoder;
import com.cdo.google.handle.CDOProtobufEncoder;
import com.cdo.util.constants.Constants;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
//import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
//import io.netty.handler.ssl.SslContext;
//import io.netty.handler.ssl.SslContextBuilder;
//import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.SystemPropertyUtil;

public class NettyClientFactory {
	private final static Logger logger=Logger.getLogger(NettyClientFactory.class);	
	private ExecutorService executor=Executors.newScheduledThreadPool(1);
	private RouteManager routeManager=RouteManager.getInstance();
	
	private static final NettyClientFactory clientFactory=new NettyClientFactory();
	//客户端连接多个服务端，共享一个工作线程组.
	private  Bootstrap bootstrap;
	
	private  int channelThread;	
	private  int retryInterval;//若断开，每隔多长时间重试一次 单位为秒  默认 5
	private  int retryCount;//重试 多少次后.中断重试。默认3
	public int maxClientCount;//在jvm里   client 同时开启多少个长连接  连接同一个remoteAddress,默认1
	
	//retryMap临时保存 remoteAddress-localAddress 的重试次数
	private Map<String,Integer>  retryMap=new HashMap<String,Integer>();
	//
	

	private NettyClientFactory(){
		channelThread=Math.max(2,SystemPropertyUtil.getInt(Constants.Netty.THREAD_CLIENT_WORK,Runtime.getRuntime().availableProcessors()));
		retryInterval=Math.max(5, SystemPropertyUtil.getInt(Constants.Netty.THREAD_CLIENT_RETRY_INTERVAL,5));
		retryCount=Math.max(3, SystemPropertyUtil.getInt(Constants.Netty.THREAD_CLIENT_RETRY_COUNT,3));
		maxClientCount=Math.max(1, SystemPropertyUtil.getInt(Constants.Netty.THREAD_CLIENT_CONNECT_ConnCount,1));
		init(channelThread);
		if(logger.isInfoEnabled())
			logger.info("NettyClientFactory init channelThread="+channelThread+",retryInterval="+retryInterval+",retryCount="+retryCount+",maxClientCount="+maxClientCount);
	}
	
	public static final NettyClientFactory getDefaultInstance(){
		return clientFactory;
	}
	/**
	 * 根据remoteAddress 创建一个长连接
	 * @param remoteAddress
	 */
	public void connect(String remoteAddress){
		String[] hostPort=remoteAddress.split(":");
		String host=hostPort[0].trim();
		int port=Integer.parseInt(hostPort[1].trim());		
		ChannelFuture future = bootstrap.connect(host, port);	    
	    channelFutureListener(future,remoteAddress);
	}
	/**
	 * 根据remoteAddress 同时创建多个客户端，客户端的个数
	 * @param remoteAddress
	 */
    public    void  createMutiClient(final String remoteAddress){
	   	 ExecutorService executor=Executors.newScheduledThreadPool(1);
	   	 executor.submit(new Runnable() {			
				@Override
				public void run() {
	   			for(int k=0;k<maxClientCount;k++){    				
	   				connect(remoteAddress);	   				
	   				try{Thread.sleep(2000);}catch(Exception ex){}
	   			}
				}
			});
	   	executor.shutdown(); 
   }
	/**
	 * 一个jvm  创建一个 NettyClientFactory实例,初始化一个Bootstrap
	 * @param channelThread  共享的线程池组	 
	 * *  	
	 */
	void init(int channelThread){		
		try {
		        NioEventLoopGroup workerGroup = new NioEventLoopGroup(channelThread);		    	    		    	    
			    bootstrap = new Bootstrap();			    
			    bootstrap.group(workerGroup);
			    bootstrap.channel(NioSocketChannel.class);		    	    
//			    bootstrap.option(ChannelOption.TCP_NODELAY,true);	    	
			    bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			    //连接断开后,需要处理连接 ,尝试重连
			    bootstrap.handler(new ChannelInitializer<SocketChannel>(){
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
			        ChannelPipeline p = ch.pipeline();			       
			        p.addLast(new ChannelInboundHandlerAdapter() {
				          @Override
				          public void channelInactive(ChannelHandlerContext ctx) throws Exception {
				            super.channelInactive(ctx);	
				            SocketChannel socketChannel=(SocketChannel)ctx.channel();		
				            InetSocketAddress remoteAddress=socketChannel.remoteAddress();	
				            InetSocketAddress localAddress=socketChannel.localAddress();	
				            String serverAddress=remoteAddress.getHostString()+":"+remoteAddress.getPort();
				            String clientAddress=localAddress.getHostString()+":"+localAddress.getPort();
				            //删除对应的失效的长连接
				            boolean flag=routeManager.removeRPCClient(serverAddress, ctx.channel().pipeline().get(RPCClientHandler.class));
				            if(!flag){
					        	  logger.warn("delete handle to CircleRPCQueue fail,maybe not found in queue");					        	  
					          }
				            logger.warn( "ctx is channelInactive,after 5 secondes retry connection remote=["+serverAddress+"],local="+clientAddress);
				            executor.execute(new Runnable() {								
									@Override
									public void run() {
										try{
											TimeUnit.SECONDS.sleep(retryInterval);
										}catch(Exception ex){}										
										doConnect(serverAddress,clientAddress);								
									}
								});	  
				          }
				        });			        
			        p.addLast("encoder",new CDOProtobufEncoder());
			        p.addLast("decoder",new CDOProtobufDecoder());  
			        p.addLast("ideaHandler",new IdleStateHandler(60,10,0));
			        p.addLast("heartbeat",new HeartbeatClientHandler());
			        p.addLast("handle",new RPCClientHandler());				
				}            	 
		      });
		    }catch(Exception ex){
		      logger.error(ex.getMessage(), ex);
		    }	    		 
	}
	
	
	
	private void doConnect(String serverAddress,String clientAddress) {
        String key=serverAddress+"-"+clientAddress;
        int retry=retryMap.get(key)==null?0:retryMap.get(key).intValue();
        if(routeManager.isFullCircleQueue(serverAddress)){
        	logger.info(" remote=["+serverAddress+"],local circle client queue is full,noretry conention");
        	return;
        }
        retry++;//记录重试次数
        retryMap.put(key, retry);
        if(clientAddress!=null){
	        InetSocketAddress remote=new InetSocketAddress(serverAddress.split(":")[0], Integer.valueOf(serverAddress.split(":")[1]));
	        InetSocketAddress local=new InetSocketAddress(clientAddress.split(":")[0], Integer.valueOf(clientAddress.split(":")[1]));
		    ChannelFuture future = bootstrap.connect(remote,local);
		    channelFutureListener(future,serverAddress,clientAddress,retry);
        }else{	        
		    ChannelFuture future = bootstrap.connect(serverAddress.split(":")[0],Integer.valueOf(serverAddress.split(":")[1]));
		    channelFutureListener(future,serverAddress);
        }
	
	  }
	
	
	private void channelFutureListener(ChannelFuture future,String remoteAddress){		
		 channelFutureListener(future,remoteAddress,null,0);
	}
	
	private void channelFutureListener(ChannelFuture future,String serverAddress,String clientAddress,int retry ){
		
	    future.addListener(new ChannelFutureListener() {
		      public void operationComplete(ChannelFuture f) throws Exception {	
		         if (f.isSuccess()) {
			    	  Channel channel=f.channel();			         			         			        
			          RPCClientHandler handle=f.channel().pipeline().get(RPCClientHandler.class);
			          //添加到路由
			          boolean flag=routeManager.addRPCClientHandler(serverAddress, handle);	   
			          if(!flag){
			        	  logger.warn("add handle to CircleRPCQueue fail,close channel");
			        	  channel.close();
			          }else{
			        	  logger.info("Started  Client Success  connection,channel="+channel);
			          }
		         } else {	          
		        	 //连接没有成功...重新尝试连接.....	
			          f.channel().close();
			          String key=serverAddress+"-"+clientAddress;
			          if(retry==retryCount){
			          	 retryMap.remove(key);
			          	//不再重试...
			          	logger.warn(" already connection  "+retryCount+" times   failed, remote=["+serverAddress+"],local="+clientAddress+",noretry conention ");
			          	return;
			          }else{
			        	  if(retry>0)
				             logger.warn("retry "+retry+" times  connection fail, remote=["+serverAddress+"],local="+clientAddress);
			          }
			          executor.execute(new Runnable() {								
							@Override
							public void run() {
								try{
									TimeUnit.SECONDS.sleep(retryInterval);
								}catch(Exception ex){}										
								doConnect(serverAddress,clientAddress);								
							}
						});          
		        }
		      }
		    });
		
	}
}
