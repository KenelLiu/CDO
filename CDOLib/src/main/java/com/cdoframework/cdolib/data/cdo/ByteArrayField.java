/**
 * www.cdoforum.com 2007版权所有
 *
 * $Header: /CVSData/Frank/CVSROOT/CDOForum/CDOLib/Source/com/cdoframework/cdolib/data/cdo/ByteArrayField.java,v 1.4 2008/03/12 10:30:58 Frank Exp $
 *
 *
 */

package com.cdoframework.cdolib.data.cdo;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Map;

import com.cdoframework.cdolib.base.DataType;
import com.cdoframework.cdolib.base.Utility;

/**
 * @author Frank
 * modify by @author KenelLiu 
 */
public class ByteArrayField extends ArrayFieldImpl
{

	//内部类,所有内部类在此声明----------------------------------------------------------------------------------

	//静态对象,所有static在此声明并初始化------------------------------------------------------------------------

	//内部对象,所有在本类中创建并使用的对象在此声明--------------------------------------------------------------

	/**
	 * 
	 */
	private static final long serialVersionUID = 1390757657933478538L;
	//属性对象,所有在本类中创建，并允许外部访问的对象在此声明并提供get/set方法-----------------------------------	
	private ByteBuffer buffer=null;	
	private final int dataIndex=5;//数据保存的起始位置
	private final int databuffer=1;//数据占用字节
	
	public void setValue(byte[] bysValue)
	{
		if(bysValue==null)
		{
			bysValue=new byte[0];
		}
		allocate(bysValue);
	}
	public byte[] getValue()
	{
		buffer.position(1);
		int len=buffer.getInt();
		byte[] bsValue=new byte[len];
		buffer.position(dataIndex);
		buffer.limit(buffer.capacity());		
		(buffer.slice()).get(bsValue);		
		return bsValue;
	}

	public byte getValueAt(int nIndex)
	{
		checkArrayIndex(nIndex);
		int pos=dataIndex+databuffer*nIndex;
		buffer.position(pos);
		return buffer.get();
	}

	public void setValueAt(int nIndex,byte byValue)
	{
		checkArrayIndex(nIndex);
		int pos=dataIndex+databuffer*nIndex;
		buffer.position(pos);
		buffer.put(byValue);		
	}
	
	public int getLength()
	{
		buffer.position(1);
		return buffer.getInt();
	}
	
	public Object getObjectValue()
	{
		return getValue();
	}

	public Object getObjectValueAt(int nIndex)
	{
		return new Byte(getValueAt(nIndex));
	}
	
	@Override
	public Buffer getBuffer() {	
		return buffer;
	}
	
	private void allocateBuffer(byte[] bsValue){
		int len=dataIndex+bsValue.length*databuffer;
		buffer=ByteBuffer.allocate(len);
		buffer.put((byte)DataType.BYTE_ARRAY_TYPE);
		buffer.putInt(bsValue.length);
	}
	
	private void allocate(byte[] bsValue){
		if(buffer==null){
			allocateBuffer(bsValue);
		}else{
			buffer.position(1);
			int len=buffer.getInt();
			if(len<bsValue.length){
				//最新数组大于原来数组, 重新分配长度
				allocateBuffer(bsValue);
			}else if(len>bsValue.length){
				//最新数组小于原来数组  截取原长度的一部分 作为本次数据存放,不需要重新分配内存
				buffer.position(1);
				buffer.putInt(bsValue.length);
				len=dataIndex+bsValue.length*databuffer;
				buffer.position(0);
				buffer.limit(len);
				buffer=buffer.slice();
			}
			//若相等，数据进行覆盖即可;
		}
		//设置起始位置  
		buffer.position(dataIndex);
		buffer.put(bsValue);
		buffer.flip();
	}	
	//引用对象,所有在外部创建并传入使用的对象在此声明并提供set方法-----------------------------------------------

	//内部方法,所有仅在本类或派生类中使用的函数在此定义为protected方法-------------------------------------------

	//公共方法,所有可提供外部使用的函数在此定义为public方法------------------------------------------------------
	public void toAvro(String prefixField,Map<CharSequence,ByteBuffer> fieldMap){				
		fieldMap.put(prefixField+this.getName(), buffer);
	}
	
	public void toXML(StringBuilder strbXML)
	{			
		strbXML.append("<BYAF N=\"").append(this.getName()).append("\"");;
		strbXML.append(" V=\"");
		//拆分成2段，循环一半，对数据量多的平均速度提高15-20%
		byte[] bysValue=getValue();
		int length=bysValue.length;
		int mid=length/2;								
		StringBuilder tmpBuild=new StringBuilder(mid);
		int j=mid+1;
		int tmpLength=length-1;//用于判断是否需要添加逗号
		for(int i=0;i<=mid ;i++){
			strbXML.append(bysValue[i]);
			if(i<tmpLength)
				strbXML.append(",");					
			if(j<length){
				tmpBuild.append(bysValue[j]);
				j++;
				if(j<length)
					tmpBuild.append(",");	
			}
		}
		strbXML.append(tmpBuild);			
		strbXML.append("\"/>");		
	}
	public void toXMLWithIndent(int nIndentSize,StringBuilder strbXML)
	{
		String strIndent=Utility.makeSameCharString('\t',nIndentSize);
		
		strbXML.append(strIndent).append("<BYAF N=\"").append(this.getName()).append("\"");;
		strbXML.append(" V=\"");
		//拆分成2段，循环一半，对数据量多的平均速度提高15-20%
		byte[] bysValue=getValue();
		int length=bysValue.length;
		int mid=length/2;								
		StringBuilder tmpBuild=new StringBuilder(mid);
		int j=mid+1;
		int tmpLength=length-1;//用于判断是否需要添加逗号
		for(int i=0;i<=mid ;i++){
			strbXML.append(bysValue[i]);
			if(i<tmpLength)
				strbXML.append(",");					
			if(j<length){
				tmpBuild.append(bysValue[j]);
				j++;
				if(j<length)
					tmpBuild.append(",");	
			}
		}
		strbXML.append(tmpBuild);			
		strbXML.append("\"/>\r\n");
	}
	
	public void toXMLLog(StringBuilder strbXML){
		byte[] bysValue=getValue();
		strbXML.append("<BYAF N=\"").append(this.getName()).append("\"");;
		strbXML.append(" V=\"");
		strbXML.append(bysValue);		
		strbXML.append("\"/>");		
	}

	public String toJSON()
	{
		StringBuffer str_JSON=new StringBuffer();
		str_JSON.append("\"").append(this.getName()).append("\"").append(":").append("[");
		byte[] bysValue=getValue();
		int _length=bysValue.length;
		for(int i=0;i<_length;i=i+1)
		{
			String _sign=(i==_length-1)?"":",";
			str_JSON.append("").append(bysValue[i]).append(_sign);
		}
		str_JSON.append("],");
		return str_JSON.toString();
	}	
	
	
	public String toJSONString()
	{
		StringBuffer str_JSON=new StringBuffer();
		byte[] bysValue=getValue();
		str_JSON.append("\\\"").append(this.getName()).append("\\\"").append(":").append("[");
		str_JSON.append("").append(bysValue);
		str_JSON.append("],");
		return str_JSON.toString();
	}

	//接口实现,所有实现接口函数的实现在此定义--------------------------------------------------------------------

	//事件处理,所有重载派生类的事件类方法(一般为on...ed)在此定义-------------------------------------------------

	//事件定义,所有在本类中定义并调用，由派生类实现或重载的事件类方法(一般为on...ed)在此定义---------------------

	//构造函数,所有构造函数在此定义------------------------------------------------------------------------------

	public ByteArrayField(String strName)
	{

		//请在此加入初始化代码,内部对象和属性对象负责创建或赋初值,引用对象初始化为null，初始化完成后在设置各对象之间的关系
		super(strName);
		setType(Data.BYTE_ARRAY);

		allocate(new byte[0]);
	}

	public ByteArrayField(String strName,byte[] bysValue)
	{

		//请在此加入初始化代码,内部对象和属性对象负责创建或赋初值,引用对象初始化为null，初始化完成后在设置各对象之间的关系
		super(strName);
		
		setType(Data.BYTE_ARRAY);
		
		if(bysValue==null)
		{
			bysValue=new byte[0];
		}
		allocate(bysValue); 
	}
	
	ByteArrayField(String strName,ByteBuffer buffer){
		super(strName);		
		
		setType(Data.BYTE_ARRAY);
		
		this.buffer=buffer;
	}
	
}
