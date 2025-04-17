package org.com.code.im.pojo;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserFanListResponse {
    private List<UserFan> userFanList;
    private FanListPageQuery fanListPageQuery;
}
