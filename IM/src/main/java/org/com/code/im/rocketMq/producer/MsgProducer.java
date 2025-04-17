package org.com.code.im.rocketMq.producer;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.com.code.im.exception.RocketmqException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class MsgProducer {
    @Autowired
    @Qualifier("CustomizedTemplate")
    private RocketMQTemplate producerTemplate;


    public void asyncSendMessage(String topic, String tags, Object content) {
        String msg= JSON.toJSONString(content);

        String destination = topic + ":" + tags;
        //自动实现 producer.setRetryTimesWhenSendFailed(3)最多重新发送3次 这个配置
        producerTemplate.asyncSend(destination, msg, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
            }

            @Override
            public void onException(Throwable throwable) {
                throw new RocketmqException("生产者发送消息失败,消息标签为:"+destination+", 消息体为:"+msg);
            }
        });
    }
}
