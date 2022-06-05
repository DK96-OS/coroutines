package coroutines.queue

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CancellationException

/** A Queue for organizing asynchronous coroutines.
 * @author DK96-OS : 2021 - 2022
 */
class CoroutineQueue<T>(
	/** The maximum number of coroutines allowed in the queue at one time. */
	val capacity: Int,
) {
	
    private val mQueue
    : Queue<Deferred<T?>> = ArrayBlockingQueue(
	    capacity, true
    )

	val count: Int
		get() = mQueue.size
	
	/** Add a deferred result to the Queue.
	 * @param task The Task to be inserted into the Queue.
	 * @return True if the queue allowed the task to be added (didn't exceed capacity)
	 */
	fun add(
		task: Deferred<T?>
	) : Boolean = mQueue.offer(task)

	/** Wait for the first coroutine in the Queue, return it's result..
	 * @return The result of the first task, or null if Queue is empty.
	 */
	suspend fun awaitNext()
	: T? = mQueue.poll()?.await()

	/** Await each element in the queue, add it to a list and return the list
	 * @return An ArrayList containing the results of all tasks in the queue.
	 */
	suspend fun awaitList()
	: ArrayList<T> {
		var task: Deferred<T?>? = mQueue.poll()
		val list = ArrayList<T>(mQueue.count())
		while (task != null) {
			val result = task.await()
			if (result != null)
				list.add(result)
			task = mQueue.poll()
		}
		return list
	}

    /** Wait for a group of tasks to complete.
     * @param limit The maximum number of tasks to wait for. zero is unlimited.
     * @return The number of tasks that were waited on.
     */
	suspend fun awaitAll(
	    limit: Int = 0,
	) : Int {
	    // Count the number of tasks that are waited for.
	    var taskCount = 0
	    // Get the next Task in the Queue
		var task: Deferred<T?>? = mQueue.poll()
		while (task != null) {
			// Wait for the task to complete
			task.await()
			// Increment the counter, check if limit is reached
			if (++taskCount == limit) return limit
			// Get the next task
			task = mQueue.poll()
		}
	    return taskCount
	}

	/** Tries to cancel everything in the queue.
	 * @param cause An Exception to be passed to all cancelled tasks.
	 * @return The number of tasks that were cancelled.
	 */
	fun cancel(
		cause: CancellationException? = null,
	) : Int {
		// Count the number of affected tasks
		var taskCount = 0
		// Get and cancel all tasks
		var task = mQueue.poll()
		while (task != null) {
			// Cancel this task
			task.cancel(cause)
			// Increment the counter
			++taskCount
			// Get the next task
			task = mQueue.poll()
		}
		return taskCount
	}

	companion object {

		/** Applies a suspendable transformation on a list using the CoroutineQueue.
		 * Skips using CoroutineQueue if input size is less than 2.
		 * @param input The input List to run a transformation on.
		 * @param transform The suspending transformation operation.
		 * @return A new ArrayList containing the non-null transformation products.
		 */
		fun <A, B> transformList(
			coroutineScope: CoroutineScope,
			input: List<A>,
			transform: suspend (A) -> B?
		) : ArrayList<B> {
			if (1 < input.size) {
				val queue = CoroutineQueue<B>(input.size)
				input.forEach { a ->
					queue.add(coroutineScope.async(Dispatchers.IO) {
						transform(a)
					})
				}
				return runBlocking { queue.awaitList() }
			} else if (input.size == 1) {
				// The input list has only one element
				val result = runBlocking { transform(input[0]) }
				if (result != null)
					return arrayListOf(result)
			}
			// By default, return new empty ArrayList
			return ArrayList(0)
		}

		/** Applies a suspendable transformation on an array using CoroutineQueue.
		 * Skips instantiating CoroutineQueue if input size is less than 2.
		 * @param input The input List to run a transformation on.
		 * @param transform The suspending transformation operation.
		 * @return A new ArrayList containing the non-null transformation products.
		 */
		fun <A, B> transformArray(
			coroutineScope: CoroutineScope,
			input: Array<A>,
			transform: suspend (A) -> B?,
		) : ArrayList<B> {
			if (input.size < 2) {
				if (input.size == 1) {
					val result = runBlocking {
						transform(input[0])
					}
					if (result != null)
						return arrayListOf(result)
				}
			} else {
				val queue = CoroutineQueue<B>(input.size)
				input.forEach { a ->
					queue.add(coroutineScope.async(Dispatchers.IO) {
						transform(a)
					})
				}
				return runBlocking { queue.awaitList() }
			}
			// By default, return new empty ArrayList
			return ArrayList(0)
		}
	}

}