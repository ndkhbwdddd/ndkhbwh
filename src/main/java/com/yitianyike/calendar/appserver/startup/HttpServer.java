/**
 * 
 */
package com.yitianyike.calendar.appserver.startup;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.apache.log4j.Logger;

import com.yitianyike.calendar.appserver.handler.BusinessInitializer;
import com.yitianyike.calendar.appserver.service.ApplicationConfig;
import com.yitianyike.calendar.appserver.util.PropertiesUtil;

/**
 * @author xujinbo
 * 
 */
public class HttpServer implements Server {

	private static Logger log = Logger.getLogger(HttpServer.class.getName());

	private static int businessPort;

	/*
	 * (non-Javadoc)
	 */
	public void init() {

		String envPort = System.getenv("PORT_BUSINESS");
		if (null != envPort) {
			businessPort = Integer.parseInt(envPort);
		}
		businessPort = PropertiesUtil.httpPort;
		
		if (businessPort <= 0) {
			businessPort = 8080;
		}

		ApplicationConfig.init();
	}

	/*
	 * (non-Javadoc)
	 */
	public void start() throws Exception {
		startBusiness();
	}

	private void startBusiness() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).childOption(ChannelOption.TCP_NODELAY, true)
					.childOption(ChannelOption.SO_KEEPALIVE, true)
					.channel(NioServerSocketChannel.class)
					.childHandler(new BusinessInitializer());

			//Channel ch = b.bind(businessPort).sync().channel();
			b.bind(businessPort).sync();
			System.out.println("socket server started at port "
					+ businessPort + '.');
			System.out
					.println("Open your browser and navigate to http://localhost:"
							+ businessPort + '/');
			//ch.closeFuture().sync();
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	public void stop() {

	}

	public void status() {
		log.info("Show Status");
	}
}
