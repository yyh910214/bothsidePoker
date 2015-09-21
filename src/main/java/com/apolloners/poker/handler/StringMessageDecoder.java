/**
 * 2015. 9. 11.
 * Copyright by yyh / Hubigo AIAL
 * StringMessageDecoder.java
 */
package com.apolloners.poker.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

/*
 * 나중에 데이터 편향 일어날 경우 사용할 디코더
 */
public class StringMessageDecoder extends ByteToMessageDecoder {

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.netty.handler.codec.ByteToMessageDecoder#decode(io.netty.channel.
	 * ChannelHandlerContext, io.netty.buffer.ByteBuf, java.util.List)
	 */
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		String output;
		short len = in.readShort();
		output = in.readBytes(len).toString(CharsetUtil.UTF_8);

		out.add(output);
	}

}
