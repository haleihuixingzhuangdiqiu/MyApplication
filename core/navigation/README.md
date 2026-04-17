# `:core:navigation`

- **内容**：`RoutePaths`（ARouter 字符串路径）及本目录约定说明。
- **依赖方**：`app`、各 `feature:*` 需要发起/声明路由时 `implementation(project(":core:navigation"))`。
- **不依赖**：业务实现、Hilt、`framework`（保持最薄，仅常量）。

## 边界约定（方案 D）

1. **`feature` 之间禁止** `implementation(project(":feature:其它模块"))`；共享类型放在 `core:common` 或单独 `api` 模块。
2. **跨业务跳转**只通过 **`RoutePaths` + ARouter**（或等价字符串），**不要** `import com.example.myapplication.xxx.OtherActivity`。
3. **壳模块 `app`** 可依赖 `core:*` 与 `core:navigation` 及各 feature；业务 feature 不反向依赖 `app`。

编译仍可能全量包含各 feature（与 product flavor 无关时），但依赖图与协作约定更清晰。
