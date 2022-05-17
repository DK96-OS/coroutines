package coroutines.queue

import coroutines.examples.InputData
import coroutines.examples.OutputData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.math.roundToInt
import kotlin.random.Random

/** Test suite for CoroutineQueue
 * @author DK96-OS : 2021 - 2022
 */
class CoroutineQueueTest {

	/** The capacity is the size of the input list */
	private val capacity = 1000

	private lateinit var queue: CoroutineQueue<OutputData>

  	/** Generate immutable input data to be used in all tests */
	private val inputList: List<InputData> = Array(capacity) {
	    InputData(
			stream = Random
				.nextBytes(64)
				.sortedArray(),	// sort to avoid string init issues
			key = Random
				.nextInt()
				.ushr(25)	// shift to remove negatives
				.toByte()
		)
    }.toList()

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
			capacity, output.size)
		for (out in output)
			assertEquals(
				64, out.title.length)
			// Now sort the times, check their differences
		val sortedList = output.sortedBy {
			it.createTime
		}
		val diffTimeList = ArrayList<Int>(
			sortedList.size - 1)
		for (i in 1 until sortedList.size) {
			val diff = sortedList[i].createTime - sortedList[i - 1].createTime
			diffTimeList.add(
				diff.toInt())	// Diffs are small enough to be Int
		}
		println("Shortest Diff: ${diffTimeList.minOrNull()}")
		println("Average Diff: ${diffTimeList.average().roundToInt()}")
		println("Longest Diff: ${diffTimeList.maxOrNull()}")
	}

	@Test
	fun testAwaitAll() {
		// Await All does not return a value
		runBlocking {
			// Load results into this arrayList
			val array = ArrayList<OutputData>(capacity)
			// Start Coroutines
			for (i in inputList) queue.add(async {
				val output = i.transform()
				array.add(output)
				output
			})
			// Await
			queue.awaitAll()
			assertEquals(
				capacity, array.size)
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
					queue.awaitNext())
			assertNull(
				queue.awaitNext())
		}
	}

	@Test
    fun testCancel() {
		runBlocking {
			for (i in inputList) queue.add(async {
				i.transform()
			})
			assertEquals(
				inputList[0].key,
				queue.awaitNext()!!.key
			)
			assertEquals(
				capacity - 1, queue.count)
			queue.cancel(
				CancellationException("Testing Cancel Operation"))
			assertEquals(
				0, queue.count)
			assertNull(
				queue.awaitNext())
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
				capacity, output.size)
			for (out in output)
				assertEquals(
					64, out.title.length)
		}
	}
  
	@Test
    fun testTransformListFunction() {
		runBlocking {
			val output = CoroutineQueue.transformList(inputList) {
				delay(20)
				it.transform()
			}
			assertEquals(
				capacity, output.size)
			for (out in output)
				assertEquals(
					64, out.title.length)
		}
	}
	
	@Test
    fun testTransformNullability() {
		runBlocking {
			val nullTestInputs = listOf(
				1, 2, 3, 4, 5, 6
			)
			val output = CoroutineQueue.transformList(
				nullTestInputs
			) {
				when {
					it % 2 == 0 -> "Even"
					it % 3 == 0 -> "Three"
					else -> null
				}
			}
				// 1 is removed because null
			assertEquals(
				"Even", output[0])		// Two is even
			assertEquals(
				"Three", output[1]) 	// Three
			assertEquals(
				"Even", output[2])		// Four is even
				// Five is null
			assertEquals(
				"Even", output[3])		// Six is even
			assertEquals(
				4, output.size)
		}
	}
}