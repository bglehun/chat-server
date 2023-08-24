package com.aimed.signalschat.socket.controller

import com.aimed.signalschat.socket.dto.DmMessageDto
import com.aimed.signalschat.socket.service.chat.*
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

//    @GetMapping("/user/message")
//    fun sendToUser(
//        @RequestParam sessionId: String,
//    ): ResponseEntity<String> {
//        val message = ChatMessage(
//            roomId = "0001",
//            message = "hi",
//            type = ChatMessage.MessageType.TALK,
//            senderId = "SERVER",
//        )
//        val headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE)
//        if (sessionId != null) headerAccessor.sessionId = sessionId
//        headerAccessor.setLeaveMutable(true)
//        val h = headerAccessor.getMessageHeaders()
//        simpMessageSendingOperations.convertAndSendToUser(
//            sessionId,
//            "/match/1000",
//            message,
//            h,
//        )
//        return ResponseEntity<String>(HttpStatus.ACCEPTED)
//    }
}
