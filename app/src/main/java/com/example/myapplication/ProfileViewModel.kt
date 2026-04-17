package com.example.myapplication

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.myapplication.framework.BaseViewModel
import com.example.myapplication.session.SessionRepository
import com.example.myapplication.session.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val sessionRepository: SessionRepository,
) : BaseViewModel() {

    private val appContext: Context = context.applicationContext

    val session: LiveData<SessionState> = sessionRepository.state.asLiveData()

    val statusText: LiveData<String> = session.map { s: SessionState ->
        if (s.isLoggedIn) {
            appContext.getString(R.string.profile_logged_in_as, s.displayName.orEmpty())
        } else {
            appContext.getString(R.string.profile_logged_out_short)
        }
    }

    fun onSignOut() {
        viewModelScope.launch {
            sessionRepository.signOut()
        }
    }
}
