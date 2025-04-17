package org.com.code.im.service.Impl;

import org.com.code.im.mapper.MessageMapper;
import org.com.code.im.pojo.Messages;
import org.com.code.im.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageImpl implements MessageService {
    @Autowired
    private MessageMapper messageMapper;
    @Override
    @Transactional
    public void insertBatchMsg(List<Messages> messages) {
        messageMapper.insertBatchMsg(messages);
    }
}
