package com.socket.handler

import com.entity.cache.SessionInfo
import com.socket.service.chat.DmSocketService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent

@Component
class WebSocketListener(
    private val dmSocketService: DmSocketService,
) {
    private val logger: Logger = LoggerFactory.getLogger(WebSocketListener::class.java)

    private fun getSessionInfo(event: AbstractSubProtocolEvent?): SessionInfo {
        val userAuthentication = event!!.user as Authentication
        val sessionId = userAuthentication.principal.toString()
        val userId = userAuthentication.credentials.toString()
        return SessionInfo(sessionId, userId)
    }

    @EventListener
    fun handleWebSocketDisconnectListener(event: SessionDisconnectEvent) = if (event.user == null) =
        dmSocketService.removeUserAndRedisMessageLister(getSessionInfo(event))

    @EventListener
    fun handleWebSocketSubscribeListener(event: SessionSubscribeEvent) =
        dmSocketService.addUserAndRedisMessageLister(getSessionInfo(event))

    @EventListener
    fun handleWebSocketUnSubscribeListener(event: SessionUnsubscribeEvent) =
        dmSocketService.removeUserAndRedisMessageLister(getSessionInfo(event))
}
