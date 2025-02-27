import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import thesis.filconnected.databinding.ItemGestureRecognizerResultBinding
import com.google.mediapipe.tasks.components.containers.Category
import java.util.Locale
import kotlin.math.min

class GestureRecognizerResultsAdapter(private val context: Context) :
    RecyclerView.Adapter<GestureRecognizerResultsAdapter.ViewHolder>() {

    companion object {
        private const val NO_VALUE = "--"
        private const val STATIC_GESTURE_DELAY = 100L
        private const val MIN_CONFIDENCE_THRESHOLD = 0.6f // Adjust as needed
    }

    private var adapterCategories: MutableList<Category?> = mutableListOf()
    private var adapterSize: Int = 0
    var sentence: StringBuilder = StringBuilder("")
    private val handler = Handler(Looper.getMainLooper())
    private val pendingGestures: MutableMap<String, Int> = mutableMapOf() // Tracks the current position in the sequence
    private var staticGesture: String? = null
    private val staticGestureHandler = Handler(Looper.getMainLooper())
    private lateinit var textToSpeech: TextToSpeech
    private var isTTSInitialized = false
    private var queuedText: String? = null

    init {
        initializeTTS(context)
    }

    fun initializeTTS(context: Context) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.US
                isTTSInitialized = true
                queuedText?.let { speak(it) }
                queuedText = null
            } else {
                Log.e("TTS", "TextToSpeech initialization failed")
                isTTSInitialized = false
            }
        }
    }

    fun shutdownTTS() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.shutdown()
            isTTSInitialized = false
        }
    }

    private fun speak(text: String) {
        if (isTTSInitialized && !text.isNullOrEmpty()) {
            Log.d("TTS", "Speaking: $text")
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, null)
        } else {
            Log.e("TTS", "TextToSpeech is not initialized or text is null/empty")
            queuedText = text
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateResults(categories: List<Category>?) {
        // Clear the adapter categories initially
        adapterCategories = MutableList(adapterSize) { null }

        // Check if the categories list is null or empty
        if (categories.isNullOrEmpty()) {
            Log.d("GestureRecognizer", "No valid categories detected.")
            return
        }

        // Filter out low-confidence predictions
        val filteredCategories = categories.filter { it.score() >= MIN_CONFIDENCE_THRESHOLD }
        if (filteredCategories.isEmpty()) {
            Log.d("GestureRecognizer", "All predictions below confidence threshold.")
            return
        }

        // Sort categories by descending confidence score
        val sortedCategories = filteredCategories.sortedByDescending { it.score() }

        // Find the closest matching gesture
        val closestMatch = findClosestGestureMatch(sortedCategories)
        if (closestMatch == null) {
            Log.d("GestureRecognizer", "No valid closest gesture match found.")
            return
        }

        // Update the adapter categories
        val min = min(sortedCategories.size, adapterCategories.size)
        for (i in 0 until min) {
            adapterCategories[i] = sortedCategories[i]
        }
        notifyDataSetChanged()

        // Process the closest match for sentence building
        val predictedText = closestMatch.categoryName()
        if (!predictedText.isNullOrEmpty()) {
            val baseText = extractBaseText(predictedText)
            val numericSuffix = extractNumericSuffix(predictedText)

            if (numericSuffix != null) {
                // Handle dynamic gestures
                val currentPosition = pendingGestures[baseText] ?: 0
                if (numericSuffix == currentPosition + 1) {
                    // Update the current position in the sequence
                    pendingGestures[baseText] = numericSuffix

                    // Check if this is the final gesture in the sequence
                    if (isFinalGesture(baseText, numericSuffix)) {
                        updateSentenceAndSpeak(baseText)
                        pendingGestures.remove(baseText) // Clear the sequence after recognition
                    }
                } else {
                    // Reset the sequence if the gesture is out of order
                    pendingGestures[baseText] = numericSuffix
                }
            } else {
                // Handle static gestures
                if (staticGesture != predictedText) {
                    staticGesture = predictedText
                    staticGestureHandler.postDelayed({
                        if (staticGesture == predictedText) {
                            updateSentenceAndSpeak(predictedText)
                        }
                    }, STATIC_GESTURE_DELAY)
                }
            }
        }
    }

    private fun extractBaseText(input: String): String {
        return input.replace(Regex("\\d+$"), "") // Remove trailing numeric suffix
    }

    private fun extractNumericSuffix(input: String): Int? {
        return input.replace(Regex("[^\\d]"), "").toIntOrNull() // Extract numeric suffix
    }

    private fun findClosestGestureMatch(categories: List<Category>): Category? {
        // Define a dataset of known gestures (this can be replaced with your actual dataset)
        val knownGestures = listOf(
            "A",
            "B",
            "C",
            "D",
            "GOOD MORNING",
            "GOOD AFTERNOON"
        )

        // Find the closest match by comparing category names with known gestures
        return categories.maxByOrNull { category ->
            val categoryName = category.categoryName().lowercase()
            val matchScore = knownGestures.map { knownGesture ->
                calculateSimilarity(categoryName, knownGesture)
            }.maxOrNull() ?: 0.0f
            matchScore * category.score() // Combine similarity score with confidence
        }
    }

    private fun calculateSimilarity(input: String, target: String): Float {
        // Simple similarity metric: Levenshtein distance normalized by length
        val maxLength = maxOf(input.length, target.length).toFloat()
        val distance = levenshteinDistance(input, target)
        return 1 - (distance / maxLength)
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // Deletion
                    dp[i][j - 1] + 1,      // Insertion
                    dp[i - 1][j - 1] + cost // Substitution
                )
            }
        }
        return dp[s1.length][s2.length]
    }

    private fun isFinalGesture(baseText: String, numericSuffix: Int): Boolean {
        // Determine if the current gesture is the final one in the sequence
        // For example, if the dataset contains "goodafternoon1", "goodafternoon2", "goodafternoon3",
        // then the final gesture for "goodafternoon" is "goodafternoon3".
        // You can implement this logic based on your dataset.
        return numericSuffix > 1 && !pendingGestures.containsKey("$baseText${numericSuffix + 1}")
    }

    private fun updateSentenceAndSpeak(newText: String) {
        sentence.append("$newText ")
        notifyLabelUpdate()
        speak(newText)
    }

    private fun notifyLabelUpdate() {
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    fun updateAdapterSize(size: Int) {
        adapterSize = size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGestureRecognizerResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        adapterCategories[position]?.let { category ->
            holder.bind(category.categoryName(), category.score())
        }
    }

    override fun getItemCount(): Int = adapterCategories.size

    inner class ViewHolder(private val binding: ItemGestureRecognizerResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(label: String?, score: Float?) {
            with(binding) {
                tvLabel.text = sentence.toString()
                tvLabel.setHorizontallyScrolling(true)
                tvLabel.isHorizontalScrollBarEnabled = true
                tvLabel.isSelected = true
                tvLabel.post {
                    val textLength = tvLabel.layout?.getLineWidth(0)?.toInt() ?: 0
                    val scrollWidth = tvLabel.width
                    if (textLength > scrollWidth) {
                        tvLabel.scrollTo(textLength - scrollWidth, 0)
                    }
                }
//                tvScore.text = if (score != null) String.format(Locale.US, "%.2f", score) else NO_VALUE
            }
        }
    }
}

