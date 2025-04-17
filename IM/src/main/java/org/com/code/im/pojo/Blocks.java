package org.com.code.im.pojo;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Blocks {

  private long blockerId;
  private long blockedId;
  private String blockedName;
  private LocalDateTime blockedAt;

}
