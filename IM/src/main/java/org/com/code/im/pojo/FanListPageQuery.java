package org.com.code.im.pojo;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FanListPageQuery {
    long curPageMaxId;
    long curPageMinId;
    long nextPage;
    Long userId;
}
