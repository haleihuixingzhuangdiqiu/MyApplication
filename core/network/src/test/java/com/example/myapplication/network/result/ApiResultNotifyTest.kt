package com.example.myapplication.network.result

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiResultNotifyTest {

    @Test
    fun failureMessageOrNull_successReturnsNull() {
        assertNull(ApiResult.Success(1).failureMessageOrNull())
    }

    @Test
    fun failureMessageOrNull_businessFallbackWhenMessageBlank() {
        assertEquals("操作失败（3）", ApiResult.BusinessError(3, null).failureMessageOrNull())
        assertEquals("操作失败（3）", ApiResult.BusinessError(3, "  ").failureMessageOrNull())
    }

    @Test
    fun failureMessageOrNull_businessUsesMessage() {
        assertEquals("msg", ApiResult.BusinessError(0, "msg").failureMessageOrNull())
    }

    @Test
    fun failureMessageOrNull_networkUsesUserMessageOrDefault() {
        val r = ApiResult.NetworkError(IllegalStateException("x"), null)
        assertEquals("x", r.failureMessageOrNull())
    }

    @Test
    fun onSuccessOrNotify_successInvokesBlockOnly() {
        val seen = mutableListOf<String>()
        ApiResult.Success("ok").onSuccessOrNotify(notify = { seen += "e" }) { seen += it }
        assertEquals(listOf("ok"), seen)
    }

    @Test
    fun onSuccessOrNotify_failureNotifiesByDefault() {
        var msg: String? = null
        ApiResult.BusinessError(1, "m").onSuccessOrNotify(notify = { msg = it }) {
            throw AssertionError()
        }
        assertEquals("m", msg)
    }

    @Test
    fun onSuccessOrNotify_shouldNotifyFalseSkipsNotify() {
        var called = false
        ApiResult.BusinessError(1, "m").onSuccessOrNotify(
            notify = { called = true },
            shouldNotify = { false },
        ) { }
        assertTrue(!called)
    }

    @Test
    fun onSuccessOrNotify_networkErrorNotifies() {
        var msg: String? = null
        val r = ApiResult.NetworkError(IllegalStateException("net"), "oops")
        r.onSuccessOrNotify(notify = { msg = it }) { throw AssertionError() }
        assertEquals("oops", msg)
    }

    @Test
    fun onSuccessOrNotify_networkErrorFallbackWhenMessageNull() {
        var msg: String? = null
        val r = ApiResult.NetworkError(IllegalStateException("x"), null)
        r.onSuccessOrNotify(notify = { msg = it }) { }
        assertEquals("x", msg)
    }

    @Test
    fun onSuccessOrNotify_defaultNotifyOncePerFailureKind() {
        var n = 0
        ApiResult.BusinessError(1, "a").onSuccessOrNotify(notify = { n++ }) { }
        assertEquals(1, n)
        n = 0
        ApiResult.NetworkError(Exception("e"), "b").onSuccessOrNotify(notify = { n++ }) { }
        assertEquals(1, n)
    }

    @Test
    fun onSuccessOrNotify_successNeverCallsShouldNotifyOnNotifyPath() {
        // Success 时不上 notify、也不走失败分支
        var notifyCount = 0
        ApiResult.Success(42).onSuccessOrNotify(
            notify = { notifyCount++ },
        ) { v ->
            assertEquals(42, v)
        }
        assertEquals(0, notifyCount)
    }

    @Test
    fun failureMessageOrNull_businessCodeZero() {
        assertNotNull(ApiResult.BusinessError(0, null).failureMessageOrNull())
    }
}
