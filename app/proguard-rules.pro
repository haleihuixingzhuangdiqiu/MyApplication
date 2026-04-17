# 本应用 Release：与 build-logic 中 isMinifyEnabled=true 配套；规则尽量只保留反射/框架所需符号。

# 崩溃栈还原（上传 mapping 到 Play / 自建符号服务）
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Kotlin ---
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.reflect.jvm.internal.**

# --- Jetpack / AndroidX（与 R8 默认合并规则互补）---
-keep public class * extends androidx.fragment.app.Fragment
-keep class androidx.databinding.** { *; }

# --- Hilt / Dagger（依赖库自带 consumer-rules；此处补常见反射告警）---
-dontwarn com.google.errorprone.annotations.**

# --- ARouter（编译期 javax.lang.model 引用，运行时不存在；R8 需忽略）---
-dontwarn javax.lang.model.element.Element
-dontwarn javax.lang.model.**

-keep public class com.alibaba.android.arouter.routes.** { *; }
-keep public class com.alibaba.android.arouter.facade.** { *; }
-keep class * implements com.alibaba.android.arouter.facade.template.ISyringe { *; }
-keep interface com.alibaba.android.arouter.facade.template.IProvider
-keep class * implements com.alibaba.android.arouter.facade.template.IProvider
-keep class * implements com.alibaba.android.arouter.facade.template.IInterceptor
-keepclassmembers class * {
    @com.alibaba.android.arouter.facade.annotation.Autowired <fields>;
}
-keep @com.alibaba.android.arouter.facade.annotation.Route class * { *; }
-keep @com.alibaba.android.arouter.facade.annotation.Interceptor class * { *; }

# --- CC（运行时注册组件，需保留实现类）---
-keep class com.billy.cc.** { *; }
-keep class * implements com.billy.cc.core.component.IDynamicComponent { *; }

# --- Retrofit + OkHttp + Gson ---
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
# 网络 DTO 包（Gson 反序列化）
-keep class com.example.myapplication.network.dto.** { *; }

# --- Coil（图片加载器子类等）---
-keep class coil.decode.** { *; }
-dontwarn coil.**

# --- Lottie ---
-dontwarn com.airbnb.lottie.**

# --- Timber（Release 常 no-op，仍避免被误删）---
-keep class timber.log.** { *; }

# --- ModuleAdapter（列表注册若走反射需类名；保守保留库包）---
-keep class com.tory.module_adapter.** { *; }
