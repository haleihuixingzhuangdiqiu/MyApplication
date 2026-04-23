package com.example.myapplication.mall

import androidx.databinding.ViewDataBinding
import com.example.myapplication.mvvm.BaseBindingVmActivity
import com.example.myapplication.mvvm.BaseViewModel

/** `feature:mall` 内带 VM 的 DataBinding Activity 基类；约定布局变量名为 `vm`。 */
abstract class MallBindingVmActivity<VB : ViewDataBinding, VM : BaseViewModel> :
    BaseBindingVmActivity<VB, VM>() {

    override val viewModelBrId: Int = BR.vm
}
