package com.cdo.business.rpc.server.proto;

import java.io.File;
import java.util.List;
import java.util.concurrent.TransferQueue;

import org.apache.log4j.Logger;

import com.cdo.business.BusinessService;
import com.cdo.business.rpc.RPCFile;
import com.cdo.google.Header;
import com.cdo.google.ParseProtoCDO;
import com.cdo.google.handle.CDOMessage;
import com.cdo.google.handle.ProtoProtocol;
import com.cdo.google.protocol.GoogleCDO;
import com.cdoframework.cdolib.base.Return;
import com.cdoframework.cdolib.data.cdo.CDO;
import com.cdoframework.cdolib.servicebus.IServiceBus;
import com.cdoframework.cdolib.servicebus.ITransService;

public class Consumer implements Runnable {
	private static Logger logger=Logger.getLogger(Consumer.class);
	private boolean stop=false;
	private String name;
	private TransferQueue<GoogleCDO.CDOProto> lnkTransQueue;
	private RPCServerHandler handle;
	private IServiceBus serviceBus;
	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public Consumer(String name,TransferQueue<GoogleCDO.CDOProto> lnkTransQueue,RPCServerHandler handle){
		this.name=name;
		this.lnkTransQueue=lnkTransQueue;
		this.handle=handle;
		this.serviceBus=BusinessService.getInstance().getServiceBus();
	}
	@Override
	public void run() {	
		while(true){
			GoogleCDO.CDOProto proto=null;
			try {
				if(logger.isDebugEnabled())
					logger.debug("name="+name+" is waiting to take element....,Thread="+Thread.currentThread().getId());
				proto=lnkTransQueue.take();
				if(logger.isDebugEnabled())							
					logger.debug("name="+name+" received Element....,Thread="+Thread.currentThread().getId());
			} catch (InterruptedException  ex) {
				if(logger.isDebugEnabled())
					logger.debug("name="+name+"  Thread break,sleep 0.5 seconds,continue run()");
				try{Thread.sleep(500);}catch(Exception e){}									
			}catch(Exception ex){
				if(lnkTransQueue==null || stop){
					logger.warn("name="+name+",stop="+stop+",lnkTransQueue="+lnkTransQueue+",break Consumer queue");					
					break;
				}
			}
			if(proto!=null){
				process(proto);					
			}
		}
	}
	
	 void process(GoogleCDO.CDOProto proto){
			CDO cdoRequest=null;				
			CDO cdoOutput=null;
			GoogleCDO.CDOProto.Builder resProtoBuiler=null;
			try{		
				String strServiceName=null;
				String strTransName=null;				
				//解释cdo
		    	try{
					cdoRequest=ParseProtoCDO.ProtoParse.parse(proto);
					strServiceName=cdoRequest.exists(ITransService.SERVICENAME_KEY)?cdoRequest.getStringValue(ITransService.SERVICENAME_KEY):"null";
					strTransName=cdoRequest.exists(ITransService.TRANSNAME_KEY)?cdoRequest.getStringValue(ITransService.TRANSNAME_KEY):"null";				
					//执行业务逻辑后  输出......
					cdoOutput=handleTrans(cdoRequest,strServiceName,strTransName);	   		    		
		    		resProtoBuiler=cdoOutput.toProtoBuilder();		    		
		    	}catch(Throwable ex){
				    //解释异常 ,返回给错误给客户端
		    		logger.error(ex.getMessage(), ex);
		    		if(cdoOutput==null)
		    			cdoOutput=new CDO();
		    		setFailOutCDO(cdoOutput, "strServiceName="+strServiceName+",strTransName="+strTransName+" RPCServer 发生异常:"+ex.getMessage());
		    		resProtoBuiler=cdoOutput.toProtoBuilder();
		    	}   	
		    	//同步调用,回写结果
				resProtoBuiler.setCallId(proto.getCallId());				
				Header resHeader=new Header();
				resHeader.setType(ProtoProtocol.TYPE_CDO);
				CDOMessage resMessage=new CDOMessage();
				resMessage.setHeader(resHeader);
				resMessage.setBody(resProtoBuiler.build());

				handle.writeAndFlush(resMessage);
			}finally{
				cdoRequest.deepRelease();
				cdoOutput.deepRelease();
				resProtoBuiler=null;
				proto=null;
			}		
		}	
		/**
		 * 为了在客户端比较快速解释，使用固定顺序，采用 cdoReturn放在第一位,cdoResponse放在第二位
		 * 返回的 cdoOutput 只有2个cdo对象,cdoReturn,cdoResponse
		 * cdoReturn 只有 三个数据 
		 * 其余 cdoResponse里的数据
		 * 使用下标  判断 替换 原来的 字符串比较
		 * @param cdoRequest
		 * @param listFile
		 * @param strServiceName
		 * @param strTransName
		 * @return
		 */
		private CDO handleTrans(CDO cdoRequest,String strServiceName,String strTransName){		
			CDO cdoOutput=new CDO();
			CDO cdoResponse=new CDO();;		
			try{							
				//处理业务
				Return ret=serviceBus.handleTrans(cdoRequest, cdoResponse);
				if(ret==null){			
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
				setFailOutCDO(cdoOutput,"strServiceName="+strServiceName+",strTransName="+strTransName+"服务端业务处理异常:"+ex.getMessage());
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
