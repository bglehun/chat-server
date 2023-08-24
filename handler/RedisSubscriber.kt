package com.socket.handler

import com.entity.jpa.DmMessage
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Service

@Service
class RedisSubscriber(
    private val objectMapper: ObjectMapper,
    @Qualifier("redisTemplateForDmMessage") private val redisTemplate: RedisTemplate<String, DmMessage>,
    private val simpMessageSendingOperations: SimpMessageSendingOperations,
) : MessageListener {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(RedisSubscriber::class.java)
    }

    override fun onMessage(message: Message, pattern: ByteArray?) {
        /** 받은 메시지 역직렬화 */
        val messageString = redisTemplate.stringSerializer.deserialize(message.body)
        val dmMessage = objectMapper.readValue(messageString, DmMessage::class.java)

        simpMessageSendingOperations.convertAndSend("/subscribe/user/${dmMessage.receiverId}", dmMessage)
    }
}
