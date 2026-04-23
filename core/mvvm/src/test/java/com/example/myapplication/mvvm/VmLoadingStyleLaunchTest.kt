package com.example.myapplication.mvvm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class VmLoadingStyleLaunchTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** 子类把 protected API 露给单测。 */
    private class VmSubject : BaseViewModel() {
        fun toContent() = showPageContent()
        fun toEmpty() = showPageEmpty("x")
    }

    @Test
    fun none_doesNotTouchLoadingOrPageOverlay() = runTest {
        val vm = VmSubject()
        assertEquals(false, vm.loading.value)
        assertEquals(PageOverlayState.Hidden, vm.pageOverlay.value)
        vm.launch(loadingStyle = VmLoadingStyle.NONE) { delay(5) }
        advanceUntilIdle()
        assertEquals(false, vm.loading.value)
        assertEquals(PageOverlayState.Hidden, vm.pageOverlay.value)
    }

    @Test
    fun inline_setsLoadingTrueWhileRunning_thenFalseInFinally() = runTest {
        val vm = VmSubject()
        val seen = mutableListOf<Boolean>()
        val collector = backgroundScope.launch {
            vm.loading.take(5).collect { seen.add(it) }
        }
        vm.launch(loadingStyle = VmLoadingStyle.INLINE) { delay(5) }
        advanceUntilIdle()
        collector.cancel()
        assertTrue("inline 应出现 loading=true", seen.contains(true))
        assertEquals(false, vm.loading.value)
    }

    @Test
    fun page_startsLoading_resolvedWithShowContent() = runTest {
        val vm = VmSubject()
        vm.launch(loadingStyle = VmLoadingStyle.PAGE) {
            assertTrue(vm.pageOverlay.value is PageOverlayState.Loading)
            delay(5)
            vm.toContent()
        }
        advanceUntilIdle()
        assertEquals(PageOverlayState.Hidden, vm.pageOverlay.value)
    }

    @Test
    fun page_doesNotAutoClearInFinally() = runTest {
        val vm = VmSubject()
        vm.launch(loadingStyle = VmLoadingStyle.PAGE) { delay(5) }
        advanceUntilIdle()
        assertTrue(vm.pageOverlay.value is PageOverlayState.Loading)
    }

    @Test
    fun launchInlineLoading_delegatesToInline() = runTest {
        val vm = VmSubject()
        var seen = false
        backgroundScope.launch { vm.loading.collect { if (it) seen = true } }
        vm.launchInlineLoading { delay(5) }
        advanceUntilIdle()
        assertTrue(seen)
    }

    @Test
    fun launchPageLoading_delegatesToPage() = runTest {
        val vm = VmSubject()
        vm.launchPageLoading {
            delay(5)
            vm.toEmpty()
        }
        advanceUntilIdle()
        assertTrue(vm.pageOverlay.value is PageOverlayState.Empty)
    }
}
