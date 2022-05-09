package androidtools.data.coroutine

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.math.roundToInt
import kotlin.random.Random

/** Test suite for CoroutineQueue
 * @author DK96-OS : 2021
 */
class CoroutineQueueTest {

	/** Data structure used as coroutine input for tests */
	private class ProcessInput(val stream: ByteArray, val key: Byte)

	/** Data structure for coroutine output */
	private class ProcessOutput(val title: String, val index: Int) {
		/** Reveals when this object is created */
		val createTime: Long = System.nanoTime()
	}

	/** The capacity is the size of the input list */
	private val capacity = 1000
	private lateinit var q1: CoroutineQueue<ProcessOutput>

  	/** Generate immutable input data to be used in all tests */
	private val inputList: List<ProcessInput> = Array(capacity) {
        ProcessInput(
		stream = Random.nextBytes(64).sortedArray(),	// sort to avoid string init issues
		key = Random.nextInt().ushr(25).toByte()	// shift to remove negatives
	)
    }.toList()

	@Before
	fun setup() {
		q1 = CoroutineQueue(capacity)
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
			for (i in inputList) q1.add(async {
				ProcessOutput(String(i.stream), i.key.toInt())
			})
			println("Filled Queue: ${System.nanoTime()}")
			q1.awaitList()	// returns a list of the output type
		}
		println("Output ready: ${System.nanoTime()}")
		assertEquals(capacity, output.size)
		for (out in output) assertEquals(64, out.title.length)
			// Now sort the times, check their differences
		val sortedList = output.sortedBy {it.createTime}
		val diffTimeList = ArrayList<Int>(sortedList.size - 1)
		for (i in 1 until sortedList.size) {
			val diff = sortedList[i].createTime - sortedList[i - 1].createTime
			diffTimeList.add(diff.toInt())	// Diffs are small enough to be Int
		}
		println("Shortest Diff: ${diffTimeList.minOrNull()}")
		println("Average Diff: ${diffTimeList.average().roundToInt()}")
		println("Longest Diff: ${diffTimeList.maxOrNull()}")
	}

	@Test
    fun testAwaitNext() {
		runBlocking {
			for (i in inputList) q1.add(async {
				ProcessOutput(String(i.stream), i.key.toInt())
			})
			var counter = q1.count	// Count down to zero
			while (counter-- > 0)
				assert(q1.awaitNext() != null)
			assertEquals(null, q1.awaitNext())
		}
	}

	@Test
    fun testCancel() {
		runBlocking {
			for (i in inputList) q1.add(async {
				ProcessOutput(String(i.stream), i.key.toInt())
			})
			assertEquals(inputList[0].key.toInt(), q1.awaitNext()!!.index)
			assertEquals(capacity - 1, q1.count)
			q1.cancel(CancellationException("Testing Cancel Operation"))
			assertEquals(0, q1.count)
			assertEquals(null, q1.awaitNext())
		}
	}

	@Test
    fun testReusability() {
		runBlocking {
			for (i in inputList) q1.add(async {
				ProcessOutput(String(i.stream), i.key.toInt())
			})
			q1.cancel()
				// Now Retry
			for (i in inputList) q1.add(async {
				ProcessOutput(String(i.stream), i.key.toInt())
			})
			val output = q1.awaitList()
			assertEquals(capacity, output.size)
			for (out in output) assertEquals(64, out.title.length)
		}
	}
  
	@Test
    fun testTransformListFunction() {
		runBlocking {
			val output = CoroutineQueue.transformList(inputList) {
				delay(20)
				ProcessOutput(String(it.stream), it.key.toInt())
			}
			assertEquals(capacity, output.size)
			for (out in output) assertEquals(64, out.title.length)
		}
	}
	
	@Test
    fun testTransformNullability() {
		runBlocking {
			val nullTestInputs = listOf(1, 2, 3, 4, 5, 6)
			val output = CoroutineQueue.transformList(nullTestInputs) {
				when {
					it % 2 == 0 -> "Even"
					it % 3 == 0 -> "Three"
					else -> null
				}
			}
				// 1 is removed because null
			assertEquals("Even", output[0])		// Two is even
			assertEquals("Three", output[1]) 	// Three
			assertEquals("Even", output[2])		// Four is even
				// Five is null
			assertEquals("Even", output[3])		// Six is even
			assertEquals(4, output.size)
		}
	}
}