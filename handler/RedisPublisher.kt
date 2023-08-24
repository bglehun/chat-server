package com.socket.handler

import com.entity.jpa.DmMessage
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service

@Service
class RedisPublisher(
    @Qualifier("redisTemplateForDmMessage") private val redisTemplate: RedisTemplate<String, DmMessage>,
) {
    fun publish(topic: ChannelTopic, message: DmMessage) {
        redisTemplate.convertAndSend(topic.topic, message)
    }
}
