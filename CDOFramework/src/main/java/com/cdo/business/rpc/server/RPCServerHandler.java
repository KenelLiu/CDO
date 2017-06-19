package com.cdo.business.rpc.server;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;

import com.cdo.business.BusinessService;
import com.cdo.business.rpc.RPCFile;
import com.cdo.google.handle.CDOMessage;
import com.cdo.google.handle.Header;
import com.cdo.google.handle.ParseProtoCDO;
import com.cdo.google.handle.ProtoProtocol;
import com.cdo.google.protocol.GoogleCDO;
import com.cdo.util.common.UUidGenerator;
import com.cdo.util.constants.Constants;
import com.cdoframework.cdolib.base.Return;
import com.cdoframework.cdolib.data.cdo.CDO;
import com.cdoframework.cdolib.servicebus.ITransService;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.SystemPropertyUtil;

public class RPCServerHandler extends SimpleChannelInboundHandler<CDOMessage> {

	private static Logger logger=Logger.getLogger(RPCServerHandler.class);
	private final  BusinessService serviceBus=BusinessService.getInstance();
	static Map<String,SocketChannel> socketChannelMap=new ConcurrentHashMap<String, SocketChannel>();
	private ExecutorService executor =Executors.newFixedThreadPool(Math.max(5,SystemPropertyUtil.getInt(Constants.Netty.THREAD_BUSINESS,Runtime.getRuntime().availableProcessors()*2)));
	
    /**
     * 防止   客户机与服务器之间的长连接   发生阻塞,业务数据采用线程池处理,长连接channel仅用于数据传输，
     * 多个action通过长连接 发送请求到service端 
     */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, CDOMessage msg)
			throws Exception {	
		  Header header=msg.getHeader();
		  
		if(header.getType()==ProtoProtocol.TYPE_CDO){			
			try{
				GoogleCDO.CDOProto proto=(GoogleCDO.CDOProto)(msg.getBody());
				List<File> listFile=msg.getFiles();
				String clientId=UUidGenerator.ClientId.toString(proto.getClientId().toByteArray());
				socketChannelMap.put(clientId,(SocketChannel)ctx.channel());						
				InetSocketAddress remoteAddress=((SocketChannel)ctx.channel()).remoteAddress();			
				Task task=new Task(proto,remoteAddress,listFile);
				executor.submit(task);
			}finally{
				ReferenceCountUtil.release(msg);
			}
		}else if(header.getType()==ProtoProtocol.TYPE_HEARTBEAT_REQ){
			//客户端主动发起 心跳检查，服务端回复心跳
		 try{	
				Header resHeader=new Header();
				resHeader.setType(ProtoProtocol.TYPE_HEARTBEAT_RES);
				CDOMessage resMessage=new CDOMessage();
				resMessage.setHeader(resHeader);
				ctx.writeAndFlush(resMessage);
				
			    if(logger.isDebugEnabled())
	    			logger.debug("server receive client address["+(InetSocketAddress)ctx.channel().remoteAddress()+"] heart msg:"+msg);
			}finally{
				ReferenceCountUtil.release(msg);
			}
		}else if(header.getType()==ProtoProtocol.TYPE_HEARTBEAT_RES){
			try{
				//服务端主动发起心跳检查,客户端回复心跳
			    if(logger.isDebugEnabled())
			    			logger.debug("server receive client address["+(InetSocketAddress)ctx.channel().remoteAddress()+"] heart msg:"+msg);
			}finally{
				ReferenceCountUtil.release(msg);
			}
		}else if(header.getType()==ProtoProtocol.TYPE_STOPLOCALServer){
			RPCServer.stop();
		}else{
			ctx.fireChannelRead(msg);
		}
	}

	private class Task implements Runnable{
		private GoogleCDO.CDOProto proto;
		private List<File> listFile;
		private InetSocketAddress remoteInetAddress;
		public Task(GoogleCDO.CDOProto proto,InetSocketAddress remoteInetAddress,List<File> listFile){
			this.proto=proto;
			this.remoteInetAddress=remoteInetAddress;
			this.listFile=listFile;
		}
		@Override
		public void run() {				
			
			CDO cdoRequest=null;				
			CDO cdoOutput=null;
			try{
				String clientId=UUidGenerator.ClientId.toString(this.proto.getClientId().toByteArray());
				SocketChannel channel=socketChannelMap.get(clientId);			
				if(channel==null){
					//管道被关闭 或不可用
					logger.error("channel is null,clientId="+clientId+",client remoteInetAddress="+remoteInetAddress);
					return;
				}			
		    	//响应里存在文件,即下载到客服端.是否有文件传输到客户端
				List<File> files=null;
				
				//解释cdo
		    	try{
					cdoRequest=ParseProtoCDO.ProtoParse.parse(proto);
					cdoOutput=handleTrans(cdoRequest);	   
		    		files=RPCFile.readFileFromCDO(cdoOutput.getCDOValue("cdoResponse"));    		
		    	}catch(Throwable ex){
				    //解释异常 ,..文件不存在,返回给错误给客户端
		    		logger.error(ex.getMessage(), ex);
		    		if(cdoOutput==null)
		    			cdoOutput=new CDO();
		    		setFailOutCDO(cdoOutput, "RPCServer "+ex.getMessage());
		    	}
		    	
				GoogleCDO.CDOProto.Builder resProtoBuiler=cdoOutput.toProtoBuilder();
				resProtoBuiler.setCallId(proto.getCallId());
				
				Header resHeader=new Header();
				resHeader.setType(ProtoProtocol.TYPE_CDO);
				CDOMessage resMessage=new CDOMessage();
				resMessage.setHeader(resHeader);
				resMessage.setBody(resProtoBuiler.build());
				resMessage.setFiles(files);
				
				channel.writeAndFlush(resMessage);
				
			}finally{
				CDO.release(cdoRequest);
				CDO.deepRelease(cdoOutput);				
			}
		}
		
		private CDO handleTrans(CDO cdoRequest){
			
			CDO cdoOutput=new CDO();
			CDO cdoResponse=new CDO();;			
			try{							
				//将client传过来的文件 设置到cdoRequest里
				RPCFile.setFile2CDO(cdoRequest, this.listFile);
				//处理业务
				Return ret=serviceBus.handleTrans(cdoRequest, cdoResponse);
				if(ret==null){
					String strServiceName=cdoRequest.exists(ITransService.SERVICENAME_KEY)?cdoRequest.getStringValue(ITransService.SERVICENAME_KEY):"null";
					String strTransName=cdoRequest.exists(ITransService.TRANSNAME_KEY)?cdoRequest.getStringValue(ITransService.TRANSNAME_KEY):"null";					
					setFailOutCDO(cdoOutput," ret is null,Request method :strServiceName="+strServiceName+",strTransName="+strTransName);	
					logger.error("ret is null,Request method:strServiceName="+strServiceName+",strTransName="+strTransName);
				}else{
					CDO cdoReturn=new CDO();
					cdoReturn.setIntegerValue("nCode",ret.getCode());
					cdoReturn.setStringValue("strText",ret.getText());
					cdoReturn.setStringValue("strInfo",ret.getInfo());

					cdoOutput.setCDOValue("cdoReturn",cdoReturn);
					cdoOutput.setCDOValue("cdoResponse", cdoResponse);					
				}
			}catch(Throwable ex){
				logger.error(ex.getMessage(), ex);	
				setFailOutCDO(cdoOutput,"服务端处理异常:"+ex.getMessage());
			}	
			return cdoOutput;
		}

		private void setFailOutCDO(CDO cdoOutput,String message){
			
			CDO cdoReturn=new CDO();
			cdoReturn.setIntegerValue("nCode",-1);
			cdoReturn.setStringValue("strText",message);
			cdoReturn.setStringValue("strInfo",message);
			
			cdoOutput.setCDOValue("cdoReturn",cdoReturn);
			cdoOutput.setCDOValue("cdoResponse",new CDO());		
		}
		
	}
	
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {        
        super.channelInactive(ctx);
        SocketChannel channel=((SocketChannel)ctx.channel());
        //该方法用于服务器内部构建的SOA服务，如果对外开放支持长连接服务,重新构建。             
        for(Iterator<Map.Entry<String, SocketChannel>> it=socketChannelMap.entrySet().iterator();it.hasNext();){
        	Entry<String, SocketChannel> entry=it.next();
        	if(entry.getValue().equals(channel)){
        		socketChannelMap.remove(entry.getKey());        	    
        		break;
        	}
        }
    }
    /**
     * 服务端设定  在60秒内未接受到客户端的请求数据,则关闭连接
     * 空闲时 使用客户端来检查,大约每5秒发起心跳检查   @see RPCClient#IdleStateHandler
     * 
     */
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {//The connection is closed when there is no inbound traffic  for 30 seconds.see RPCServerInitializer,RPCClient
                case READER_IDLE:
                	ctx.close();
                    break;
                case WRITER_IDLE:                	
        			break;
                default:
                    break;
            }
        }
    }
}
