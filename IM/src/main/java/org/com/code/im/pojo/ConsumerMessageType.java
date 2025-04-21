package org.com.code.im.pojo;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerMessageType {
    long sessionId;
    long userId;
    String type;
}
