package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConditionType
import com.example.ui.theme.ToponAlertCoral
import com.example.ui.theme.ToponVerifiedGreen

@Composable
fun ConditionBadge(condition: ConditionType, modifier: Modifier = Modifier) {
    val bg = Color(condition.badgeColorHex).copy(alpha = 0.16f)
    val border = Color(condition.badgeColorHex).copy(alpha = 0.6f)
    val text = Color(condition.badgeColorHex)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(text)
        )
        Text(
            text = condition.label,
            color = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 5.dp)
        )
    }
}

@Composable
fun VerificationBadge(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ToponVerifiedGreen.copy(alpha = 0.15f))
            .border(1.dp, ToponVerifiedGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "تأیید رسمی",
            tint = ToponVerifiedGreen,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = "تأیید رسمی پلیس و امداد",
            color = ToponVerifiedGreen,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun VerificationBadge(status: com.example.data.model.VerificationStatus, modifier: Modifier = Modifier) {
    val (color, label) = when (status) {
        com.example.data.model.VerificationStatus.VERIFIED -> ToponVerifiedGreen to "تأیید رسمی مراجع"
        com.example.data.model.VerificationStatus.UNDER_INVESTIGATION -> Color(0xFF0EA5E9) to "در حال تحقیق میدانی"
        com.example.data.model.VerificationStatus.PENDING_REVIEW -> Color(0xFFD97706) to "در انتظار بررسی مأمور"
        com.example.data.model.VerificationStatus.RESOLVED -> Color(0xFF2563EB) to "مختومه / امدادرسانی شده"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun PrivacyEncryptionBadge(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "رمزنگاری امن",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = "حفظ حریم خصوصی و هش محلی",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
