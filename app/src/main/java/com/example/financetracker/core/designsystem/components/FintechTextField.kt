package com.example.financetracker.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.designsystem.FintechColors
import com.example.financetracker.core.designsystem.FintechTypography

@Composable
fun FintechTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = FintechTypography.bodyMedium) },
        placeholder = { Text(placeholder, style = FintechTypography.bodyMedium, color = FintechColors.TextMuted) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        singleLine = singleLine,
        readOnly = readOnly,
        enabled = enabled,
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage, color = FintechColors.OutflowRed, style = FintechTypography.labelSmall) }
        } else null,
        leadingIcon = if (leadingIcon != null) {
            { Icon(leadingIcon, contentDescription = null, tint = FintechColors.TextSecondary) }
        } else null,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FintechColors.Primary,
            unfocusedBorderColor = FintechColors.BorderHairline,
            disabledBorderColor = FintechColors.BorderHairline,
            focusedContainerColor = FintechColors.Surface,
            unfocusedContainerColor = FintechColors.Surface,
            disabledContainerColor = FintechColors.Surface,
            errorContainerColor = FintechColors.Surface,
            focusedTextColor = FintechColors.TextPrimary,
            unfocusedTextColor = FintechColors.TextPrimary,
            disabledTextColor = FintechColors.TextPrimary,
            disabledLabelColor = FintechColors.TextSecondary,
            disabledLeadingIconColor = FintechColors.TextSecondary,
            disabledTrailingIconColor = FintechColors.Primary,
            cursorColor = FintechColors.Primary
        )
    )
}
