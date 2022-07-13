package coroutines.examples

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

}