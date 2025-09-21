package org.com.code.im.service;

import org.com.code.im.pojo.Messages;
import org.springframework.stereotype.Service;

import java.util.List;

public interface MessageService {
    void insertBatchMsg(List<Messages> messages);
}
