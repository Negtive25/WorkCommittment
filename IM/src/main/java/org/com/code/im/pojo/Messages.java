package org.com.code.im.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Messages {

  /**
   * 用户需要发送的消息格式,举例
   * {
   *     "sequenceId":1,
   *     "sessionId":1,
   *     "content":"hello",
   *     "messageType":"text"
   * }
   */
  private long messageId;
  //维护消息顺序性和去重
  private long sequenceId;
  private long sessionId;
  private long senderId;
  private String content;
  //messageType ENUM('text', 'image', 'file') DEFAULT 'text',
  private String messageType;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
  private LocalDateTime createdAt;
  private long timestamp;
}
