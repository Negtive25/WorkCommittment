package org.com.code.im.pojo;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PrivateMemberQueryHandler {
    private long sessionId;
    private long userId;
    private String userName;
    private String avatar;
}
