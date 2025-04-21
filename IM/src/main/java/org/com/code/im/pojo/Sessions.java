package org.com.code.im.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Sessions {

  private long sessionId;
  private String sessionType;
  private String groupAvatar;
  private String groupName;
  private long ownerId;
  @JSONField(format = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;
}
