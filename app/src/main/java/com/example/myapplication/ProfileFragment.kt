package com.example.myapplication

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alibaba.android.arouter.launcher.ARouter
import com.example.myapplication.databinding.FragmentProfileBinding
import com.example.myapplication.navigation.RoutePaths
import com.example.myapplication.session.SessionRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : AppBindingVmFragment<FragmentProfileBinding, ProfileViewModel>() {

    override val layoutId: Int = R.layout.fragment_profile

    override val viewModel: ProfileViewModel by viewModels()

    @Inject
    lateinit var sessionRepository: SessionRepository

    override fun onVmBound(view: View, savedInstanceState: Bundle?) {
        binding.btnGoLogin.setOnClickListener {
            ARouter.getInstance().build(RoutePaths.LOGIN).navigation(requireContext())
        }
        binding.rowSandbox.setOnClickListener {
            ARouter.getInstance().build(RoutePaths.SANDBOX_HUB).navigation(requireContext())
        }
        binding.rowLogout.setOnClickListener {
            viewModel.onSignOut()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionRepository.state.collect { s ->
                    binding.groupGuest.isVisible = !s.isLoggedIn
                    binding.groupLoggedIn.isVisible = s.isLoggedIn
                }
            }
        }
    }
}
