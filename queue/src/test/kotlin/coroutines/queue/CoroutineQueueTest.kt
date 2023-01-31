package coroutines.queue

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/** Testing [CoroutineQueue]
 * @author DK96-OS : 2021 - 2023
 */
@ExperimentalCoroutinesApi
class CoroutineQueueTest
	: QueueTestingTemplate(
		inputDataSize = 100,
	) {

	/** The capacity of the Queue.
	 */
	private val capacity = 100

	@BeforeEach
	fun testSetup() {
		init(capacity)
    }

	@Test
	fun testInputKeysArePositive() {
		for (input in input)
            assert(input.key >= 0)
	}

	@Test
	fun testAwaitListLimit5() {
		addAllInputData()
		val limit = 5
		runTest {
			val resultList = queue.awaitList(limit)
			assertEquals(
				limit, resultList.size
			)
		}
		assertEquals(
			capacity - limit, queue.count
		)
	}

	@Test
	fun testAwaitListLimitNegative() {
		addAllInputData()
		val limit = -5
		runTest {
			val resultList = queue.awaitList(limit)
			assertEquals(
				capacity, resultList.size
			)
			assertEquals(
				0, queue.count
			)
		}
	}

	@Test
	fun testAwaitListEmptyQueue() {
		runTest {
			val result = queue.awaitList()
			assertEquals(
				0, result.size
			)
			assertEquals(
				0, queue.count
			)
		}
	}

	@Test
	fun testAwaitListQueueSizeBelowLimit() {
		addAllInputData()
		runTest {
			val result = queue.awaitList(capacity + 1)
			assertEquals(
				capacity, result.size
			)
			assertEquals(
				0, queue.count
			)
		}
	}

	@Test
	fun testAwaitListCountNullResults() {
		// Prepare results with first 10 non-null, then 20 null, then all non-null
		runTest {
			var i = 0
			while (i < 10) {
				val inputData = input[i++]
				val task = async { inputData.transform() }
				queue.add(task)
			}
			while (i < 30) {
				val inputData = input[i++]
				val task = async { inputData.transform(true) }
				queue.add(task)
			}
			while (i < inputDataSize) {
				val inputData = input[i++]
				val task = async { inputData.transform() }
				queue.add(task)
			}
		}
		// Get the first 20 task results, while counting null values
		runTest {
			val results = queue.awaitList(20, true)
			assertEquals(
				10, results.size
			)
			assertEquals(
				80, queue.count
			)
		}
	}

	@Test
	fun testAwaitListNoNullResults() {
		// Prepare results with first 10 non-null, then 20 null, then all non-null
		runTest {
			var i = 0
			while (i < 10) {
				val inputData = input[i++]
				val task = async { inputData.transform() }
				queue.add(task)
			}
			while (i < 30) {
				val inputData = input[i++]
				val task = async { inputData.transform(true) }
				queue.add(task)
			}
			while (i < inputDataSize) {
				val inputData = input[i++]
				val task = async { inputData.transform() }
				queue.add(task)
			}
		}
		// Get the first 20 non-null task results
		runTest {
			val results = queue.awaitList(20, false)
			assertEquals(
				20, results.size
			)
			assertEquals(
				60, queue.count
			)
		}
	}

	@Test
	fun testAwaitAllEmptyQueue() {
		runTest {
			assertEquals(
				0, queue.count
			)
			assertEquals(
				0, queue.awaitAll()
			)
			assertEquals(
				0, queue.count
			)
		}
	}

	@Test
	fun testAwaitAllFullQueue() {
		addAllInputData()
		assertEquals(
			capacity, queue.count
		)
		runTest {
			assertEquals(
				capacity, queue.awaitAll()
			)
		}
		assertEquals(
			0, queue.count
		)
	}

	@Test
	fun testAwaitAllLimit5() {
		addAllInputData()
		assertEquals(
			capacity, queue.count
		)
		val limit = 5
		runBlocking {
			// Wait for 5 tasks
			assertEquals(
				limit, queue.awaitAll(limit)
			)
			assertEquals(
				capacity - limit, queue.count
			)
			// Check the new first item in the Queue
			assertEquals(
				input[limit].key,
				queue.awaitNext()!!.key
			)
		}
	}

	@Test
	fun testAwaitAllLimitAboveCapacity() {
		addAllInputData()
		assertEquals(
			capacity, queue.count
		)
		runTest {
			// Wait for more than capacity
			assertEquals(
				capacity, queue.awaitAll(capacity + 100)
			)
			assertEquals(
				0, queue.count
			)
			assertNull(
				queue.awaitNext()
			)
		}
	}

	@Test
    fun testAwaitNext() {
		addAllInputData()
		runTest {
			var counter = queue.count	// Count down to zero
			while (counter-- > 0)
				assertNotNull(
					queue.awaitNext()
				)
			assertNull(
				queue.awaitNext()
			)
		}
	}

	@Test
	fun testCancelAll() {
		addAllInputData()
		runTest {
			assertEquals(
				input[0].key,
				queue.awaitNext()!!.key
			)
			val remainingTasks = capacity - 1
			assertEquals(
				remainingTasks, queue.count
			)
			val cancelledTasks = queue.cancel(
				CancellationException("Testing Cancel Operation")
			)
			assertEquals(
				remainingTasks, cancelledTasks
			)
			assertEquals(
				0, queue.count
			)
			assertNull(
				queue.awaitNext()
			)
		}
	}

	@Test
    fun testCancelSingle() {
		val input = input[0]
		runTest {
			val task = async {
				input.transform()
			}
			queue.add(task)
			assertEquals(
				1, queue.count
			)
			// Cancel and check Task result
			val exception = CancellationException("Testing Cancel Operation")
			assertEquals(
				1, queue.cancel(exception)
			)
			assertTrue(
				task.isCancelled
			)
			assertEquals(
				0, queue.count
			)
		}
	}

	@Test
	fun testCancelEmptyQueue() {
		assertEquals(
			0, queue.cancel()
		)
	}

	@Test
    fun testReusability() {
		addAllInputData()
		queue.cancel()
		addAllInputData()
		val output = runBlocking { queue.awaitList() }
		assertEquals(
			capacity, output.size
		)
		for (out in output)
			assertEquals(
				64, out.title.length
			)
	}

	@Test
	fun testAddExceedsCapacity() {
		addAllInputData()
		assertEquals(
			capacity, queue.count
		)
		runTest {
			assertFalse(
				queue.add(async { input[0].transform() })
			)
		}
		assertEquals(
			capacity, queue.count
		)
	}

}