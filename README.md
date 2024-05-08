# Coroutines
Repository of [__Coroutine__](https://github.com/Kotlin/kotlinx.coroutines) powered data structures.

## Introduction
The `CoroutineQueue` is the flagship module of this repository. It organizes Kotlin Coroutines with a Queue.

There are ideas for additional data structures, but there is no need for them yet.

## Coroutine Queue
The `CoroutineQueue` is a simple yet powerful class designed to manage and execute coroutines in a queue-like manner. It offers several convenient methods for adding, removing, and executing tasks asynchronously using Kotlin [`coroutines`](https://github.com/Kotlin/kotlinx.coroutines). This library can be used in Android or any other platform supporting Kotlin coroutines.

## Usage
To use the `CoroutineQueue`, first add the following dependency to your project's `build.gradle` file:
```groovy
implementation "io.github.dk96-os.coroutines:queue:0.4.10"
```
After adding the dependency, you can use the `CoroutineQueue` class in your Kotlin code as shown below:

### Adding Tasks
To add tasks to the queue, use the `add` method of the `CoroutineQueue` instance. This method takes a `Deferred<T>` object that represents a coroutine result.
```kotlin
// Create an instance of CoroutineQueue
val queue = CoroutineQueue<Int>()

// Create a new coroutine
val task1 = async { /* ...do some work... */ }

// Add the coroutine to the queue
queue.add(task1)

// Simplify
queue.add(async { /* ...do some work... */ })
```

### Cancelling Tasks
To cancel tasks in the queue, use the `cancel` method of the `CoroutineQueue` instance. This method cancels all currently pending coroutines in the queue and returns the number of cancelled coroutines.
```kotlin
// val queue = CoroutineQueue()
val cancelCount = queue.cancel()
```

## CoroutineQueue Instance Methods
After constructing an instance, you can access the following regular methods.
___
- **`add(): Boolean`**
- **`cancel(): Int`**
- **`getCount(): Int`**
- **`getCapacity(): Int`**
___

To obtain the Coroutine results from the queue, you will need to use the following suspending methods.

**Note: These will suspend until the requested coroutines finish, or the queue becomes empty.**
___
- **`suspend awaitNext(): T`**
- **`suspend awaitList(limit: Int = 0): List<T>`**
- **`suspend awaitAll(limit: Int = 0): Int`**
___

### awaitNext()
Retrieves the next coroutine from the queue, and waits on it's `Deferred` result.
- Returns the coroutine result.

### awaitList()
Retrieves a list of coroutine results from the queue, with an optional size limit parameter.
- Returns a list of coroutine results.

### awaitAll()
Waits for all coroutines in the queue to finish, or an optional number of coroutines.
- Returns the number of coroutines waited for.

## Static Queue Methods
`CoroutineQueue` also provides convenient methods for processing items in a List or an array.
___
- **`suspend transformList(input: List<A>, transform: (A) -> B?): ArrayList<B>`**
- **`suspend transformArray(input: Array<A>, transform: (A) -> B?): ArrayList<B>`**
___
Both methods construct their own fixed size `CoroutineQueue` instances to process the input, and both return an ArrayList of the transformed data.
