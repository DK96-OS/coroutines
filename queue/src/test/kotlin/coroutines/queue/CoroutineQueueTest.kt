package coroutines.queue

import coroutines.examples.InputData
import coroutines.examples.OutputData
import coroutines.examples.TestDataProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.math.roundToInt

/** Test suite for CoroutineQueue
 * @author DK96-OS : 2021 - 2022
 */
class CoroutineQueueTest {

	private val provider = TestDataProvider()

	/** The capacity is the size of the input list.
	 */
	private val capacity = 400

	/** A CoroutineQueue instance created before each test.
	 */
	private lateinit var queue: CoroutineQueue<OutputData>

  	/** An immutable input data to be used in tests.
     */
	private val inputList: List<InputData> = provider.createInput(
	    capacity, 300
    ).toList()

	@BeforeEach
	fun setup() {
		queue = CoroutineQueue(capacity)
    }

	@Test
	fun testInputKeysArePositive() {
		for (input in inputList)
            assert(input.key >= 0)
	}

	@Test
	fun testAwaitList() {
		val output = runBlocking {
			println("Loadin Queue: ${System.nanoTime()}")
			for (i in inputList) queue.add(async {
				i.transform()
			})
			println("Filled Queue: ${System.nanoTime()}")
			queue.awaitList()	// returns a list of the output type
		}
		println("Output ready: ${System.nanoTime()}")
		assertEquals(
			capacity, output.size
		)
		for (out in output)
			assertEquals(
				64, out.title.length
			)
			// Now sort the times, check their differences
		val sortedList = output.sortedBy {
			it.createTime
		}
		val diffTimeList = ArrayList<Int>(
			sortedList.size - 1
		)
		for (i in 1 until sortedList.size) {
			val diff = sortedList[i].createTime - sortedList[i - 1].createTime
			diffTimeList.add(
				diff.toInt()
			)	// Diffs are small enough to be Int
		}
		println("Shortest Diff: ${diffTimeList.minOrNull()}")
		println("Average Diff: ${diffTimeList.average().roundToInt()}")
		println("Longest Diff: ${diffTimeList.maxOrNull()}")
	}

	@Test
	fun testAwaitAll() {
		// Load results into this arrayList
		val array = ArrayList<OutputData>(capacity)
		// Await All does not return a value
		runBlocking {
			// Start Coroutines
			for (i in inputList) queue.add(async {
				val output = i.transform()
				array.add(output)
				output
			})
			// Await all returns count of completed tasks
			assertEquals(
				capacity, queue.awaitAll()
			)
		}
		assertEquals(
			capacity, array.size
		)
	}

	@Test
	fun testAwaitAllEmptyQueue() {
		runBlocking {
			assertEquals(
				0, queue.awaitAll()
			)
		}
	}

	@Test
    fun testAwaitNext() {
		runBlocking {
			for (i in inputList) queue.add(async {
				i.transform()
			})
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
		runBlocking {
			for (i in inputList) queue.add(async {
				i.transform()
			})
			assertEquals(
				inputList[0].key,
				queue.awaitNext()!!.key
			)
			assertEquals(
				capacity - 1, queue.count
			)
			queue.cancel(
				CancellationException("Testing Cancel Operation")
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
		val input = inputList[0]
		runBlocking {
			val task = async {
				input.transform()
			}
			queue.add(task)
			assertEquals(
				1, queue.count
			)
			// Cancel and check Task result
			val exception = CancellationException("Testing Cancel Operation")
			queue.cancel(exception)
			//
			assertTrue(
				task.isCancelled
			)
			assertEquals(
				0, queue.count
			)
		}
	}

	@Test
    fun testReusability() {
		runBlocking {
			for (i in inputList) queue.add(async {
				i.transform()
			})
			queue.cancel()
			// Now Retry
			for (i in inputList) queue.add(async {
				i.transform()
			})
			val output = queue.awaitList()
			assertEquals(
				capacity, output.size
			)
			for (out in output)
				assertEquals(
					64, out.title.length
				)
		}
	}

}