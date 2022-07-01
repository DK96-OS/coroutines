package coroutines.queue

import coroutines.examples.OutputData
import coroutines.examples.TestDataProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/** Testing the Coroutine Queue with an unlimited capacity.
 * @author DK96-OS : 2022
 */
class CoroutineQueueNoCapacityTest {

	private val provider = TestDataProvider()

	private lateinit var queue: CoroutineQueue<OutputData>

	/** The capacity value passed to the Coroutine Queue during construction.
	 */
	private val capacity = 0

	@BeforeEach
	fun testSetup() {
		queue = CoroutineQueue(capacity)
	}

	@Test
	fun testInitialValues() {
		assertEquals(
			capacity, queue.capacity
		)
		assertEquals(
			0, queue.count
		)
	}

	@Test
	fun testNegativeCapacity() {
		queue = CoroutineQueue(-1)
		assertEquals(
			-1, queue.capacity
		)
		assertEquals(
			0, queue.count
		)
		runBlocking {
			val additionCount = 10
			for (i in provider.createInput(additionCount))
				assertTrue(
					queue.add(async { i.transform() })
				)
			assertEquals(
				additionCount, queue.count
			)
			assertEquals(
				additionCount, queue.awaitAll()
			)
		}
	}

	@Test
	fun testAdd100() {
		val input = provider.createInput(100)
		runBlocking {
			for (i in input)
				assertTrue(
					queue.add(async { i.transform() })
				)
			assertEquals(
				100, queue.count
			)
			// Await All tasks in the queue
			assertEquals(
				100, queue.awaitAll()
			)
			assertEquals(
				0, queue.count
			)
		}
	}

}
