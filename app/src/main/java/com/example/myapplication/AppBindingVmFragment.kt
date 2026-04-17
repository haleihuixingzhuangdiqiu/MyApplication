package com.example.myapplication

import androidx.databinding.ViewDataBinding
import com.example.myapplication.framework.BaseBindingVmFragment
import com.example.myapplication.framework.BaseViewModel

/** `:app` 内带 VM 的 DataBinding Fragment 基类：固定本模块 `BR.vm`。 */
abstract class AppBindingVmFragment<VB : ViewDataBinding, VM : BaseViewModel> :
    BaseBindingVmFragment<VB, VM>() {

    override val viewModelBrId: Int = BR.vm
}
