package com.example.myapplication.sandbox

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.mvvm.collectFlows
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.myapplication.common.toast.showError
import com.example.myapplication.common.toast.showInfo
import com.example.myapplication.framework.adapter.setItemsWithStableItemDiff
import com.example.myapplication.framework.disableItemChangeAnimations
import com.example.myapplication.sandbox.moduleadapter.MaRowModel
import com.example.myapplication.sandbox.moduleadapter.MaRowView
import com.example.myapplication.mvvm.BaseUiActivity
import com.example.myapplication.mvvm.UiUserMessage
import com.example.myapplication.navigation.RoutePaths
import com.example.myapplication.mvvm.paging.RefreshListHost
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tory.module_adapter.base.NormalModuleAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 沙箱：[:core:mvvm] 中 [com.example.myapplication.mvvm.paging.BasePagedViewModel] +
 * [RefreshListHost] + [NormalModuleAdapter] 的标准接法。业务可换 [loadPage] 与 `register` 的 ItemView。
 */
@Route(path = RoutePaths.SANDBOX_PAGED_REFRESH)
@AndroidEntryPoint
class SandboxPagedRefreshActivity : BaseUiActivity() {

    override val standaloneShellLayoutId: Int = R.layout.activity_sandbox_paged_refresh_shell
    override val standaloneToolbarId: Int = R.id.toolbar

    private val viewModel: SandboxPagedRefreshViewModel by viewModels()

    private val listAdapter = NormalModuleAdapter(calDiff = true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.sandbox_paged_refresh_title)

        val refresh = findViewById<SmartRefreshLayout>(R.id.sandbox_paged_refresh)
        val recycler = findViewById<RecyclerView>(R.id.sandbox_paged_recycler)
        listAdapter.register { MaRowView(it.context) }
        recycler.layoutManager = listAdapter.getGridLayoutManager(this)
        recycler.disableItemChangeAnimations()
        val host = RefreshListHost(refresh, recycler)
        host.installDefaults(viewModel.pageConfig)
        host.bind(viewModel, this)
        recycler.adapter = listAdapter

        collectFlows {
            viewModel.items.onValue { list ->
                val rows: List<Any> = list.map { MaRowModel(it.title) }
                listAdapter.setItemsWithStableItemDiff(
                    newItems = rows,
                    stableItemId = { item ->
                        val m = item as MaRowModel
                        val i = rows.indexOf(m)
                        if (i >= 0) list[i].id else m.label
                    },
                )
            }
            viewModel.userMessage.onValue { ev ->
                ev.getContentIfNotHandled()?.let { msg ->
                    when (msg) {
                        is UiUserMessage.ErrorMessage -> msg.errorMessage.showError()
                        is UiUserMessage.InfoMessage -> msg.text.showInfo()
                    }
                }
            }
        }

        if (savedInstanceState == null) {
            lifecycleScope.launch {
                viewModel.runRefresh()
            }
        }
    }
}
