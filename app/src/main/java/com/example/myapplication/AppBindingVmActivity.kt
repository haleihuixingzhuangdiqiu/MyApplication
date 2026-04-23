package com.example.myapplication

import androidx.databinding.ViewDataBinding
import com.example.myapplication.framework.BaseBindingVmActivity
import com.example.myapplication.mvvm.BaseViewModel

/**
 * `:app` 内 DataBinding 页面基类：固定本模块 `BR.vm`，子类只需 `override val viewModel by viewModels()`。
 */
abstract class AppBindingVmActivity<VB : ViewDataBinding, VM : BaseViewModel> :
    BaseBindingVmActivity<VB, VM>() {

    override val viewModelBrId: Int = BR.vm
}
