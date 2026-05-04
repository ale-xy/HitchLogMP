package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography

/**
 * Reusable Material 3 dialog for confirmations (logout, delete, etc.)
 *
 * @param visible Whether the dialog is visible
 * @param onDismiss Called when the dialog is dismissed (via scrim or cancel)
 * @param title Dialog title text
 * @param message Dialog message text
 * @param confirmLabel Confirm button label
 * @param cancelLabel Cancel button label
 * @param onConfirm Called when the confirm button is clicked
 * @param icon Optional leading icon (e.g., delete icon)
 * @param isDestructive Whether the confirm action is destructive (uses error color)
 */
@Composable
fun HLConfirmationDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    icon: ImageVector? = null,
    isDestructive: Boolean = false
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            modifier = Modifier
                .width(312.dp)
                .background(
                    color = HLColors.SurfaceContainerLow,
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Icon (optional)
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) HLColors.Error else HLColors.OnSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.height(16.dp))
            }

            // Title
            Text(
                text = title,
                style = HLTypography.titleLarge,
                color = HLColors.OnSurface
            )

            Spacer(Modifier.height(16.dp))

            // Message
            Text(
                text = message,
                style = HLTypography.bodyMedium,
                color = HLColors.OnSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel button
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = HLColors.Primary
                    )
                ) {
                    Text(
                        text = cancelLabel,
                        style = HLTypography.labelLarge.copy(fontWeight = FontWeight.Medium)
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Confirm button
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDestructive) HLColors.Error else HLColors.Primary,
                        contentColor = if (isDestructive) HLColors.OnError else HLColors.OnPrimary
                    ),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = confirmLabel,
                        style = HLTypography.labelLarge.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun ConfirmationDialogDestructivePreview() {
    HLTheme {
        HLConfirmationDialog(
            visible = true,
            onDismiss = { },
            title = "Удалить хронику?",
            message = "Хроника «Москва → Санкт-Петербург» и все её записи будут удалены без возможности восстановления.",
            confirmLabel = "Удалить",
            cancelLabel = "Отмена",
            onConfirm = { },
            icon = Icons.Default.Delete,
            isDestructive = true
        )
    }
}

@Preview
@Composable
private fun ConfirmationDialogNormalPreview() {
    HLTheme {
        HLConfirmationDialog(
            visible = true,
            onDismiss = { },
            title = "Выйти из аккаунта?",
            message = "Данные хроник сохранятся на сервере. Вы сможете войти снова в любое время.",
            confirmLabel = "Выйти",
            cancelLabel = "Отмена",
            onConfirm = { },
            isDestructive = false
        )
    }
}
