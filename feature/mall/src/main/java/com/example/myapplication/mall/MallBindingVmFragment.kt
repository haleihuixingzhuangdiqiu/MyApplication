package com.example.myapplication.mall

import androidx.databinding.ViewDataBinding
import com.example.myapplication.framework.BaseBindingVmFragment
import com.example.myapplication.framework.BaseViewModel

abstract class MallBindingVmFragment<VB : ViewDataBinding, VM : BaseViewModel> :
    BaseBindingVmFragment<VB, VM>() {

    override val viewModelBrId: Int = BR.vm
}
