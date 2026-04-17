# 模块与导航约定

## 依赖方向

- **`app`**：集成壳，依赖 `core:common`、`core:framework`、`core:database`、`core:network`、`core:navigation` 及各 `feature:*`。
- **`core:navigation`**：仅 **路由常量**（`RoutePaths`），无业务代码、无 Android UI 依赖以外的负担。
- **`feature:*`**：依赖 `core:*` 与 **`core:navigation`**（需要路由时），**不得** `implementation(project(":feature:其它业务模块"))`。

## 跨模块跳转

- 使用 **`com.example.myapplication.navigation.RoutePaths`** + **ARouter**（`ARouter.getInstance().build(path).navigation()`）。
- **禁止**在 `feature:A` 中 `import com.example.myapplication.featureB.*Activity` 做显式跳转（同 feature 内部页面除外）。

## CC 与路由

- CC 组件名见 `core:common` 的 `CcNames`；打开页面时内部仍应通过 **`RoutePaths`** 与 ARouter 对齐，避免硬编码路径字符串分散。

## 说明

全量编译时 `app` 仍可包含所有 feature；本约定主要约束 **依赖边界与协作方式**，便于后续接 product flavor 或动态特性时少改代码。
