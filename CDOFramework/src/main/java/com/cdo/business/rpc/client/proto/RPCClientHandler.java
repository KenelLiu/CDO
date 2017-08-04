package com.cdo.business.rpc.client.proto;

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;

import com.cdo.business.rpc.client.Call;
import com.cdo.business.rpc.client.CallsLinkedHashMap;
import com.cdo.business.rpc.client.ClientHandler;
import com.cdo.google.Header;
import com.cdo.google.handle.CDOMessage;
import com.cdo.google.handle.ProtoProtocol;
import com.cdo.google.protocol.GoogleCDO;

import com.cdoframework.cdolib.data.cdo.CDO;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class RPCClientHandler extends  ClientHandler {
	private  Logger logger=Logger.getLogger(RPCClientHandler.class);

    private  Channel channel; 
    final CallsLinkedHashMap calls = new CallsLinkedHashMap();
    /** A counter for generating call IDs. */
    final AtomicInteger callIdCounter = new AtomicInteger();
    
    public RPCResponse handleTrans(CDO cdoRequest) {      	
    	//CDO请求数据
    	int callId=callIdCounter.getAndIncrement() & Integer.MAX_VALUE;
        final Call call =new Call(callId);    
    	GoogleCDO.CDOProto.Builder proto=cdoRequest.toProtoBuilder();
    	proto.setCallId(callId);
		//构造proto message发送数据
		Header reqHeader=new Header();
		reqHeader.setType(ProtoProtocol.TYPE_CDO);
		CDOMessage reqMessage=new CDOMessage();
		reqMessage.setHeader(reqHeader);
		reqMessage.setBody(proto.build());				
		
        channel.writeAndFlush(reqMessage);
        //同步调用
        calls.put(callId, call,new RPCResponse());        
        boolean interrupted = false;
        synchronized (call) {
          while (!call.done()) {
            try {
              call.wait();                 
            } catch (InterruptedException ie) {
              // save the fact that we were interrupted
              interrupted = true;
            }
          }
          if (interrupted) {
            // set the interrupt flag now that we are done waiting
            Thread.currentThread().interrupt();
          }          
         reqMessage=null;
         return (RPCResponse)call.getRPCResponse();         
       }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        channel = ctx.channel();
        //clientId=ByteString.copyFrom(UUidGenerator.ClientId.getClientId());

    }
    
   @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {    
    	if(msg instanceof CDOMessage){ 
    		CDOMessage cdoMessage=(CDOMessage)msg;    	
		    switch(cdoMessage.getHeader().getType()){
		    	case ProtoProtocol.TYPE_CDO:
	        		GoogleCDO.CDOProto proto=(GoogleCDO.CDOProto)(cdoMessage.getBody());
	    			int callId=proto.getCallId();
	    			Call call = calls.get(callId);
	    	        calls.remove(callId);    	        
	    	        call.setRPCResponse(new RPCResponse(proto,null));	
	    	      break;
	    	    default:
	    	    	ctx.fireChannelRead(msg);
		    }	
    	}else{
    		ctx.fireChannelRead(msg);
    	}        
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
       logger.warn("channel ["+ctx.channel()+"] error:"+cause.getMessage()+",channel will close",cause);
    	//出现channel异常 关闭channel连接    	
       try{if(channel!=null) channel.close();if(ctx!=null)ctx.close();}catch(Exception ex){};	
    }
      
	@Override
	public String toString(){
		return "channel="+channel;
	}	
}
