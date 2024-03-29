package coroutines.queue

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.*
import java.util.concurrent.CancellationException

/** A Queue for organizing asynchronous coroutines.
 * @author DK96-OS : 2021 - 2023
 */
class CoroutineQueue<T>(
	/** The maximum number of coroutines allowed in the queue at one time.
	 * If this value is zero or negative, the capacity is unlimited.
	 */
	val capacity: Int,
) {
	
    private val mQueue
    : Deque<Deferred<T?>> = ArrayDeque(capacity)

	/** The number of Coroutines currently in the queue.
	 */
	val count: Int
		get() = mQueue.size
	
	/** Add a deferred result to the Queue.
	 * @param task The Task to be inserted into the Queue.
	 * @return True if the queue allowed the task to be added (didn't exceed capacity)
	 */
	fun add(
		task: Deferred<T?>
	) : Boolean {
		// If there is no capacity
		if (capacity < 1)
			return mQueue.add(task)
		// Check capacity
		if (mQueue.size < capacity)
			return mQueue.offer(task)
		// Queue is at Capacity
		return false
	}

	/** Wait for the first coroutine in the Queue, return it's result..
	 * @return The result of the first task, or null if Queue is empty.
	 */
	suspend fun awaitNext()
	: T? = mQueue.poll()?.await()

	/** Await each element in the queue, add it to a list and return the list.
	 * @param limit The maximum number of elements to include in the list. 0 (or negative) is unlimited.
	 * @param countNull Whether null deferred results count towards the limit.
	 * @return An ArrayList containing the results of all tasks in the queue.
	 */
	suspend fun awaitList(
		limit: Int = 0,
		countNull: Boolean = true,
	) : ArrayList<T> {
		val unlimited = limit < 1
		val queueSize = mQueue.size
		//
		val expectedSize: Int = when {
			queueSize == 0 -> return ArrayList<T>()
			unlimited -> queueSize
			limit <= queueSize -> limit
			else -> queueSize
		}
		val list = ArrayList<T>(expectedSize)
		//
		var task: Deferred<T?>? = mQueue.poll()
		var counter = 0
		if (unlimited)
			while (task != null) {
				val result = task.await()
				if (result != null) list.add(result)
				// Obtain next task
				task = mQueue.poll()
			}
		else
			while (task != null) {
				val result = task.await()
				when {
					result != null -> {
						list.add(result)
						if (++counter >= limit) return list
					}
					countNull -> {
						if (++counter >= limit) return list
					}
					else -> {
						// Result was null, and nulls do not count towards limit
					}
				}
				// Obtain next Task
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
		suspend fun <A, B> transformList(
			input: List<A>,
			transform: suspend (A) -> B?
		) : ArrayList<B> {
			if (1 < input.size) return coroutineScope {
				val queue = CoroutineQueue<B>(input.size)
				for (i in input)
					queue.add(async {
						transform(i)
					})
				queue.awaitList()
			} else if (input.size == 1) {
				// The input list has only one element
				val result = transform(input[0])
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
		suspend fun <A, B> transformArray(
			input: Array<A>,
			transform: suspend (A) -> B?,
		) : ArrayList<B> {
			if (input.size < 2) {
				if (input.size == 1) {
					val result = transform(input[0])
					if (result != null)
						return arrayListOf(result)
				}
			} else return coroutineScope {
				val queue = CoroutineQueue<B>(input.size)
				input.forEach { a ->
					queue.add(async {
						transform(a)
					})
				}
				queue.awaitList()
			}
			// By default, return new empty ArrayList
			return ArrayList(0)
		}
	}

}
