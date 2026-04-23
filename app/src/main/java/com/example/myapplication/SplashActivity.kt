package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.example.myapplication.mvvm.BaseUiActivity
import com.example.myapplication.splash.SplashConfigLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 冷启动闪屏：全屏 Lottie，与 [SplashConfigLoader.mockLoadRemoteConfig] 并行；固定展示约 2s 后进入 [MainActivity]。
 *
 * 进程未杀、任务栈里已有主界面时，从桌面再次点图标可能仍会进本页；此时 [isTaskRoot] 为 false，直接回主壳、不重复播闪屏。
 */
class SplashActivity : BaseUiActivity() {

    override val useAutoSize: Boolean = false

    override val shouldApplyRootStatusBarInsets: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (!isTaskRoot) {
            navigateToMainSkippingSplash()
            return
        }

        setContentView(R.layout.activity_splash)
        findViewById<LottieAnimationView>(R.id.splash_lottie).playAnimation()

        lifecycleScope.launch {
            launch {
                try {
                    SplashConfigLoader.mockLoadRemoteConfig()
                } catch (_: Exception) {
                    // mock 失败不阻塞进入主壳
                }
            }
            delay(SPLASH_DISPLAY_MS)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            // 覆盖系统默认的左右 Activity 切换，改为淡入淡出
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
            finish()
        }
    }

    private fun navigateToMainSkippingSplash() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
        )
        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }

    private companion object {
        private const val SPLASH_DISPLAY_MS = 2000L
    }
}
