package com.lawnroad.template.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LawyerFileTemplateDetailDto {
  
  // 템플릿 고유번호 (PK)
  private Long no;
  
  // 템플릿명
  private String name;
  
  // 가격 (원 단위)
  private Integer price;
  
  // 할인율 (% 단위)
  private Integer discountRate;
  
  // 카테고리 번호 (FK → category)
  private Long categoryNo;
  
  // 템플릿 상세 설명
  private String description;
  
  // 썸네일 이미지 경로
  private String thumbnailPath;
  
  // 템플릿 파일 경로 + 원본명 목록 (JSON 형태 문자열)
  private String pathJson;
  
  // 생성 일시
  private LocalDateTime createdAt;
}