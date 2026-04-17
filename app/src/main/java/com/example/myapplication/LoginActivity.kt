package com.example.myapplication

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.myapplication.databinding.ActivityLoginBinding
import com.example.myapplication.navigation.RoutePaths
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@Route(path = RoutePaths.LOGIN)
@AndroidEntryPoint
class LoginActivity : AppBindingVmActivity<ActivityLoginBinding, LoginViewModel>() {

    override val layoutId: Int = R.layout.activity_login

    override val viewModel: LoginViewModel by viewModels()

    override fun onVmBound(savedInstanceState: Bundle?) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        viewModel.setOnSuccessFinish { finish() }

        binding.editPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.submit()
                true
            } else {
                false
            }
        }

        binding.editPassword.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                (v as? TextInputEditText)?.let { hideKeyboard(it) }
            }
        }
    }

    private fun hideKeyboard(view: android.view.View) {
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
