/**
 * 2015. 9. 14.
 * Copyright by yyh / Hubigo AIAL
 * DataOutputStreamEncoder.java
 */
package com.apolloners.poker.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class DataOutputStreamEncoder extends MessageToByteEncoder<String> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.netty.handler.codec.MessageToByteEncoder#encode(io.netty.channel.
	 * ChannelHandlerContext, java.lang.Object, io.netty.buffer.ByteBuf)
	 */
	@Override
	protected void encode(ChannelHandlerContext ctx, String msg,
			ByteBuf out) throws Exception {
		byte[] bytes = msg.getBytes("UTF-8");
		if (bytes.length > 1 << 16)
			throw new IllegalArgumentException();
		ByteBuf buf = ctx.alloc().buffer(bytes.length);
		
		buf.writeShort((short) bytes.length);
		buf.writeBytes(bytes);
		try	{
			out.writeBytes(buf);
		} finally	{
			buf.release();
		}
	}

}
