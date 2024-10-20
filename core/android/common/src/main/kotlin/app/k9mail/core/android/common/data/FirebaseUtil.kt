package app.k9mail.core.android.common.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.firestore
import com.hungbang.email2018.f.c.SignInConfigs
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

object FirebaseUtil {
    private val TAG: String = "hungnd"

    private var firebaseAnalytics: FirebaseAnalytics? = null
    @SuppressLint("MissingPermission")
    fun init(context: Context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    suspend fun getSignInConfigFromFirebase(domain: String): SignInConfigs? = suspendCancellableCoroutine { continuation ->
        val auth = FirebaseAuth.getInstance()
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val db = Firebase.firestore
                    db.collection("SignInConfig").document(domain)
                        .get()
                        .addOnSuccessListener { document ->
                            Log.d("hungnd", "getSignInConfigFromFirebase: $document")
                            continuation.resume(document.toObject(SignInConfigs::class.java))
                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG, "Error getting documents.", exception)
                            continuation.resume(null)
                        }
                } else {
                    continuation.resume(null)
                }
            }
    }

    fun logEvent(eventName: String, arrData: Array<Pair<String, String>>? = null) {
        val bundle = Bundle().apply {
            arrData?.forEach {
                putString(it.first, it.second)
            }
        }
        firebaseAnalytics?.logEvent(eventName, bundle)
        Log.d("hungnd", "logEvent: $eventName $firebaseAnalytics")

        if(arrData == null){
            FirebaseCrashlytics.getInstance().log("$eventName")
        }else{
            FirebaseCrashlytics.getInstance().log("$eventName $bundle")
        }
        FirebaseCrashlytics.getInstance().recordException(Throwable("log_event"))
    }
}

object FireBaseScreenEvent {
    const val SEND_MAIL = "send_mail"
    const val SEND_MAIL_SUCCESS = "send_mail_success"
    const val SEND_MAIL_FAILED = "send_mail_failed"
    const val VIEW_MESSAGE_DETAIL = "view_mail_detail"
    const val GET_DISCOVERY_SETTING_FROM_FIREBASE_SUCCESS = "got_ds_from_fb"
    const val CLICK_CANCEL_RE_LOGIN = "click_cancel_re_login"
    const val CLICK_RE_LOGIN = "click_re_login"
    const val SHOW_DIALOG_REQUIRE_RE_LOGIN = "show_dialog_re_login"
    const val SAVED_CONFIG_NULL = "saved_config_null"
    const val SAVED_CONFIG_FOUND = "saved_config_found"
    const val WELCOME_SCREEN = "welcome_screen"
    const val WELCOME_SCREEN_SKIP = "welcome_screen_skip"
    const val WELCOME_SCREEN_TRY_AI = "welcome_screen_try_ai"

    const val AI_INTRO = "ai_intro"
    const val AI_INTRO_GENERATE = "ai_intro_screen_generate"
    const val AI_INTRO_SKIP = "ai_intro_screen_skip"

    const val SIGN_IN = "signin_click_"
    const val SIGN_IN_OUTLOOK = "sign_in_outlook_error"
    const val SIGN_IN_SUCCESS = "sign_in_success"

    const val FIND_SERVER_ERROR = "find_server_error"
    const val SERVER_VALIDATION = "server_validation"

    const val ERROR_REFRESH_ACCOUNT = "error_refresh_account"
}

object FireBaseParam{
    const val SIGN_IN_TYPE = "type_mail_server"
    const val SIGN_IN_ERROR = "error_type"
    const val SIGN_IN_RE_LOGIN = "is_re_login_old_mail"
    const val SIGN_IN_ERROR_CONTENT = "error_content"
    const val SIGN_IN_EMAIL = "email_server_not_found"
    const val IS_INCOMING_VALIDATION = "IS_INCOMING_VALIDATION"
    const val ERROR_CONTENT = "ERROR_CONTENT"
}
