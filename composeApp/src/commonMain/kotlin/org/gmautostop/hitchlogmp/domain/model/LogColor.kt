package org.gmautostop.hitchlogmp.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = LogColorSerializer::class)
enum class LogColor { BLUE, RED, YELLOW, GREEN, PURPLE, CYAN, BLACK, WHITE }

object LogColorSerializer : KSerializer<LogColor> {
    override val descriptor = PrimitiveSerialDescriptor("LogColor", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LogColor) = encoder.encodeString(value.name)
    override fun deserialize(decoder: Decoder): LogColor =
        runCatching { LogColor.valueOf(decoder.decodeString()) }.getOrDefault(LogColor.BLUE)
}
