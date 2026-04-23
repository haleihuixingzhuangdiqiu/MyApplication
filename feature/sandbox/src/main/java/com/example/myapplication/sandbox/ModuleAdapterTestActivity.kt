package com.example.myapplication.sandbox

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.myapplication.navigation.RoutePaths
import com.example.myapplication.mvvm.BaseUiActivity
import com.example.myapplication.sandbox.moduleadapter.MaGridCellModel
import com.example.myapplication.sandbox.moduleadapter.MaGridCellView
import com.example.myapplication.sandbox.moduleadapter.MaRowModel
import com.example.myapplication.sandbox.moduleadapter.MaRowView
import com.example.myapplication.sandbox.moduleadapter.MaTitleModel
import com.example.myapplication.sandbox.moduleadapter.MaTitleView
import com.tory.module_adapter.base.ItemSpace
import com.tory.module_adapter.base.NormalModuleAdapter
import com.tory.module_adapter.utils.dp
import com.tory.module_adapter.views.ModuleEmptyModel
import com.tory.module_adapter.views.ModuleGroupSectionModel

@Route(path = RoutePaths.SANDBOX_MODULE_ADAPTER)
class ModuleAdapterTestActivity : BaseUiActivity() {

    private val listAdapter = NormalModuleAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_module_adapter_test)
        registerViews()
        findViewById<RecyclerView>(R.id.recycler_module_adapter).apply {
            adapter = listAdapter
            layoutManager = listAdapter.getGridLayoutManager(this@ModuleAdapterTestActivity)
        }
        listAdapter.setItems(buildItems())
    }

    private fun registerViews() {
        listAdapter.register {
            MaTitleView(it.context)
        }
        listAdapter.register {
            MaRowView(it.context)
        }
        listAdapter.register(
            gridSize = 4,
            itemSpace = ItemSpace(
                spaceH = 8.dp(this),
                spaceV = 8.dp(this),
                edgeH = 16.dp(this),
            ),
        ) {
            MaGridCellView(it.context)
        }
    }

    private fun buildItems(): List<Any> {
        val ctx = this
        return buildList {
            add(MaTitleModel("NormalModuleAdapter · 全宽标题 / 行 / 四列宫格"))
            add(ModuleEmptyModel(height = 8.dp(ctx)))
            add(MaRowModel("整行说明条（可换成业务 Banner / 表单行等）"))
            add(ModuleEmptyModel(height = 8.dp(ctx)))
            repeat(8) { add(MaGridCellModel(it)) }
            add(ModuleGroupSectionModel())
            add(MaTitleModel("第二组（groupPosition 会重新计数）"))
            add(MaRowModel("分组后的整行"))
            repeat(4) { add(MaGridCellModel(100 + it)) }
        }
    }
}
