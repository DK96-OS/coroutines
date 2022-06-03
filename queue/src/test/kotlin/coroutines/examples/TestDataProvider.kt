package coroutines.examples

import kotlin.random.Random

/** Provides test data.
 */
class TestDataProvider {

	/** Generate random input data to be used by tests.
	 * @param capacity The size of the array to be created.
	 * @param randomSeed A random number generator seed.
	 * @return A new Array of InputData.
	 */
	fun createInput(
		capacity: Int,
		randomSeed: Long = 100L,
	) : Array<InputData> {
		val rng = Random(randomSeed)
		val rng2 = Random(randomSeed)
		return Array(capacity) {
			InputData(
				stream = rng
					.nextBytes(64)
					.sortedArray(),	// sort to avoid string init issues
				key = rng2
					.nextInt()
					.ushr(25)	// shift to remove negatives
					.toByte()
			)
		}
	}

}