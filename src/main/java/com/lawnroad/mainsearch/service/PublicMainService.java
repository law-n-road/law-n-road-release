package com.lawnroad.mainsearch.service;

import com.lawnroad.mainsearch.dto.BoardHomepageDto;
import com.lawnroad.mainsearch.dto.LawyerAdBannerDto;
import com.lawnroad.mainsearch.dto.TopLawyerDto;
import com.lawnroad.template.dto.TemplateDto;

import java.util.List;

public interface PublicMainService {
  // 메인 페이지 메인 베너 조회
  List<LawyerAdBannerDto> getActiveMainBanners();
  
  // 메인 페이지 최신 게시글 5개 조회
  List<BoardHomepageDto> getLatestBoards();
  
  // 인기 상품 10개 조회
  List<TemplateDto> getPopularTemplates();
  
  // 메인 페이지 서브 베너 조회
  List<LawyerAdBannerDto> getActiveSubBanners();
  
  // 랭킹 조회
  List<TopLawyerDto> findTop3LawyersByViews();
}
