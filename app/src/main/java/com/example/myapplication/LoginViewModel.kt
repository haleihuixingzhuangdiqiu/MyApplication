package com.example.myapplication

import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.example.myapplication.framework.BaseViewModel
import com.example.myapplication.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

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
        viewModelScope.launch {
            val u = username.get().orEmpty()
            val p = password.get().orEmpty()
            showPageLoading()
            sessionRepository.signIn(u, p)
                .onSuccess {
                    hidePageOverlay()
                    _onSuccessFinish?.invoke()
                }
                .onFailure { e ->
                    hidePageOverlay()
                    showToast(e.message ?: "登录失败")
                }
        }
    }
}
