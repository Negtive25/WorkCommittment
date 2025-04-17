package org.com.code.im.pojo;

import lombok.Data;

@Data
public class CreateSessionOrInviteRequest {
    private Long ownerId;
    private Long[] userIds;
    private Sessions session;
    private String requestType;
}
