package org.com.code.im.pojo;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserFollowing {
    private long id;
    private long sessionId;
    private String avatar;
    private String userName;
}
