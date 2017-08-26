/**
 * 
 */
package com.yitianyike.calendar.appserver.handler;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.util.CharsetUtil;

import java.util.Map;

import org.apache.log4j.Logger;

import com.yitianyike.calendar.appserver.common.EnumConstants;

/**
 * @author pineapple Handles handshakes and messages
 */
public class BusinessHandler extends SimpleChannelInboundHandler<HttpObject> {
	private static final Logger logger = Logger.getLogger(BusinessHandler.class);

	private volatile int count = 0;

	@Override
	public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		if (msg instanceof FullHttpRequest) {
			handleHttpRequest(ctx, (FullHttpRequest) msg);
		} else {

		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
		// Handle a bad request.
		if (!req.getDecoderResult().isSuccess()) {
			ResponseGenerator.sendHttpResponse(ctx, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
			return;
		}

		Map<String, String> parmMap = new RequestParser(req).parse(); // 将Header,
																		// GET,
																		// POST所有请求参数转换成Map对象
		if (parmMap.size() == 0) {
			logger.error(EnumConstants.COLOR_RED + "parse param error!" + EnumConstants.COLOR_NONE);
			FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
			ResponseGenerator.sendHttpResponse(ctx, res);
			ctx.close();
			return;
		}
		// System.out.println(parmMap);

		String accessPath = req.getUri();
		if (accessPath.indexOf("?") >= 0) {
			accessPath = accessPath.substring(0, accessPath.indexOf("?"));
		}

		String content = req.content().toString(CharsetUtil.UTF_8);

		if (req.getMethod() == GET || req.getMethod() == POST) {

			if ("/".equals(accessPath)) {
				FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
				ResponseGenerator.sendHttpResponse(ctx, res);
				return;
			} else if ("/webmvc/test".equals(accessPath)) {

				new TestHandler(ctx, parmMap, content).process();
				return;

				//v1
			} else if ("/register".equals(accessPath)) {

				new RegisterHandler(ctx, parmMap, content).process();
				return;

				//v1
			} else if ("/login".equals(accessPath)) {

				new LoginHandler(ctx, parmMap, content).process();
				return;

				//v1
			} else if ("/subscribe".equals(accessPath)) {

				new SubscribeHandler(ctx, parmMap, content).process();
				return;

				//v1
			} else if ("/unSubscribed".equals(accessPath)) {

				new UnSubscribeHandler(ctx, parmMap, content).process();
				return;
				//v1
			} else if ("/allSubscribeList".equals(accessPath)) {

				new AllSubscribeListHandler(ctx, parmMap, content).process();
				return;

				//v1
			} else if ("/recommendSubscribeList".equals(accessPath)) {

				new RecommendSubscribeListHandler(ctx, parmMap, content).process();
				return;

				//v1
			} else if ("/tabsList".equals(accessPath)) {

				new TabsListHandler(ctx, parmMap, content).process();
				return;

				//v1
			} else if ("/subscribedList".equals(accessPath)) {

				new SubscribedListHandler(ctx, parmMap, content).process();
				return;

				//v1
			} else if ("/currentData".equals(accessPath)) {

				new CurrentDataHandler(ctx, parmMap, content).process();
				return;
			} else if ("/incrementData".equals(accessPath)) {

				new IncrementDataHandler(ctx, parmMap, content).process();
				return;

			} else if ("/updateData".equals(accessPath)) {

				new UpdateDataHandler(ctx, parmMap, content).process();
				return;

			} else if ("/updatePeriodData".equals(accessPath)) {

				new UpdatePeriodDataHandler(ctx, parmMap, content).process();
				return;

			} else if ("/synchronizeData".equals(accessPath)) {

				new SynchronizeDataHandler(ctx, parmMap, content).process();
				return;

			} else if ("/getSportsData".equals(accessPath)) {

				new GetSportsDataHandler(ctx, parmMap, content).process();
				return;

			} else if ("/mergeSubscriptions".equals(accessPath)) {
				new MergeSubscriptionsHandler(ctx, parmMap, content).process();
				return;

			} else if ("/unbind".equals(accessPath)) {
				new UnbindHandler(ctx, parmMap, content).process();
				return;

			} else if ("/updatePeriodData".equals(accessPath)) {
				new UpdatePeriodDataHandler(ctx, parmMap, content).process();
				return;

			} else if ("/deleteSubscribedList".equals(accessPath)) {

				new DeleteSubscribedListHandler(ctx, parmMap, content).process();
				return;

			} else if ("/bindPush".equals(accessPath)) {
				new BindPushHandler(ctx, parmMap, content).process();
				return;

			} else if ("/unbindPush".equals(accessPath)) {
				new UnbindPushHandler(ctx, parmMap, content).process();
				return;

			}

			logger.info(EnumConstants.COLOR_RED + "not known uri: " + accessPath + EnumConstants.COLOR_NONE);

			if (count > 0) {
				logger.info("count = " + count);
				ResponseGenerator.sendHttpResponse(ctx, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
				return;
			}
		} else {
			logger.info("req fail!");
		}

		ResponseGenerator.sendHttpResponse(ctx, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
		count++;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error(cause.getMessage(), cause);
		ctx.close();
	}

}
