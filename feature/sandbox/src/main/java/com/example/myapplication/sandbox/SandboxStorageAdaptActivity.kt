package com.example.myapplication.sandbox

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.myapplication.framework.BaseUiActivity
import com.example.myapplication.navigation.RoutePaths
import com.example.myapplication.storage.KV
import com.example.myapplication.storage.KvScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 演示 core 层 MMKV 包装和 375 宽屏幕适配结果。
 */
@Route(path = RoutePaths.SANDBOX_STORAGE_ADAPT)
class SandboxStorageAdaptActivity : BaseUiActivity() {

    private lateinit var storageResultText: TextView
    private lateinit var adaptInfoText: TextView
    private lateinit var kv: KvScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sandbox_storage_adapt)

        kv = KV.of(STORE_ID)
        storageResultText = findViewById(R.id.text_storage_result)
        adaptInfoText = findViewById(R.id.text_adapt_info)

        findViewById<Button>(R.id.btn_storage_write).setOnClickListener {
            val timestamp = timeFormatter.format(Date())
            val count = kv.getString(KEY_WRITE_COUNT, "0")?.toIntOrNull()?.plus(1) ?: 1
            kv.putString(KEY_LAST_WRITE, timestamp)
            kv.putString(KEY_WRITE_COUNT, count.toString())
            renderStorageState()
        }
        findViewById<Button>(R.id.btn_storage_read).setOnClickListener {
            renderStorageState()
        }
        findViewById<Button>(R.id.btn_storage_clear).setOnClickListener {
            kv.clear()
            renderStorageState()
        }

        renderStorageState()
        renderAdaptState()
    }

    override fun onResume() {
        super.onResume()
        renderAdaptState()
    }

    private fun renderStorageState() {
        val lastWrite = kv.getString(KEY_LAST_WRITE, null)
        val count = kv.getString(KEY_WRITE_COUNT, "0") ?: "0"
        storageResultText.text = if (lastWrite.isNullOrBlank()) {
            getString(R.string.sandbox_storage_empty)
        } else {
            getString(R.string.sandbox_storage_written, "time=$lastWrite, count=$count")
        }
    }

    private fun renderAdaptState() {
        val dm = resources.displayMetrics
        val calculatedWidthDp = dm.widthPixels / dm.density
        adaptInfoText.text = getString(
            R.string.sandbox_adapt_runtime_info,
            dm.density,
            dm.widthPixels,
            resources.configuration.screenWidthDp,
            calculatedWidthDp,
        )
    }

    private companion object {
        const val STORE_ID = "sandbox_storage_adapt"
        const val KEY_LAST_WRITE = "last_write"
        const val KEY_WRITE_COUNT = "write_count"

        val timeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    }
}
