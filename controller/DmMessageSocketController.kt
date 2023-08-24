package com.socket.controller

import com.socket.dto.DmMessageDto
import com.socket.service.chat.*
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.*
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.*

@Controller
class DmMessageSocketController(
    private val dmSocketService: DmSocketService,
) {
    @MessageMapping("/dm/message")
    fun message(message: DmMessageDto, accessor: StompHeaderAccessor) =
        dmSocketService.broadcastChatMessage(message, accessor)

}
