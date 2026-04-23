package com.example.myapplication.network.result

import com.example.myapplication.network.dto.JsonPlaceholderPostDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/** 与 [NetworkToApiResult] 中 [handleData] / [handleResult] / [handleDataFlow] / [handleResultFlow] / [asState] 一致。 */
@OptIn(ExperimentalCoroutinesApi::class)
class NetworkRequestModesTest {

    private val server = MockWebServer()

    @After
    fun tearDown() {
        server.shutdown()
    }

    private interface DirectPostsApi {
        @GET("posts")
        suspend fun list(): List<JsonPlaceholderPostDto>
    }

    private interface WrappedStringApi {
        @GET("wrapped")
        suspend fun load(): ApiResponse<String>
    }

    @Test
    fun handleData_success() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""[{"userId":1,"id":1,"title":"a","body":"b"}]"""),
        )
        val api = retrofit().create(DirectPostsApi::class.java)
        val r = handleData { api.list() }
        assertTrue(r is ApiResult.Success)
        val list = (r as ApiResult.Success).data
        assertEquals(1, list.size)
        assertEquals("a", list[0].title)
    }

    @Test
    fun handleData_httpError() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("{}"))
        val api = retrofit().create(DirectPostsApi::class.java)
        val r = handleData { api.list() }
        assertTrue(r is ApiResult.NetworkError)
    }

    @Test
    fun handleData_onSuccessOrNotify_onlySuccessBlockWhenOk() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""[{"userId":1,"id":1,"title":"a","body":"b"}]"""),
        )
        val api = retrofit().create(DirectPostsApi::class.java)
        val r = handleData { api.list() }
        var toast: String? = null
        r.onSuccessOrNotify(notify = { toast = it }) { list ->
            assertEquals(1, list.size)
            assertEquals("a", list[0].title)
        }
        assertNull(toast)
    }

    @Test
    fun handleData_onSuccessOrNotify_notifiesOnHttpError() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("{}"))
        val api = retrofit().create(DirectPostsApi::class.java)
        val r = handleData { api.list() }
        var toast: String? = null
        r.onSuccessOrNotify(notify = { toast = it }) {
            throw AssertionError("onSuccess 不应在失败时被调用")
        }
        assertNotNull(toast)
        assertTrue(toast!!.isNotEmpty())
    }

    @Test
    fun handleResult_onSuccessOrNotify_businessErrorUsesMessage() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"code":1,"message":"nope","data":null}"""),
        )
        val api = retrofit().create(WrappedStringApi::class.java)
        val r = handleResult { api.load() }
        var toast: String? = null
        r.onSuccessOrNotify(notify = { toast = it }) {
            throw AssertionError()
        }
        assertEquals("nope", toast)
    }

    @Test
    fun handleResult_businessError() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"code":1,"message":"nope","data":null}"""),
        )
        val api = retrofit().create(WrappedStringApi::class.java)
        val r = handleResult { api.load() }
        assertTrue(r is ApiResult.BusinessError)
        assertEquals(1, (r as ApiResult.BusinessError).code)
    }

    @Test
    fun handleResult_success() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"code":0,"data":"ok"}"""),
        )
        val api = retrofit().create(WrappedStringApi::class.java)
        val r = handleResult { api.load() }
        assertTrue(r is ApiResult.Success)
        assertEquals("ok", (r as ApiResult.Success).data)
    }

    @Test
    fun handleDataFlow_emitsLoadingThenSuccess() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""[]"""),
        )
        val api = retrofit().create(DirectPostsApi::class.java)
        val d = UnconfinedTestDispatcher(testScheduler)
        val states = handleDataFlow(dispatcher = d) { api.list() }
            .toList()
        assertEquals(ApiState.Loading, states[0])
        assertTrue(states[1] is ApiState.Success)
    }

    @Test
    fun handleResultFlow_emitsLoadingThenSuccess() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"code":0,"data":"x"}"""),
        )
        val api = retrofit().create(WrappedStringApi::class.java)
        val d = UnconfinedTestDispatcher(testScheduler)
        val states = handleResultFlow(dispatcher = d) { api.load() }
            .toList()
        assertEquals(ApiState.Loading, states[0])
        assertTrue(states[1] is ApiState.Success)
    }

    @Test
    fun asState_mapsAndCatches() = runTest {
        val ok = flowOf(1)
            .asState()
            .toList()
        assertTrue(ok[0] is ApiState.Success)
        assertEquals(1, (ok[0] as ApiState.Success).data)
        val err = flow<Int> { error("boom") }
            .asState()
            .toList()
        assertTrue(err[0] is ApiState.Error)
    }

    private fun retrofit() = Retrofit.Builder()
        .baseUrl(server.url("/"))
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
