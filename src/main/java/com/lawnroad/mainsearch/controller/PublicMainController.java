package com.lawnroad.mainsearch.controller;

import com.lawnroad.mainsearch.dto.BoardHomepageDto;
import com.lawnroad.mainsearch.dto.LawyerAdBannerDto;
import com.lawnroad.mainsearch.dto.TopLawyerDto;
import com.lawnroad.mainsearch.service.PublicMainService;
import com.lawnroad.template.dto.TemplateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/main")
@RequiredArgsConstructor
public class PublicMainController {
  
  private final PublicMainService publicMainService;
  
  @GetMapping("/main-banners")
  public List<LawyerAdBannerDto> getMainBanners() {
    return publicMainService.getActiveMainBanners();
  }
  
  @GetMapping("/latest")
  public List<BoardHomepageDto> getLatestBoards() {
    return publicMainService.getLatestBoards();
  }
  
  @GetMapping("/templates/popular")
  public List<TemplateDto> getPopularTemplatesForClient() {
    return publicMainService.getPopularTemplates();
  }
  
  @GetMapping("/sub-banners")
  public List<LawyerAdBannerDto> getSubBanners() {
    return publicMainService.getActiveSubBanners();
  }
  
  @GetMapping("/top-lawyers")
  public List<TopLawyerDto> getTopLawyers() {
    return publicMainService.findTop3LawyersByViews();
  }
}
