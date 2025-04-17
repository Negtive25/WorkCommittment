package org.com.code.im.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Videos {

  private long id;
  private long userId;
  private String title;
  private String url;
  private long views;
  private long likes;
  private long commentCount;
  private String tags;
  private String category;
  private double duration;
  private String description;
  private LocalDateTime createdAt;
  private String status;
  private long reviewerId;
  private LocalDateTime reviewedAt;
  private String reviewNotes;

  public Map toMap() {
    Map map = new HashMap();
    map.put("id", id);
    map.put("userId", userId);
    map.put("title", title);
    map.put("url", url);
    map.put("views", views);
    map.put("likes", likes);
    map.put("tags", tags);
    map.put("category", category);
    map.put("duration", duration);
    map.put("description", description);
    map.put("createdAt", createdAt);

    return map;
  }
}