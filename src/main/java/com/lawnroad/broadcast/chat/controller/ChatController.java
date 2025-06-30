package com.lawnroad.broadcast.chat.controller;

import com.lawnroad.broadcast.chat.dto.AutoReplyDTO;
import com.lawnroad.broadcast.chat.dto.ChatDTO;
import com.lawnroad.broadcast.chat.service.AutoReplyService;
import com.lawnroad.broadcast.chat.service.ChatMongodbSaveService;
import com.lawnroad.broadcast.chat.service.ChatRedisSaveServiceImpl;
import com.lawnroad.broadcast.chat.service.ClovaForbiddenService;
import com.lawnroad.common.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final ChatRedisSaveServiceImpl chatRedisSaveService;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtTokenUtil jwtTokenUtil;
    private final AutoReplyService autoReplyService;
    private final ClovaForbiddenService clovaForbiddenService;
    private final ChatMongodbSaveService chatMongodbSaveService;

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatDTO chatDTO, @Header("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtTokenUtil.parseToken(token);

        String nickname = claims.get("nickname", String.class);
        if (nickname == null || nickname.trim().isEmpty()) {
            nickname = chatDTO.getName()+" 변호사";
        }
        chatDTO.setNickname(nickname);
        chatDTO.setType("ENTER");
        chatDTO.setMessage(nickname + " 님이 접속했습니다.");
        chatDTO.setCreatedAt(LocalDateTime.now());

        messagingTemplate.convertAndSend("/topic/" + chatDTO.getBroadcastNo(), chatDTO);
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatDTO chatDTO, @Header("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtTokenUtil.parseToken(token);

        String nickname = claims.get("nickname", String.class);
       // Long no = claims.get("no", Long.class);
        chatDTO.setNickname(nickname);
        chatDTO.setCreatedAt(LocalDateTime.now());
        //chatDTO.setNo(no);
        chatDTO.setBlind(false);
        chatDTO.setChecked(false);

        // 공지 사항
        if ("NOTICE".equals(chatDTO.getType())) {
            chatDTO.setNickname(nickname);
           // chatDTO.setNo(no);
            chatDTO.setCreatedAt(LocalDateTime.now());

            messagingTemplate.convertAndSend("/topic/" + chatDTO.getBroadcastNo(), chatDTO);
            return;
        }
        if(chatDTO.getType() == null) {
            chatDTO.setType("CHAT"); // 기본값
        }

        // ----------------- AI 욕설/금칙어 검사 -----------------
        String msg = chatDTO.getMessage();

        // Redis 장애시 MongoDB fallback
        try {
            chatRedisSaveService.saveChatMessage(chatDTO);
        } catch (Exception e) {
            chatMongodbSaveService.saveChatMessage(chatDTO);
        }

        messagingTemplate.convertAndSend("/topic/" + chatDTO.getBroadcastNo(), chatDTO);

        // ------- 자동응답 처리 -------
        if (msg != null && msg.startsWith("!")) {
            String keyword = msg.substring("!".length()).trim();

            // !자동응답 명령어 처리
            if (keyword.equals("자동응답")) {
                // 프론트에서 scheduleNo를 꼭 보내주세요!
                Long scheduleNo = chatDTO.getScheduleNo();

                List<AutoReplyDTO> allReplies = autoReplyService.findByAutoReply(scheduleNo);
                if (allReplies != null && !allReplies.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("📢 자동응답 명령어 목록<br>");
                    for (AutoReplyDTO dto : allReplies) {
                        sb.append("!")
                                .append(dto.getKeyword())
                                .append("<br>");
                    }
                    ChatDTO reply = ChatDTO.builder()
                            .broadcastNo(chatDTO.getBroadcastNo())
                            .nickname("AutoReply")
                            .message(sb.toString().trim())
                            .type("AUTO_REPLY")
                            .createdAt(LocalDateTime.now())
                            .build();
                    messagingTemplate.convertAndSend("/topic/" + chatDTO.getBroadcastNo(), reply);
                } else {
                    ChatDTO reply = ChatDTO.builder()
                            .broadcastNo(chatDTO.getBroadcastNo())
                            .nickname("AutoReply")
                            .message("등록된 자동응답 명령어가 없습니다.")
                            .type("AUTO_REPLY")
                            .createdAt(LocalDateTime.now())
                            .build();
                    messagingTemplate.convertAndSend("/topic/" + chatDTO.getBroadcastNo(), reply);
                }
                return;
            }
            String autoReplyMsg = autoReplyService.findReplyMessage(chatDTO.getBroadcastNo(), keyword);
            // 기존 단건 자동응답 처리
            if (autoReplyMsg != null) {
                ChatDTO reply = ChatDTO.builder()
                        .broadcastNo(chatDTO.getBroadcastNo())
                        .nickname("AutoReply")
                        .message(autoReplyMsg)
                        .type("AUTO_REPLY")
                        .createdAt(LocalDateTime.now())
                        .build();
                messagingTemplate.convertAndSend("/topic/" + chatDTO.getBroadcastNo(), reply);
            }
        }

    }

    @GetMapping("/api/client/my-no")
    public ResponseEntity<Long> getMyNo(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtTokenUtil.parseToken(token);
        Long no = claims.get("no", Long.class);
        return ResponseEntity.ok(no);
    }

    @GetMapping("/api/Lawyer/my-no")
    public ResponseEntity<Long> getLawyerMyNo(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtTokenUtil.parseToken(token);
        Long no = claims.get("no", Long.class);
        return ResponseEntity.ok(no);
    }
    // 0.5초마다 최근 미검사 메시지에 대해 금칙어 검사 및 블라인드 처리
    @Scheduled(fixedDelay = 500)
    public void blindBadMessages() {
        List<ChatDTO> uncheckedChats = chatRedisSaveService.findUncheckedMessages(); // blind==false && checked==false
        for (ChatDTO chat : uncheckedChats) {
            boolean isProhibited = clovaForbiddenService.containsProhibitedWords(chat.getMessage());
            if (isProhibited) {
                chat.setBlind(true);
                chat.setMessage("🚨 관리자에 의해 메시지가 가려졌습니다.");
            }
            chat.setChecked(true);
            chatRedisSaveService.updateChatMessage(chat);

            //  프론트에 수정된 메시지 브로드캐스트 (중요)
            if (chat.isBlind()) {
                messagingTemplate.convertAndSend("/topic/" + chat.getBroadcastNo(), chat);
            }
        }
    }



}
