package com.seaman.service;

import com.seaman.entity.SessionEntity;
import com.seaman.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    public boolean insertSession(SessionEntity entity) {
        return this.sessionRepository.insert(entity);
    }

    public SessionEntity findById(String sessionId){
        return this.sessionRepository.findById(sessionId);
    }

    public boolean updateStatus(SessionEntity entity){
        return this.sessionRepository.updateStatus(entity);
    }

}
