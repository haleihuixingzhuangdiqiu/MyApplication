package com.example.myapplication.social

import androidx.databinding.ViewDataBinding
import com.example.myapplication.framework.BaseBindingVmFragment
import com.example.myapplication.framework.BaseViewModel

abstract class SocialBindingVmFragment<VB : ViewDataBinding, VM : BaseViewModel> :
    BaseBindingVmFragment<VB, VM>() {

    override val viewModelBrId: Int = BR.vm
}
