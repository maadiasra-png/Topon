package com.example.data.api

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.MissingPersonCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiAnalysisService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeSightingWithGemini(
        bitmap: Bitmap?,
        targetCase: MissingPersonCase,
        audioNotes: String
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d("GeminiService", "Gemini API key is not configured; using offline biometric engine.")
            return@withContext null
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val prompt = """
                شما سیستم هوش مصنوعی شبکه امداد و جستجوی تاپان (TOPON) هستید.
                اطلاعات پرونده فرد گمشده:
                نام: ${targetCase.fullName}
                سن: ${targetCase.age}
                وضعیت: ${targetCase.conditionType.label}
                مشخصات فیزیکی: قد ${targetCase.physicalAttributes.heightCm} سانتی‌متر، جثه: ${targetCase.physicalAttributes.buildType}
                فاصله چشم و هندسه چهره: ${targetCase.physicalAttributes.eyeDistanceRatio}
                پوشش: ${targetCase.physicalAttributes.clothingDescription}
                یادداشت صوتی/رفتاری گزارشگر: $audioNotes

                تصویر پیوست شده از مشاهده میدانی را ارزیابی کرده و موارد زیر را به صورت شفاف به فارسی گزارش دهید:
                ۱. برآورد درصد تشابه بیومتریک و چهره
                ۲. ارزیابی هماهنگی فیزیکی و فاصله چشم و قامت
                ۳. هشدارهای اضطراری و راهنمای برخورد محترمانه متناسب با شرایط سلامتی فرد
            """.trimIndent()

            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", prompt))

            if (bitmap != null) {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                val base64Data = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

                val inlineDataObj = JSONObject()
                    .put("mimeType", "image/jpeg")
                    .put("data", base64Data)

                partsArray.put(JSONObject().put("inlineData", inlineDataObj))
            }

            val contentObj = JSONObject().put("parts", partsArray)
            val contentsArray = JSONArray().put(contentObj)

            val requestJson = JSONObject()
                .put("contents", contentsArray)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(endpoint)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w("GeminiService", "Gemini call failed with code: ${response.code}")
                return@withContext null
            }

            val responseBody = response.body?.string() ?: return@withContext null
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")
            text
        } catch (e: Exception) {
            Log.e("GeminiService", "Error calling Gemini API: ${e.message}")
            null
        }
    }
}
