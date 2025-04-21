package org.com.code.im.pojo;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GroupMembers {

  private long sessionId;
  private long userId;
  private String nickName;
  @JSONField(format = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime joinedTime;
  private String role;

}
