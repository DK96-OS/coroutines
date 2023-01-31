package coroutines.queue

import coroutines.examples.InputData
import coroutines.examples.OutputData
import coroutines.examples.TestDataProvider
import coroutines.queue.CoroutineQueue.Companion.transformArray
import coroutines.queue.CoroutineQueue.Companion.transformList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Testing the CoroutineQueue Companion.
 * @author DK96-OS : 2022 - 2023
 */
class CoroutineQueueCompanionTest {

	private val provider = TestDataProvider()

	/** The size of the input data set.
	 */
	private val capacity = 100

	/** An array of InputData for use in tests.
	 */
	private val inputArray
		: Array<InputData> = provider.createInput(capacity)

	/** A list of InputData for use in tests.
	 */
	private val inputList
		: List<InputData> = inputArray.toList()

	@Test
	fun testTransformListEmptyList() {
		runBlocking {
			val result = transformList(emptyList<InputData>()) {
				it.transform()
			}
			assertEquals(
				0, result.size
			)
		}
	}

	@Test
	fun testTransformListSingleItem() {
		val input = listOf(
			InputData(
				77, byteArrayOf(5, 7, 9, 3)
			)
		)
		runBlocking {
			val result = transformList(input) {
				it.transform()
			}
			assertEquals(
				1, result.size
			)
			assertEquals(
				77, result[0].key
			)
		}
	}

	@Test
	fun testTransformListSingleItemToNull() {
		val input = listOf(
			InputData(
				77, byteArrayOf(45, 23)
			)
		)
		runBlocking {
			val result: ArrayList<OutputData> = transformList(input) {
				it.transform()
				null
			}
			assertEquals(
				0, result.size
			)
		}
	}

	@Test
	fun testTransformListFunction() {
		runBlocking {
			val output = transformList(inputList) {
				it.transform()
			}
			assertEquals(
				capacity, output.size
			)
			for (out in output)
				assertEquals(
					64, out.title.length
				)
		}
	}

	@Test
	fun testTransformListNullability() {
		runBlocking {
			val nullTestInputs = listOf(
				1, 2, 3, 4, 5, 6
			)
			val output = transformList(
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

	@Test
	fun testTransformArray() {
		runBlocking {
			val output = transformArray(inputArray) {
				it.transform()
			}
			assertEquals(
				capacity, output.size
			)
			for (out in output)
				assertEquals(
					64, out.title.length
				)
		}
	}

	@Test
	fun testTransformArraySingle() {
		val input = Array(1) {
			InputData(77, byteArrayOf(4, 2, 4, 6))
		}
		runBlocking {
			val output = transformArray(input) {
				it.transform()
			}
			assertEquals(
				1, output.size
			)
			val expectedOutput = input[0].transform()!!
			assertEquals(
				expectedOutput.key,
				output[0].key
			)
			assertEquals(
				expectedOutput.title,
				output[0].title
			)
		}
	}

	@Test
	fun testTransformArrayEmpty() {
		val input = Array(0) {
			InputData(77, byteArrayOf(4, 2, 4, 6))
		}
		runBlocking {
			val output = transformArray(input) {
				it.transform()
			}
			assertEquals(
				0, output.size
			)
		}
	}

	@Test
	fun testTransformArraySingleNull() {
		val input = Array(1) {
			InputData(77, byteArrayOf(4, 2, 4, 6))
		}
		runBlocking {
			val output: ArrayList<OutputData> = transformArray(input) {
				null
			}
			assertEquals(
				0, output.size
			)
		}
	}

}
