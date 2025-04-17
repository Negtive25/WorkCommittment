package org.com.code.im.pojo;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserFan {
    private long id;
    private String avatar;
    private String userName;
    private long autoIncreasementId;
}
