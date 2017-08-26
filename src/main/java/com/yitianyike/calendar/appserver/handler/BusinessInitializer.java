/**
 * 
 */
package com.yitianyike.calendar.appserver.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * @author pineapple
 *
 */
public class BusinessInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        //pipeline.addLast("codec-http", new HttpServerCodec());
        pipeline.addLast(new HttpResponseEncoder());
		pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(104850));//10485760
        pipeline.addLast("socket", new BusinessHandler());
    }
}
