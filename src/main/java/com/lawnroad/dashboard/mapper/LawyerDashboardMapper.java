package com.lawnroad.dashboard.mapper;

import com.lawnroad.dashboard.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface LawyerDashboardMapper {
    /**
     * 변호사의 오늘 일정 조회 (상담 + 방송)
     * @param userNo 변호사 번호
     * @return 오늘 일정 리스트
     */
    List<TodayScheduleDto> getTodaySchedule(@Param("userNo") Long userNo);
    /**
     * 내일 상담 신청 목록 조회
     * @param userNo 변호사(유저) 번호
     * @return 내일 상담 신청 목록
     */
    List<TomorrowConsultationRequestDto> selectTomorrowConsultationRequests(@Param("userNo") Long userNo);

    List<TomorrowBroadcastDto> selectTomorrowBroadcasts(@Param("date") LocalDate date, @Param("userNo") Long userNo);
  
    List<DailyCountDto> selectWeeklyConsultations(@Param("userNo") Long userNo);
    List<DailyCountDto> selectWeeklyBroadcasts(@Param("userNo") Long userNo);
    
    MonthlyRevenueDto getMonthlyRevenue(Long userNo);
    
    MonthlyTemplateSalesDto getMonthlyTemplateSales(@Param("userNo") Long userNo);
  
  
  /**
   * 거니짱
   * 월별 상담 예약 + 템플릿 판매 수익 조회
   * @param lawyerNo 변호사 번호
   * @return List of MonthlyRevenueDto
   */
  List<MonthlyRevenueDto> selectMonthlySalesRevenue(@Param("lawyerNo") Long lawyerNo);
}
