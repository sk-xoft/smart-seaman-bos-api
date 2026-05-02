package com.seaman.service;

import com.seaman.constant.AppSys;
import com.seaman.constant.BusinessConstant;
import com.seaman.entity.MessageCodeEntity;
import com.seaman.repository.MessageCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageCodeService {

    private final MessageCodeRepository messageCodeRepository;

    @Cacheable(cacheNames = BusinessConstant.MASTER_MESSAGE_CODE, key = "#p0")
    public String getMessageDescription(String msgCode, String lang){

        String msgRes = "";

        List<MessageCodeEntity> msgList = messageCodeRepository.findAll();

        for(MessageCodeEntity entity : msgList){
            if(msgCode.equals(entity.getMessageCode())) {
                msgRes = AppSys.LANG_EN.equals(lang) ? entity.getMessageDescriptionEn() : entity.getMessageDescriptionTh();
                break;
            }
        }

        return msgRes.equals("") ? "Message code : " + msgCode + " is not matching in master." : msgRes;
    }
}
