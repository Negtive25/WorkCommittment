package org.com.code.im.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Follows {

  private long userId;
  private long fanId;
  private LocalDateTime createdAt;
}
