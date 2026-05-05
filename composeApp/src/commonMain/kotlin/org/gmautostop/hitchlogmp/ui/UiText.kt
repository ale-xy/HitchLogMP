package org.gmautostop.hitchlogmp.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    data class StringResourceRef(
        val id: StringResource,
        val args: Array<Any> = emptyArray()
    ) : UiText {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as StringResourceRef
            if (id != other.id) return false
            if (!args.contentEquals(other.args)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + args.contentHashCode()
            return result
        }
    }
}

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.DynamicString -> value
    is UiText.StringResourceRef -> stringResource(id, *args)
}

suspend fun UiText.asStringSuspend(): String = when (this) {
    is UiText.DynamicString -> value
    is UiText.StringResourceRef -> getString(id, *args)
}
