package com.lawnroad.template.service;

import com.lawnroad.payment.dto.OrdersCreateDTO;
import com.lawnroad.payment.service.OrdersService;
import com.lawnroad.template.dto.cart.CartItemResponseDto;
import com.lawnroad.template.dto.cart.CheckoutRequestDto;
import com.lawnroad.template.dto.cart.CheckoutResponseDto;
import com.lawnroad.template.mapper.CartMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

  private final CartMapper cartMapper;
  private final OrdersService ordersService;

  @Override
  public boolean addToCart(Long userNo, Long tmplNo) {
    if (cartMapper.existsByUserAndTemplate(userNo, tmplNo) > 0) {
      return false; // 이미 담겨 있음
    }
    cartMapper.insertCart(userNo, tmplNo);
    return true;
  }

  @Override
  public void removeFromCart(Long cartNo) {
    cartMapper.deleteByCartNo(cartNo);
  }

  @Override
  public List<CartItemResponseDto> findCartItems(Long userNo) {
    return cartMapper.selectCartListByUser(userNo);
  }

  /**
   * userNo 기준으로 장바구니 전체를 결제 → 주문 및 히스토리 생성 → 장바구니 비우기
   * @param dto 결제 요청 유저
   * @return 생성된 orderNo
   */
  @Transactional
  @Override
  public CheckoutResponseDto checkout(CheckoutRequestDto dto) {
    // ① CartItemResponseDto 를 그대로 조회
    List<CartItemResponseDto> cartItems = cartMapper.selectCartListByUser(dto.getUserNo());
    if (cartItems.isEmpty()) {
      throw new IllegalStateException("장바구니가 비어 있습니다.");
    }

    // ② 총 결제액 계산 (할인가 적용)
    int totalAmount = cartItems.stream()
            .mapToInt(item ->
                    (int)(item.getPrice() * (1 - item.getDiscountRate() / 100.0))
            )
            .sum();

    // ③ 주문 생성
    OrdersCreateDTO orderDto = new OrdersCreateDTO();
    orderDto.setUserNo(dto.getUserNo());
    orderDto.setOrderCode("TMP-" +
            UUID.randomUUID().toString().replace("-", "").substring(0, 16)
    );
    orderDto.setAmount((long) totalAmount);
    orderDto.setStatus("ORDERED");
    orderDto.setOrderType("TEMPLATE");
    Long orderNo = ordersService.createOrder(orderDto);


    // ④ 결제 API 호출
//    boolean paid = paymentGateway.charge(dto.getUserNo(), totalAmount, orderNo);
//    if (!paid) {
//      throw new PaymentException("결제에 실패했습니다.");
//    }
    
    CheckoutResponseDto response = new CheckoutResponseDto();
    response.setOrderNo(orderNo);
    response.setOrderCode(orderDto.getOrderCode());
    response.setAmount((long) totalAmount);

    return response;
  }
  
  @Override
  @Transactional
  public void completeOrder(Long userNo, String orderCode) {
    // ① 장바구니 아이템 조회
    List<CartItemResponseDto> cartItems = cartMapper.selectCartListByUser(userNo);
    
    // orderCode 에 해당하는 orderNo 구하기
    Long orderNo = cartMapper.selectOrderNoByOrderCode(orderCode);
    System.out.println(orderNo);
    
    // ② 히스토리 테이블에 복사 + 판매량 증가
    for (CartItemResponseDto item : cartItems) {
      int paidPrice = (int) (item.getPrice() * (1 - item.getDiscountRate() / 100.0));
      cartMapper.insertHistory(item.getTmplNo(), orderNo, paidPrice);
      cartMapper.incrementSalesCount(item.getTmplNo());
    }
    
    // ③ 장바구니 비우기
    cartMapper.deleteByUserNo(userNo);
  }

  @Override
  public void deleteAllByUser(Long userNo) {
    cartMapper.deleteByUserNo(userNo);
  }
}