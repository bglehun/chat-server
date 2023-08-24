package com.socket.service.chat

import com.common.exception.CustomError
import com.common.exception.CustomException
import com.entity.cache.SessionInfo
import com.entity.jpa.DmMessage
import com.repository.cache.DmCacheRepository
import com.repository.jpa.DmMemberJpaRepository
import com.repository.jpa.DmMessageJpaRepository
import com.socket.dto.DmMessageDto
import com.socket.handler.RedisPublisher
import com.socket.handler.RedisSubscriber
import org.slf4j.*
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class DmSocketService(
    private val dmCacheRepository: DmCacheRepository,
    private val dmMessageJpaRepository: DmMessageJpaRepository,
    private val dmMemberJpaRepository: DmMemberJpaRepository,
    private val redisMessageListeners: RedisMessageListenerContainer,
    private val redisSubscriber: RedisSubscriber,
    private val redisPublisher: RedisPublisher,
    private val simpMessageSendingOperations: SimpMessageSendingOperations,
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(DmSocketService::class.java)
    }

    /** 채팅 메세지 mysql 저장 및 redis publish */
    fun broadcastChatMessage(dto: DmMessageDto, accessor: StompHeaderAccessor) {
        /**
         * interceptor에서 access_token 인증 후, header에 저장한 user Authentication
         * principal: sessionId
         * credentials: userId
         * */
        val userAuthentication = accessor.user as Authentication
        val senderId = userAuthentication.credentials as String

        if (dto.type == DmMessage.MessageType.MESSAGE) {
            validationDmMembers(dto, senderId)

            /** rds에 메세지 저장 */
            val message = DmMessageDto.toEntity(dto)
            dmMessageJpaRepository.save(message)

            sendDmMessageToMe(message)
            sendDmMessageToReceiver(message)
        }
    }

    private fun validationDmMembers(dto: DmMessageDto, senderId: String) {
        val dmMembers = dmMemberJpaRepository.findAllByRoomId(dto.roomId)

        val sender = dmMembers.find {
            it.userId == dto.senderId
        }

        val receiver = dmMembers.find {
            it.userId == dto.receiverId
        }

        if (sender == null || receiver == null || senderId != sender.userId) {
            throw CustomException(
                CustomError.INVALID_REQUEST,
            )
        }
    }

    private fun sendDmMessageToMe(message: DmMessage) {
        simpMessageSendingOperations.convertAndSend("/subscribe/user/${message.senderId}", message)
    }

    private fun sendDmMessageToReceiver(message: DmMessage) {
        /** receiver가 socket 연결이 되어 있는지 확인 */
        val receiverSessionId = dmCacheRepository.getSessionIdByUserId(message.receiverId)

        if (receiverSessionId != null) {
            redisPublisher.publish(
                dmCacheRepository.getChannelTopic(
                    message.receiverId,
                ),
                message,
            )
            // TODO: slient push 발송 검토
        } else {
            // TODO: send fcm
//            logger.info("send fcm. receiverId = ${message.receiverId}")
        }
    }

    fun addUserAndRedisMessageLister(sessionInfo: SessionInfo) {
        val (sessionId, userId) = sessionInfo

        redisMessageListeners.addMessageListener(
            redisSubscriber,
            dmCacheRepository.getChannelTopic(userId),
        )
        dmCacheRepository.setSessionIdByUserId(sessionId, userId)
    }

    fun removeUserAndRedisMessageLister(sessionInfo: SessionInfo) {
        val (_, userId) = sessionInfo

        redisMessageListeners.removeMessageListener(
            redisSubscriber,
            dmCacheRepository.getChannelTopic(userId),
        )
        dmCacheRepository.removeSessionIdByUserId(userId)
    }
}
