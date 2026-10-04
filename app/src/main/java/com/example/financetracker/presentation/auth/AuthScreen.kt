package com.example.financetracker.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financetracker.R
import com.example.financetracker.core.datastore.UserPreferences
import com.example.financetracker.core.designsystem.FintechColors
import com.example.financetracker.core.designsystem.FintechTypography
import com.example.financetracker.core.util.Formatters
import com.example.financetracker.core.designsystem.components.FintechButton
import com.example.financetracker.core.designsystem.components.FintechCard
import com.example.financetracker.core.designsystem.components.FintechTextField
import com.example.financetracker.data.repository.AuthRepository
import com.example.financetracker.data.repository.FinanceRepository
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    authRepository: AuthRepository,
    financeRepository: FinanceRepository,
    userPreferences: UserPreferences,
    onAuthSuccess: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FintechColors.Background)
    ) {
        // Decorative Top Header (Blue curve aesthetic)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(FintechColors.Primary),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Finance Tracker Logo",
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Finance Tracker",
                    style = FintechTypography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = FintechColors.TextInverse,
                        letterSpacing = 0.5.sp
                    )
                )
                Text(
                    text = if (isRegisterMode) "Buat akun keluarga baru" else "Masuk ke akun keuangan Anda",
                    style = FintechTypography.bodyMedium.copy(
                        color = FintechColors.TextInverse.copy(alpha = 0.85f)
                    )
                )
            }
        }

        // Form Card Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 210.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FintechCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 24.dp
            ) {
                Text(
                    text = if (isRegisterMode) "Daftar Akun" else "Masuk",
                    style = FintechTypography.titleLarge,
                    color = FintechColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(18.dp))

                if (isRegisterMode) {
                    FintechTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Nama Lengkap",
                        placeholder = "e.g. Dionisius",
                        leadingIcon = Icons.Rounded.Person
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                FintechTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Alamat Email",
                    placeholder = "nama@email.com",
                    leadingIcon = Icons.Rounded.Email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(12.dp))

                FintechTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = "Kata Sandi",
                    placeholder = "Minimal 8 karakter",
                    leadingIcon = Icons.Rounded.Lock,
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = if (isPasswordVisible) "Sembunyikan kata sandi" else "Tampilkan kata sandi",
                                tint = FintechColors.TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                if (isRegisterMode) {
                    Spacer(modifier = Modifier.height(12.dp))

                    FintechTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = "Konfirmasi Kata Sandi",
                        placeholder = "Ulangi kata sandi",
                        leadingIcon = Icons.Rounded.Lock,
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = if (isConfirmPasswordVisible) "Sembunyikan konfirmasi kata sandi" else "Tampilkan konfirmasi kata sandi",
                                    tint = FintechColors.TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage.orEmpty(),
                        style = FintechTypography.bodySmall,
                        color = FintechColors.OutflowRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                FintechButton(
                    text = if (isRegisterMode) "Buat Akun" else "Masuk Sekarang",
                    isLoading = isLoading,
                    onClick = {
                        if (isRegisterMode) {
                            if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                errorMessage = "Mohon lengkapi semua kolom"
                                return@FintechButton
                            }
                            if (password.length < 8) {
                                errorMessage = "Kata sandi minimal 8 karakter"
                                return@FintechButton
                            }
                            if (password != confirmPassword) {
                                errorMessage = "Konfirmasi kata sandi tidak cocok"
                                return@FintechButton
                            }
                        } else {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Mohon lengkapi email dan kata sandi"
                                return@FintechButton
                            }
                        }

                        errorMessage = null
                        isLoading = true

                        coroutineScope.launch {
                            val result = if (isRegisterMode) {
                                authRepository.register(name, email, password, confirmPassword)
                            } else {
                                authRepository.login(email, password)
                            }

                            isLoading = false
                            if (result.isSuccess) {
                                // Trigger initial sync after login
                                launch { financeRepository.performSync() }
                                onAuthSuccess()
                            } else {
                                errorMessage = Formatters.humanizeError(result.exceptionOrNull())
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isRegisterMode) "Sudah punya akun?" else "Belum memiliki akun?",
                        style = FintechTypography.bodyMedium,
                        color = FintechColors.TextSecondary
                    )
                    TextButton(onClick = {
                        isRegisterMode = !isRegisterMode
                        errorMessage = null
                        password = ""
                        confirmPassword = ""
                        isPasswordVisible = false
                        isConfirmPasswordVisible = false
                    }) {
                        Text(
                            text = if (isRegisterMode) "Masuk" else "Daftar",
                            style = FintechTypography.labelLarge,
                            color = FintechColors.Primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

            }
        }
    }
}
