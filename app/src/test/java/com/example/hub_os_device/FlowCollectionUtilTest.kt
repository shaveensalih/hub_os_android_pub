import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle

@VisibleForTesting(otherwise = VisibleForTesting.NONE)
@ExperimentalCoroutinesApi
fun <T> TestScope.collectFlowValue(flow: Flow<T>): T? {
    var value: T? = null
    val job = launch {
        flow.collect {
            value = it
        }
    }

    advanceUntilIdle()
    job.cancel()

    return value
}