package coroutines.examples

/** Data structure used as coroutine input for tests
 */
class InputData(
	val key: Byte,
	val stream: ByteArray,
) {
	/** Produce an Output data from this Input data
	 */
	fun transform()
	: OutputData = OutputData(key, String(stream))
}