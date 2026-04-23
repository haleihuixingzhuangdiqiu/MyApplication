package com.example.myapplication.game

import androidx.databinding.ViewDataBinding
import com.example.myapplication.mvvm.BaseBindingVmFragment
import com.example.myapplication.mvvm.BaseViewModel

/** `feature:game` 内带 VM 的 DataBinding Fragment 基类；约定布局变量名为 `vm`。 */
abstract class GameBindingVmFragment<VB : ViewDataBinding, VM : BaseViewModel> :
    BaseBindingVmFragment<VB, VM>() {

    override val viewModelBrId: Int = BR.vm
}
