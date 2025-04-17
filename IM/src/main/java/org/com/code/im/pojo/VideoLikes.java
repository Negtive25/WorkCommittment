package org.com.code.im.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class VideoLikes {

  private long videoId;
  private long userId;
  private LocalDateTime createdAt;

}
