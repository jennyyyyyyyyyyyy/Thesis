package com.google.mediapipe.examples.gesturerecognizer

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.tasks.vision.core.RunningMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import thesis.filconnected.GestureRecognizerHelper
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(AndroidJUnit4::class)
class GestureRecognizerTest {

    private companion object {
        private const val TEST_IMAGE = "hand_thumb_up.jpg"
        private const val TEST_VIDEO = "test_video.mp4"
    }

    private val expectedCategoriesForImageAndLiveStreamMode = listOf(
        Category.create(0.8105f, 0, "Thumb_Up", ""),
    )

    private val expectedCategoryForVideoMode = listOf(
        Category.create(0.8193482f, 0, "Thumb_Up", ""),
    )
    private lateinit var lock: ReentrantLock
    private lateinit var condition: Condition

    @Before
    fun setup() {
        lock = ReentrantLock()
        condition = lock.newCondition()
    }

    /**
     * Verify that the result returned from the Gesture Recognizer Helper with
     * LIVE_STREAM mode is within the acceptable range to the expected result.
     */
    @Test
    @Throws(Exception::class)
    fun recognizerLiveStreamModeResultFallsWithinAcceptedRange() {
        var recognizerResult: GestureRecognizerHelper.ResultBundle? = null
        val gestureRecognizerHelper =
            GestureRecognizerHelper(context = ApplicationProvider.getApplicationContext(),
                runningMode = RunningMode.LIVE_STREAM,
                gestureRecognizerListener = object :
                    GestureRecognizerHelper.GestureRecognizerListener {
                    override fun onError(error: String, errorCode: Int) {
                        // Print out the error
                        println(error)

                        // Release the lock
                        lock.withLock {
                            condition.signal()
                        }
                    }

                    override fun onResults(resultBundle: GestureRecognizerHelper.ResultBundle) {
                        recognizerResult = resultBundle

                        // Release the lock and start verifying the result
                        lock.withLock {
                            condition.signal()
                        }
                    }

                    override fun onPoseAligned() {
                        TODO("Not yet implemented")
                    }

                    override fun onPoseMisaligned() {
                        TODO("Not yet implemented")
                    }
                })

        // Create Bitmap and convert to MPImage.


        // Lock to wait the GestureRecognizerHelper return the value.
        lock.withLock {
            condition.await()
        }

        // Verify that the recognizer is not null
        assertNotNull(recognizerResult)

        // Expecting one hand for this test case
        val categories = recognizerResult!!.results.first().gestures().first()

        // Verify that the categories are not empty
        assert(categories.isNotEmpty())

        // Verify that the scores are correct
        assertEquals(
            expectedCategoriesForImageAndLiveStreamMode.first().score(),
            categories.first().score(),
            0.01f
        )

        // Verify that the category names are consistent
        assertEquals(
            expectedCategoriesForImageAndLiveStreamMode.first().categoryName(),
            categories.first().categoryName()
        )
    }
}

    /**
     * Verify that the result returned from the Gesture Recognizer Helper with
     * VIDEO mode is within the acceptable range to the expected result.
     */

    /**
     * Verify that the result returned from the Gesture Recognizer Helper with
     * IMAGE mode is within the acceptable range to the expected result.
     */



