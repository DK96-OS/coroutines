package coroutines.examples

import kotlin.math.roundToInt

/** Data structure used as coroutine input for tests.
 * @author DK96-OS : 2022
 */
class InputData(
	val key: Byte,
	val stream: ByteArray,
) {

	/** Produce an Output from this Input.
	 * @param returnNull Whether to return null instead of OutputData.
	 */
	fun transform(
		returnNull: Boolean = false,
	) : OutputData? = if (returnNull)
		null
	else OutputData(
		key, String(stream)
	)

	/** Perform a slow and cpu intensive operation on the input data.
	 */
	fun cpuIntensiveTransformation()
		: OutputData {
		var variable1 = 0
		var variable2 = 0
		// Outer Loop
		for (i in stream) {
			// Inner Loop 1
			for (j in stream) {
				if (i == key) {
					variable1 += ((i + (j * key) / 767f) * 20).roundToInt()
				} else {
					variable1 -= ((j * key) / 9008f).roundToInt()
				}
			}
			// Inner Loop 2
			for (k in stream) {
				if (k == key) {
					variable2 += ((i + (k * key) / 1767f) * 3).roundToInt()
				} else {
					variable2 -= ((k * key) / 9108f).roundToInt()
				}
			}
		}
		return OutputData(
			key, "$variable1 - $variable2"
		)
	}

}