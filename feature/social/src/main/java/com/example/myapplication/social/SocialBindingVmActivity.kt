package com.example.myapplication.social

import androidx.databinding.ViewDataBinding
import com.example.myapplication.framework.BaseBindingVmActivity
import com.example.myapplication.framework.BaseViewModel

/** `feature:social` 内带 VM 的 DataBinding Activity 基类；约定布局变量名为 `vm`。 */
abstract class SocialBindingVmActivity<VB : ViewDataBinding, VM : BaseViewModel> :
    BaseBindingVmActivity<VB, VM>() {

    override val viewModelBrId: Int = BR.vm
}
