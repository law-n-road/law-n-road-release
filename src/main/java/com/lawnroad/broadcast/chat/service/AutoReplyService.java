package com.lawnroad.broadcast.chat.service;

import com.lawnroad.broadcast.chat.dto.AutoReplyDTO;

import java.util.List;

public interface AutoReplyService {



    void insertAutoReply(AutoReplyDTO autoReplyDTO);
    List<AutoReplyDTO> findByAutoReply(Long scheduleNo);
    void deleteAutoReply(Long no);
    String findReplyMessage(Long scheduleNo, String keyword);
}
