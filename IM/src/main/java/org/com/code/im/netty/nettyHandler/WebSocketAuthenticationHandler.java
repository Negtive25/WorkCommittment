package org.com.code.im.netty.nettyHandler;


import com.alibaba.fastjson.JSONObject;
import org.com.code.im.responseHandler.ResponseHandler;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.com.code.im.netty.nettyServer.WebSocketChannelInitializer;
import org.com.code.im.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Queue;


@Component
@Scope("prototype")
public class WebSocketAuthenticationHandler extends SimpleChannelInboundHandler<HttpObject> {

    public static final AttributeKey<Long> USER_ID = AttributeKey.valueOf("userId");
    /**
     *  RedisConfig用的是@Configuration注解,同时这里是用它的Bean方法,
     *  由于@Configuration注解,这个类是单例的,所以这个Bean方法得到的是同一个RedisTemplate实例
     *  所以不用担心高并发情况下反复创建和销毁对象造成性能损失
     */
    @Qualifier("redisTemplateLong")
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) throws Exception {
        // 获取请求头的token
        HttpRequest request = (HttpRequest) httpObject;
        String token = request.headers().get("token");
        long userId = 0;

        try{
            // 校验token
            userId=JWTUtils.checkToken(token);
        }catch (Exception e){
            ctx.channel().close();
            return;
        }
        /**
         * 用户上线
         * 把上线用户id及其会话的channel添加到上下文Map中
         * 添加到redis中保存的在线用户id
         */
        String stringUserId=String.valueOf(userId);

        ctx.channel().attr(USER_ID).set(userId);

        /**
         * 在用户id与该会话channel绑定后,马上触发一个事件,之后的
         * MessagesHandler的管道获取这个事件,让它获取用户id进行初始化
         */
        ctx.fireUserEventTriggered(new WebSocketChannelInitializer.userBeOnlineAlarm());

        ChannelCrud.addChannel(userId,ctx);

        List<Channel> channelList = ChannelCrud.onlineUser.get(userId);
        if(channelList.size()>JWTUtils.getMaxOnlineNumber()){
            Channel channel = channelList.get(0);
            channel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(new ResponseHandler(ResponseHandler.ERROR,"一个账号最多"+JWTUtils.getMaxOnlineNumber()+"台设备同时在线,你已被强制下线!!"))))
                    .addListener(future -> {
                        if (future.isSuccess()) {
                            channel.close(); // 消息发送成功后关闭通道
                        } else {
                            future.cause().printStackTrace(); // 打印异常信息
                            channel.close(); // 发送失败后也关闭通道
                        }
                    });
            channelList.remove(0);
        }

        /**
         * 即使是一个账号可以在多台设备上同时在线,建立多个不同的channel连接,但是本质上还只是一个账号在线
         * 所以这里设置成,同一个账号无论在线设备多少个,只有这个账号的第一次与服务器连接的时候才会
         * 往redis中保存自己的用户在线id,只保存1次
         */
        if(channelList.size()==1){
            redisTemplate.opsForHash().put("online_user",stringUserId,userId);
        }

        //如果token验证成功,则传给下一个管道,代表此次的身份验证成功,可以建立WebSocket连接

        //这里一定要再给httpObject引用计数加1,原因如下绿字：
        ReferenceCountUtil.retain(httpObject);
        ctx.fireChannelRead(httpObject);

        /**
         * 假设你的管道中有两个处理器：
         * 管道1：SimpleChannelInboundHandler<T>。
         * 管道2：另一个 SimpleChannelInboundHandler<U> 或其他类型的处理器。
         * 当消息到达 管道1 时：
         * 1.如果消息类型与 管道1 的泛型类型 T 不匹配，SimpleChannelInboundHandler<T> 会调用 ctx.fireChannelRead(msg) 将消息传递给 管道2。
         * 2.此时，管道1 不会释放消息资源，而是将资源释放的任务交给 管道2。
         * 在 管道2 中：
         * 如果 管道2 是一个 SimpleChannelInboundHandler<U>，并且消息类型与 U 匹配，则 管道2 会在处理完消息后**自动**释放资源。
         * 如果 管道2 不是 SimpleChannelInboundHandler，则需要手动释放资源（例如通过 ReferenceCountUtil.release(msg)）。
         */
    }
}
