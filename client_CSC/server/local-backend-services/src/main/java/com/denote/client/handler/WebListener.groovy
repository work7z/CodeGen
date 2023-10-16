package com.denote.client.handler

import com.denote.client.core.InitializerLogicFunc
import com.denote.client.handler.GlobalController
import com.denote.client.utils.GlobalFlag
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

@Component
class WebListener implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (GlobalFlag.WAIT_INIT) {
            GlobalController.call(InitializerLogicFunc.class, null);
            GlobalFlag.WAIT_INIT = false;
        }
    }
}
