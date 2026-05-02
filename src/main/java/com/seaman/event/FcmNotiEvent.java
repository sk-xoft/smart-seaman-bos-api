package com.seaman.event;

import com.seaman.model.external.request.FcmMessageRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FcmNotiEvent extends ApplicationEvent {

    private FcmMessageRequest request;

    public FcmNotiEvent(Object source) {
        super(source);
        this.request = (FcmMessageRequest) source;
    }

}
