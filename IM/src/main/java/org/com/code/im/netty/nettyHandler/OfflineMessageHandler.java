package org.com.code.im.netty.nettyHandler;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;
import org.com.code.im.mapper.MessageMapper;
import org.com.code.im.netty.nettyServer.WebSocketChannelInitializer;
import org.com.code.im.pojo.Messages;
import org.com.code.im.responseHandler.ResponseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Scope("prototype")
public class OfflineMessageHandler extends SimpleChannelInboundHandler<String> {

    @Qualifier("strRedisTemplate")
    @Autowired
    RedisTemplate stringRedisTemplateLong;

    @Autowired
    MessageMapper messageMapper;

    private int count=0;

    private long userId=0;

    private long unreadMessageNumber = 0;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String str) throws Exception {

    }
    /**
     *用户一上线，在他尚未能向服务器发送消息之前，先从redis中获取未读消息，然后推送给用户
     *
     * 大坑!!!!
     * WebSocket 协议升级流程
     * HTTP 升级请求：客户端发送 HTTP 请求，请求将协议升级为 WebSocket。
     * 握手处理：WebSocketServerProtocolHandler 处理握手请求，并触发 HandshakeComplete 事件。
     * 协议切换：握手完成后，连接协议正式切换为 WebSocket，此时才能发送 TextWebSocketFrame 等 WebSocket 帧。
     * 2. 消息发送的时机要求
     * 必须在握手完成后发送消息：如果在握手完成前发送 TextWebSocketFrame，Netty 会认为当前协议仍为 HTTP，导致 UnsupportedOperationException。
     * 事件触发顺序：自定义事件（如用户上线通知）必须在 HandshakeComplete 事件之后触发。
     *
     * 所以这里我离线消息的推送,
     * 1. 我需要用户的userId,所以需要在用户上线时，获取到userBeOnlineAlarm自定义时间之后执行
     * 2. 如果过早发送离线消息,此时还没有升级成WebSocket协议,直接发送TextWebSocketFrame 会发送失效
     *
     * 因此我需要等到这两次事件都完成后,才能发送消息给用户,代码如下
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof WebSocketChannelInitializer.userBeOnlineAlarm) {
            count++;
            userId = (long) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        }else if(evt instanceof WebSocketServerProtocolHandler.HandshakeComplete){
            count++;
        }
        if(count==2){
            List<Messages> messages =queryUnreadMessages(userId);

            /**
             * 先发送用户的全部未读消息数量
             */
            ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(new ResponseHandler(ResponseHandler.SUCCESS,"未读消息数量",unreadMessageNumber))));

            if(messages!=null){
                for (Messages message : messages) {
                    ResponseHandler responseMessage = new ResponseHandler(ResponseHandler.SUCCESS, "聊天消息", message);
                    ctx.channel().write(new TextWebSocketFrame(JSON.toJSONString(responseMessage)));
                }
                //统一刷新缓冲区，减小服务器压力
                ctx.channel().flush();
            }
            /**
             * 进行到这一步后offlineMessageHandler该做的都做了,可以移除掉了
             */
            ctx.fireUserEventTriggered(new WebSocketChannelInitializer.timeToRemoveOfflineMessageHandler());
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }

    /**
     *
     *查询用户未读消息
     * 1. 先查询redis中的unread_message_userId,获取用户对应的unreadMessageId未读消息id集合
     * 如果没有未读消息,则返回空
     *
     * 2. 若有未读消息,则根据获取的unread_messageId集合从redis中的messages,即暂时缓存最新消息的集合，
     *    从中获取未读消息,如果获取完后，发现还有未读消息没有获取，说明那部分未读消息已经同步到数据库了
     *    所以此时需要查询数据库
     *    极端情况下，redis返回的未读消息id集合不为空，但是里面的未读消息元素都为空，此时也要查询数据库
     */
    public List<Messages> queryUnreadMessages(long userId){
        //
        String key = "unread_message_"+userId;
        //获取用户所有未读消息id的集合
        Set<String> unreadMessageIds = stringRedisTemplateLong.opsForZSet().range(key,0,-1);

        /**
         *获取全部未读消息id集合后,删除这个未读消息集合
         *
         * 同步删除（可能阻塞主线程）
         * stringRedisTemplate.delete(key);
         *
         * 异步删除（非阻塞）
         * stringRedisTemplate.unlink(key);
         */
        stringRedisTemplateLong.unlink(key);
        //把每个sess
        if(unreadMessageIds==null||unreadMessageIds.size()==0){
            return null;
        }
        unreadMessageNumber = unreadMessageIds.size();

        //获取未读消息id的集合
        List<String> messageIds = new ArrayList<>(unreadMessageIds);
        /**
         * 也有可能有redis缓存的部分消息已经同步到数据库了
         * 所以我先通过未读消息Id的集合，用管道批量查询redis获取未读的消息体,得到messageList
         */
        List<String> messageList =getMessageListFromRedis(messageIds);
        /**
         * 由于getMessageListFromRedis 方法:
         * 使用 executePipelined 方法批量查询消息。
         * 返回一个 List<String>，其中包含查询到的消息 JSON 字符串，如果某个 messageId 不存在，则对应位置为 null。
         *
         * 所以，如果 messageList 中某个位置为 null，则表示对应 messageId 不存在，需要从数据库中查询
         * 此时就把需要查询的messageId 加入到 unreadMessageIdInDatabase 集合中
         */
        List<Messages> messages = new ArrayList<>();
        List<Long> unreadMessageIdInDatabase = new ArrayList<>();
        for (int i = 0; i < messageList.size(); i++) {
            if(messageList.get(i)!=null){
                Messages message = JSON.parseObject(messageList.get(i), Messages.class);
                messages.add(message);
            }else{
                unreadMessageIdInDatabase.add(Long.parseLong(messageIds.get(i)));
            }
        }
        /**
         * 此时循环遍历后，获取了已经被同步到数据库的未读消息Id，unreadMessageIdInDatabase
         * 所以此时需要从数据库中批量查询未读消息
         *
         * 如果unreadMessageIdInDatabase集合为空，则说明redis缓存中获取的未读消息就是全部未读消息，此时直接返回未读消息即可
         */
        if(unreadMessageIdInDatabase.size()==0)
            return messages;
        List<Messages> messagesInDatabase = messageMapper.queryUnreadMessages(unreadMessageIdInDatabase);
        messages.addAll(messagesInDatabase);
        return messages;
    }

    /**
     *这个方法是拿一个人未读消息id的集合从暂时缓存到redis中的消息集合中拿数据
     *
     * messageIds: 未读消息id的集合
     * messageList: 未读消息的集合
     */
    public List<String> getMessageListFromRedis(List<String> messageIds){
        List<String> messageList = stringRedisTemplateLong.executePipelined(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] key = stringRedisTemplateLong.getStringSerializer().serialize("messages");
                for (String messageId : messageIds) {
                    byte[] messageIdSerialized = stringRedisTemplateLong.getStringSerializer().serialize(messageId);
                    connection.hGet(key, messageIdSerialized);
                }
                return null;
            }
        });
        return messageList;
    }
}
