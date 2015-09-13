/**
 * 2015. 9. 11.
 * Copyright by yyh / Hubigo AIAL
 * StringMessageDecoder.java
 */
package com.apolloners.poker.handler;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/*
 * 나중에 데이터 편향 일어날 경우 사용할 디코더
 */
public class StringMessageDecoder extends ByteToMessageDecoder	{

	/* (non-Javadoc)
	 * @see io.netty.handler.codec.ByteToMessageDecoder#decode(io.netty.channel.ChannelHandlerContext, io.netty.buffer.ByteBuf, java.util.List)
	 */
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		// 필요하면 이부분에 디코딩 하는 과정을 넣자
		out.add((char)in.readByte());
	}
	
}
