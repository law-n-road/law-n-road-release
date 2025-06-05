package com.lawnroad.notification.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// 관심 방송 시작 알림
@Getter
@Setter
@ToString
public class BroadcastStartedDto {
  /*
    관심 방송 시작 알림

    곧 방송이 시작됩니다!
    
    #{name}님, 설정하신 방송 알림 조건에 부합하는 방송이 곧 시작됩니다. 방송을 놓치지 마세요!
    
    📺 방송 제목: #{title}
    🕒 시작 시간: #{start}
    
    이 메시지는 고객님께서 신청하신 관심 방송 시작 알림으로, 설정하신 키워드와 관련된 방송 시작시마다 발송됩니다.
    ※ 알림 수신을 원하지 않으실 경우, [마이페이지 > 방송 알림 관리]에서 언제든지 해제하실 수 있습니다.
  * */
  
  // 수신자 번호
  private String to;
  // 수신자 명
  private String name;
  // 방송 제목
  private String title;
  // 방송 시작 시간
  private String start;
}
