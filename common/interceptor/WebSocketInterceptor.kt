package com.aimed.signalschat.socket.common.interceptor

import com.aimed.signalschat.common.exception.CustomError
import com.aimed.signalschat.common.exception.CustomException
import com.aimed.signalschat.component.token.request.JwtValidationRequestClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component

@Component
class WebSocketInterceptor(
    private val jwtValidationRequestClient: JwtValidationRequestClient,
    @Value("\${auth.jwt.validate.enable}") private val tokenValidateEnable: Boolean,
) : ChannelInterceptor {
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val headerAccessor =
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java) ?: throw CustomException(
                CustomError.INVALID_MESSAGE_FORMAT,
            )

        if (headerAccessor.command == StompCommand.CONNECT) {
            val userId = if (tokenValidateEnable) {
                val token = headerAccessor.getFirstNativeHeader("token")
                if (token.isNullOrEmpty()) throw CustomException(CustomError.INVALID_PARAMETER)
                jwtValidationRequestClient.validation(token)
            } else {
                headerAccessor.getFirstNativeHeader("senderId")
            }

            val sessionId = headerAccessor.sessionId

            if (sessionId.isNullOrEmpty() || userId.isNullOrEmpty()) {
                throw CustomException(
                    CustomError.INVALID_MESSAGE_FORMAT,
                )
            }

            headerAccessor.user = UsernamePasswordAuthenticationToken(sessionId, userId, null)
        }

        return message
    }
}
