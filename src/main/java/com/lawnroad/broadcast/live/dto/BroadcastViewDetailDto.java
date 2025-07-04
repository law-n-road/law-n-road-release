package com.lawnroad.broadcast.live.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
// 방송에서 정보출력을 위한 DTO
public class BroadcastViewDetailDto {
    private Long scheduleNo;
    private String title;            // 방송 제목
    private String categoryName;     // 카테고리
    private List<String> keywords;   // 키워드
    private Long userNo;             // 변호사 no
    private String lawyerName;       // 변호사 이름
    private String lawyerProfilePath;// 프로필 이미지 경로
    private LocalDate date;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}