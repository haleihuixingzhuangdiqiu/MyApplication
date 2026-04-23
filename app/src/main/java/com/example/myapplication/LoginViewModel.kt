package com.example.myapplication

import androidx.databinding.ObservableField
import com.example.myapplication.mvvm.BaseViewModel
import com.example.myapplication.mvvm.launchPageLoading
import com.example.myapplication.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
) : BaseViewModel() {

    val username: ObservableField<String> = ObservableField("")
    val password: ObservableField<String> = ObservableField("")

    private var _onSuccessFinish: (() -> Unit)? = null

    fun setOnSuccessFinish(block: () -> Unit) {
        _onSuccessFinish = block
    }

    fun submit() {
        launchPageLoading {
            val u = username.get().orEmpty()
            val p = password.get().orEmpty()
            sessionRepository.signIn(u, p)
                .onSuccess {
                    showPageContent()
                    _onSuccessFinish?.invoke()
                }
                .onFailure { e ->
                    showPageContent()
                    showToast(e.message ?: "登录失败")
                }
        }
    }
}
