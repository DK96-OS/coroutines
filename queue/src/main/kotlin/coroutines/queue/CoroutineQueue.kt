package androidtools.data.coroutine

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CancellationException
import kotlin.collections.ArrayList

/** Simple Queue for awaiting asynchronous coroutines
  * DK96-OS : 2021 */
class CoroutineQueue<T>(val capacity: Int) {
	
    private val mQueue: Queue<Deferred<T?>> = ArrayBlockingQueue(capacity, true)

	val count: Int get() = mQueue.size
	
	/** Add a deferred result to the Queue
	 * @return True if the queue allowed the task to be added (didn't exceed capacity) */
	fun add(task: Deferred<T?>) = mQueue.offer(task)

	/** Block until next coroutine finishes, 
	  * @return null if empty queue or task result is nullable  */
	suspend fun awaitNext(): T? = mQueue.poll()?.await()

	/** Await each element in the queue, add it to a list and return the list */
	suspend fun awaitList(): ArrayList<T> {
		var task: Deferred<T?>? = mQueue.poll()
		val list = ArrayList<T>(mQueue.count())
		while (task != null) {
			val result = task.await()
			if (result != null) list.add(result)
			task = mQueue.poll()
		}
		return list
	}

    /** Block thread until queue is empty */
	suspend fun awaitAll() {
		var task: Deferred<T?>? = mQueue.poll()
		while (task != null) {
			task.await()
			task = mQueue.poll()
		}
	}

	/** Tries to cancel everything in the queue */
	fun cancel(cause: CancellationException? = null) {
		mQueue.forEach { it.cancel(cause) }
		mQueue.clear()
	}

	companion object {
		/** Applies a suspendable transformation on a list using the CoroutineQueue
		 * Skips using CoroutineQueue if input size is less than 2 */
		suspend fun <A, B> transformList(
			input: List<A>, transform: suspend (A) -> B?
		): ArrayList<B> = when (input.size) {
			0 -> arrayListOf()
			1 -> {
				val result = transform(input[0])
				if (result != null) arrayListOf(result) else arrayListOf()
			}
			else -> {
				val queue = CoroutineQueue<B>(input.size)
				coroutineScope {input.forEach {a ->
					queue.add(async(Dispatchers.IO) { transform(a) })
				} }
				queue.awaitList()
			}
		}
	}
}