package org.com.code.im.pojo;

import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class VideoComments {

  private long id;
  private long videoId;
  private long userId;
  private String content;
  private long parentId;
  private LocalDateTime createdAt;
  private long repliesCount;

  public Map toMap() {
    Map map = new HashMap();
    map.put("videoId", videoId);
    map.put("content", content);
    map.put("parentId", parentId);
    return map;
  }
}
