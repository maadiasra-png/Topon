package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserRole
import com.example.ui.theme.ToponAlertAmber
import com.example.ui.theme.ToponAlertCoral
import com.example.ui.theme.ToponVerifiedGreen
import com.example.ui.viewmodel.AuthSubScreen

@Composable
fun AuthScreen(
    isDarkMode: Boolean,
    authSubScreen: AuthSubScreen,
    authLoading: Boolean,
    authErrorMessage: String?,
    authSuccessMessage: String?,
    mfaRequired: Boolean,
    pendingMfaRole: UserRole?,
    rateLimitSeconds: Int,
    onToggleDarkMode: () -> Unit,
    onSetSubScreen: (AuthSubScreen) -> Unit,
    onClearMessages: () -> Unit,
    onLogin: (identifier: String, passwordPlain: String, rememberMe: Boolean) -> Unit,
    onVerifyMfa: (code: String, rememberMe: Boolean) -> Unit,
    onCancelMfa: () -> Unit,
    onRegister: (
        fullName: String,
        username: String,
        email: String,
        phone: String,
        passwordPlain: String,
        role: UserRole,
        org: String?,
        authCode: String?
    ) -> Unit,
    onRequestReset: (identifier: String) -> Unit,
    onConfirmReset: (identifier: String, resetCode: String, newPasswordPlain: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Login form fields
    var loginIdentifier by remember { mutableStateOf("citizen_sara") }
    var loginPassword by remember { mutableStateOf("Citizen@1403") }
    var isLoginPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    // Register form fields
    var regFullName by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var isRegPasswordVisible by remember { mutableStateOf(false) }
    var regRole by remember { mutableStateOf(UserRole.CITIZEN) }
    var regOrganization by remember { mutableStateOf("") }
    var regAuthCode by remember { mutableStateOf("") }

    // Forgot password fields
    var forgotIdentifier by remember { mutableStateOf("") }

    // Reset password fields
    var resetIdentifier by remember { mutableStateOf("") }
    var resetOtpCode by remember { mutableStateOf("") }
    var resetNewPassword by remember { mutableStateOf("") }
    var isResetPasswordVisible by remember { mutableStateOf(false) }

    // MFA verification field
    var mfaCode by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar: Shield Badge & Dark Mode Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = ToponVerifiedGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "سامانه ملی احراز هویت امن تاپان",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onToggleDarkMode,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.Brightness4 else Icons.Default.Brightness7,
                        contentDescription = "تغییر تم",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // TOPON Logo & Brand Identity
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFF0F172A))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.topon_rescue_icon),
                    contentDescription = "لوگوی TOPON",
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "TOPON • تاپان",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "شبکه هوشمند کمک‌رسانی و پایش سرنخ‌های مفقودین",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rate Limit Warning Banner (if locked out)
            if (rateLimitSeconds > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ToponAlertCoral.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = ToponAlertCoral,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "محدودیت موقت به دلیل تلاش‌های ناموفق مکرر",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ToponAlertCoral
                            )
                            Text(
                                text = "لطفاً تا $rateLimitSeconds ثانیه دیگر منتظر بمانید.",
                                fontSize = 11.sp,
                                color = ToponAlertCoral
                            )
                        }
                    }
                }
            }

            // Error Message Banner (Safe error without leaking internal secrets)
            if (!authErrorMessage.isNullOrBlank() && rateLimitSeconds <= 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ToponAlertCoral.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ToponAlertCoral,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = authErrorMessage,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ToponAlertCoral
                        )
                    }
                }
            }

            // Success Message Banner
            if (!authSuccessMessage.isNullOrBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ToponVerifiedGreen.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ToponVerifiedGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = authSuccessMessage,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ToponVerifiedGreen
                        )
                    }
                }
            }

            // Navigation Tabs (Login / Register) when not on Forgot/Reset password
            if (authSubScreen == AuthSubScreen.LOGIN || authSubScreen == AuthSubScreen.REGISTER) {
                TabRow(
                    selectedTabIndex = if (authSubScreen == AuthSubScreen.LOGIN) 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    Tab(
                        selected = authSubScreen == AuthSubScreen.LOGIN,
                        onClick = { onSetSubScreen(AuthSubScreen.LOGIN) },
                        text = {
                            Text(
                                text = "ورود به حساب",
                                fontSize = 13.sp,
                                fontWeight = if (authSubScreen == AuthSubScreen.LOGIN) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = authSubScreen == AuthSubScreen.REGISTER,
                        onClick = { onSetSubScreen(AuthSubScreen.REGISTER) },
                        text = {
                            Text(
                                text = "ثبت نام جدید",
                                fontSize = 13.sp,
                                fontWeight = if (authSubScreen == AuthSubScreen.REGISTER) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Sub-Screen Content Rendering
            when (authSubScreen) {
                AuthSubScreen.LOGIN -> {
                    LoginCard(
                        identifier = loginIdentifier,
                        password = loginPassword,
                        isPasswordVisible = isLoginPasswordVisible,
                        rememberMe = rememberMe,
                        isLoading = authLoading,
                        isLocked = rateLimitSeconds > 0,
                        onIdentifierChange = { loginIdentifier = it },
                        onPasswordChange = { loginPassword = it },
                        onTogglePasswordVisibility = { isLoginPasswordVisible = !isLoginPasswordVisible },
                        onRememberMeChange = { rememberMe = it },
                        onForgotPasswordClick = {
                            forgotIdentifier = loginIdentifier
                            onSetSubScreen(AuthSubScreen.FORGOT_PASSWORD)
                        },
                        onSubmitLogin = {
                            onLogin(loginIdentifier, loginPassword, rememberMe)
                        },
                        onSelectQuickCredential = { id, pass ->
                            loginIdentifier = id
                            loginPassword = pass
                        }
                    )
                }

                AuthSubScreen.REGISTER -> {
                    RegisterCard(
                        fullName = regFullName,
                        username = regUsername,
                        email = regEmail,
                        phone = regPhone,
                        password = regPassword,
                        isPasswordVisible = isRegPasswordVisible,
                        selectedRole = regRole,
                        organization = regOrganization,
                        authCode = regAuthCode,
                        isLoading = authLoading,
                        onFullNameChange = { regFullName = it },
                        onUsernameChange = { regUsername = it },
                        onEmailChange = { regEmail = it },
                        onPhoneChange = { regPhone = it },
                        onPasswordChange = { regPassword = it },
                        onTogglePasswordVisibility = { isRegPasswordVisible = !isRegPasswordVisible },
                        onRoleChange = { regRole = it },
                        onOrganizationChange = { regOrganization = it },
                        onAuthCodeChange = { regAuthCode = it },
                        onSubmitRegister = {
                            onRegister(
                                regFullName,
                                regUsername,
                                regEmail,
                                regPhone,
                                regPassword,
                                regRole,
                                regOrganization.ifBlank { null },
                                regAuthCode.ifBlank { null }
                            )
                        }
                    )
                }

                AuthSubScreen.FORGOT_PASSWORD -> {
                    ForgotPasswordCard(
                        identifier = forgotIdentifier,
                        isLoading = authLoading,
                        onIdentifierChange = { forgotIdentifier = it },
                        onSubmitRequest = {
                            resetIdentifier = forgotIdentifier
                            onRequestReset(forgotIdentifier)
                        },
                        onBackToLogin = {
                            onSetSubScreen(AuthSubScreen.LOGIN)
                        }
                    )
                }

                AuthSubScreen.RESET_PASSWORD -> {
                    ResetPasswordCard(
                        identifier = resetIdentifier,
                        resetCode = resetOtpCode,
                        newPassword = resetNewPassword,
                        isPasswordVisible = isResetPasswordVisible,
                        isLoading = authLoading,
                        onIdentifierChange = { resetIdentifier = it },
                        onResetCodeChange = { resetOtpCode = it },
                        onNewPasswordChange = { resetNewPassword = it },
                        onTogglePasswordVisibility = { isResetPasswordVisible = !isResetPasswordVisible },
                        onSubmitReset = {
                            onConfirmReset(resetIdentifier, resetOtpCode, resetNewPassword)
                        },
                        onBackToLogin = {
                            onSetSubScreen(AuthSubScreen.LOGIN)
                        }
                    )
                }

                AuthSubScreen.MFA_VERIFY -> {
                    // Handled as dialog below
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer Security Notes
            Text(
                text = "کلیه گذرواژه‌ها به صورت هش سالت‌شده SHA-256 ذخیره شده و اطلاعات هویتی منطبق بر استانداردهای امنیت سایبری و حریم خصوصی رمزنگاری می‌شوند.",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        // Multi-Factor Authentication (MFA) Dialog for Staff & Admin
        if (mfaRequired) {
            AlertDialog(
                onDismissRequest = onCancelMfa,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = ToponAlertAmber,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "احراز هویت دومرحله‌ای (MFA)",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ورود به نقش ${pendingMfaRole?.labelFa ?: "سازمانی"} به دلیل دسترسی به داده‌های حساس و پایگاه ملی مفقودین نیازمند رمز یک‌بارمصرف است:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        OutlinedTextField(
                            value = mfaCode,
                            onValueChange = { if (it.length <= 6) mfaCode = it },
                            label = { Text("کد ۶ رقمی OTP") },
                            placeholder = { Text("مثال: ۱۲۳۴۵۶") },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mfa_otp_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text(
                            text = "کد پیش‌فرض محیط تست: ۱۲۳۴۵۶ (یا هر کد معتبر دیگر)",
                            fontSize = 11.sp,
                            color = ToponVerifiedGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onVerifyMfa(mfaCode.ifBlank { "123456" }, rememberMe)
                        },
                        enabled = !authLoading,
                        modifier = Modifier.testTag("mfa_verify_button")
                    ) {
                        if (authLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("تأیید و ورود")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onCancelMfa) {
                        Text("انصراف")
                    }
                }
            )
        }
    }
}

/**
 * Modern, minimal Login Card
 */
@Composable
private fun LoginCard(
    identifier: String,
    password: String,
    isPasswordVisible: Boolean,
    rememberMe: Boolean,
    isLoading: Boolean,
    isLocked: Boolean,
    onIdentifierChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSubmitLogin: () -> Unit,
    onSelectQuickCredential: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Quick Demo Credentials Selector
            Text(
                text = "انتخاب سریع حساب‌های کاربری (دمو):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickDemoChip(
                    title = "شهروند",
                    roleColor = Color(UserRole.CITIZEN.badgeColorHex),
                    isSelected = identifier.contains("citizen"),
                    modifier = Modifier.weight(1f)
                ) {
                    onSelectQuickCredential("citizen_sara", "Citizen@1403")
                }

                QuickDemoChip(
                    title = "خانواده",
                    roleColor = Color(UserRole.FAMILY.badgeColorHex),
                    isSelected = identifier.contains("family"),
                    modifier = Modifier.weight(1f)
                ) {
                    onSelectQuickCredential("family_rezaei", "Family@1403")
                }

                QuickDemoChip(
                    title = "نهاد مجاز",
                    roleColor = Color(UserRole.AUTHORIZED_STAFF.badgeColorHex),
                    isSelected = identifier.contains("staff"),
                    modifier = Modifier.weight(1f)
                ) {
                    onSelectQuickCredential("staff_police_110", "Staff@1403")
                }

                QuickDemoChip(
                    title = "مدیر سیستم",
                    roleColor = Color(UserRole.ADMIN.badgeColorHex),
                    isSelected = identifier.contains("admin"),
                    modifier = Modifier.weight(1f)
                ) {
                    onSelectQuickCredential("admin_topon", "Admin@1403")
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Email / Username / Phone input
            OutlinedTextField(
                value = identifier,
                onValueChange = onIdentifierChange,
                label = { Text("نام کاربری / ایمیل / شماره تماس") },
                placeholder = { Text("مثال: citizen_sara یا sara@topon.ir") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_identifier_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Password input with visibility toggle
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("رمز عبور") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible) "مخفی‌سازی رمز" else "نمایش رمز"
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Remember Me and Forgot Password in Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onRememberMeChange(!rememberMe) }
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = onRememberMeChange,
                        modifier = Modifier.testTag("remember_me_checkbox")
                    )
                    Text(
                        text = "مرا به خاطر بسپار",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(
                    onClick = onForgotPasswordClick,
                    modifier = Modifier.testTag("forgot_password_button")
                ) {
                    Text(
                        text = "فراموشی رمز عبور؟",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Login Button
            Button(
                onClick = onSubmitLogin,
                enabled = !isLoading && !isLocked && identifier.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_login_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("در حال اعتبارسنجی...")
                } else {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ورود به سامانه TOPON", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Registration Card with strict Staff/Admin Authorization Protection
 */
@Composable
private fun RegisterCard(
    fullName: String,
    username: String,
    email: String,
    phone: String,
    password: String,
    isPasswordVisible: Boolean,
    selectedRole: UserRole,
    organization: String,
    authCode: String,
    isLoading: Boolean,
    onFullNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onRoleChange: (UserRole) -> Unit,
    onOrganizationChange: (String) -> Unit,
    onAuthCodeChange: (String) -> Unit,
    onSubmitRegister: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "انتخاب نوع حساب کاربری:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Role selection buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                UserRole.entries.forEach { role ->
                    val isSelected = selectedRole == role
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) Color(role.badgeColorHex)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { onRoleChange(role) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (role) {
                                UserRole.CITIZEN -> "شهروند"
                                UserRole.FAMILY -> "خانواده"
                                UserRole.AUTHORIZED_STAFF -> "نهاد مجاز"
                                UserRole.ADMIN -> "مدیر"
                            },
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Strict Role Security Warning for Staff and Admin
            if (selectedRole == UserRole.AUTHORIZED_STAFF || selectedRole == UserRole.ADMIN) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ToponAlertAmber.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = ToponAlertAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "حفاظت امنیتی نقش‌های سازمانی و حاکمیتی",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ToponAlertAmber
                            )
                        }
                        Text(
                            text = "نقش‌های Staff و Admin صرفاً با کد امنیتی سازمان یا دعوت‌نامه مدیر مجاز قابل ایجاد هستند. کد تستی دمو: POL-RESCUE-1403 یا ADM-ROOT-1403",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 4.dp),
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            OutlinedTextField(
                value = fullName,
                onValueChange = onFullNameChange,
                label = { Text("نام و نام خانوادگی کامل") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("نام کاربری (انگلیسی)") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("آدرس ایمیل") },
                leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text("شماره تلفن همراه (۰۹...)") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("رمز عبور (حداقل ۶ کاراکتر و ترکیب حرف و عدد)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Extra fields for Staff & Admin
            if (selectedRole == UserRole.AUTHORIZED_STAFF) {
                OutlinedTextField(
                    value = organization,
                    onValueChange = onOrganizationChange,
                    label = { Text("نام ارگان / سازمان رسمی (پلیس، هلال‌احمر، اورژانس)") },
                    leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = authCode,
                    onValueChange = onAuthCodeChange,
                    label = { Text("کد مجوز سازمانی (الزامی: POL-RESCUE-1403)") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            } else if (selectedRole == UserRole.ADMIN) {
                OutlinedTextField(
                    value = authCode,
                    onValueChange = onAuthCodeChange,
                    label = { Text("توکن ریشه مدیر سیستم (الزامی: ADM-ROOT-1403)") },
                    leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Button(
                onClick = onSubmitRegister,
                enabled = !isLoading && fullName.isNotBlank() && username.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_register_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(selectedRole.badgeColorHex))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("ثبت‌نام و صدور مجوز حساب", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Forgot Password Card (/forgot-password)
 */
@Composable
private fun ForgotPasswordCard(
    identifier: String,
    isLoading: Boolean,
    onIdentifierChange: (String) -> Unit,
    onSubmitRequest: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackToLogin) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "بازیابی رمز عبور",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "جهت بازیابی رمز عبور، لطفاً نام کاربری، آدرس ایمیل یا شماره همراه متصل به حساب خود را وارد نمایید. یک کد تأیید امنیتی برای شما صادر خواهد شد.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            OutlinedTextField(
                value = identifier,
                onValueChange = onIdentifierChange,
                label = { Text("نام کاربری / ایمیل / شماره همراه") },
                placeholder = { Text("مثال: citizen_sara یا sara@topon.ir") },
                leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("forgot_identifier_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = onSubmitRequest,
                enabled = !isLoading && identifier.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("send_reset_code_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ارسال کد تأیید بازیابی", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            TextButton(
                onClick = onBackToLogin,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("بازگشت به صفحه ورود", fontSize = 12.sp)
            }
        }
    }
}

/**
 * Reset Password Card (/reset-password)
 */
@Composable
private fun ResetPasswordCard(
    identifier: String,
    resetCode: String,
    newPassword: String,
    isPasswordVisible: Boolean,
    isLoading: Boolean,
    onIdentifierChange: (String) -> Unit,
    onResetCodeChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmitReset: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackToLogin) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "تنظیم رمز عبور جدید",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            OutlinedTextField(
                value = identifier,
                onValueChange = onIdentifierChange,
                label = { Text("نام کاربری / شناسه حساب") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = resetCode,
                onValueChange = onResetCodeChange,
                label = { Text("کد ۶ رقمی تأیید دریافتی") },
                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_otp_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = onNewPasswordChange,
                label = { Text("رمز عبور جدید") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_new_password_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = onSubmitReset,
                enabled = !isLoading && identifier.isNotBlank() && resetCode.isNotBlank() && newPassword.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("confirm_reset_password_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("ذخیره و ورود به سامانه", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Chip for quickly selecting demo credentials
 */
@Composable
private fun QuickDemoChip(
    title: String,
    roleColor: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) roleColor else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
