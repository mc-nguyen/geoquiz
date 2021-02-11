package cpp.baevee.geoquiz

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlin.math.ceil

private const val TAG = "MainActivity"
private const val KEY_INDEX = "index"
private const val REQUEST_CODE_CHEAT = 0

class MainActivity : AppCompatActivity() {
    private lateinit var trueButton: Button
    private lateinit var falseButton: Button
    private lateinit var cheatButton: Button
    private lateinit var nextButton: ImageButton
    private lateinit var previousButton: ImageButton
    private lateinit var questionTextView: TextView

    private val quizViewModel: QuizViewModel by lazy {
        ViewModelProviders.of(this).get(QuizViewModel::class.java)
    }

    private var correct = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called")
        setContentView(R.layout.activity_main)

        val currentIndex = savedInstanceState?.getInt(KEY_INDEX, 0) ?: 0
        quizViewModel.currentIndex = currentIndex
        val provider: ViewModelProvider = ViewModelProviders.of(this)
        val quizViewModel = provider.get(QuizViewModel::class.java)
        Log.d(TAG, "Got a QuizViewModel: $quizViewModel")

        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        cheatButton = findViewById(R.id.cheat_button)
        nextButton = findViewById(R.id.next_button)
        previousButton = findViewById(R.id.previous_button)
        questionTextView = findViewById(R.id.question_text_view)

        trueButton.setOnClickListener {view: View ->
            trueButton.isEnabled = false
            falseButton.isEnabled = false
            cheatButton.isEnabled = false
            checkAnswer(true)
        }

        falseButton.setOnClickListener { view: View ->
            trueButton.isEnabled = false
            falseButton.isEnabled = false
            cheatButton.isEnabled = false
            checkAnswer(false)
        }

        cheatButton.setOnClickListener {
            val answerIsTrue = quizViewModel.currentQuestionAnswer
            val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue)
            startActivityForResult(intent, REQUEST_CODE_CHEAT)
            cheatButton.isEnabled = false
        }

        nextButton.setOnClickListener {
            if (quizViewModel.currentIndex == quizViewModel.answerRecord.lastIndex) {
                nextButton.isEnabled = false
                val dialogBuilder = AlertDialog.Builder(this)
                val score = 100 * ceil(correct / quizViewModel.answerRecord.size.toDouble())
                dialogBuilder.setMessage("Done! $score%")
                    .setCancelable(false)
                    .setPositiveButton("Thank You!", DialogInterface.OnClickListener { dialog, id -> finish()})
                    .setNegativeButton("Back", DialogInterface.OnClickListener { dialog, id -> dialog.cancel()}
                )

                val alert = dialogBuilder.create()
                alert.setTitle("Submitted")
                alert.show()
            }
            else {
                quizViewModel.moveToNext()
                updateQuestion()

                if (quizViewModel.answerRecord[quizViewModel.currentIndex] == -1) {
                    trueButton.isEnabled = true
                    falseButton.isEnabled = true
                    cheatButton.isEnabled = true
                } else {
                    trueButton.isEnabled = false
                    falseButton.isEnabled = false
                    cheatButton.isEnabled = false
                }
            }
        }

        previousButton.setOnClickListener{
            if (currentIndex > 0) {
                quizViewModel.moveToPrevious()
                updateQuestion()

                if (quizViewModel.answerRecord[quizViewModel.currentIndex] == -1) {
                    trueButton.isEnabled = true
                    falseButton.isEnabled = true
                    cheatButton.isEnabled = true
                } else {
                    trueButton.isEnabled = false
                    falseButton.isEnabled = false
                    cheatButton.isEnabled = false
                }
            }
        }

        updateQuestion()
    }

    override fun onActivityResult(requestCode: Int,
                                  resultCode: Int,
                                  data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == REQUEST_CODE_CHEAT) {
            quizViewModel.isCheater =
                data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        Log.i(TAG, "onSaveInstanceState")
        savedInstanceState.putInt(KEY_INDEX, quizViewModel.currentIndex)
    }
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }

    private fun updateQuestion() {
        val questionTextResId = quizViewModel.currentQuestionText
        questionTextView.setText(questionTextResId)
    }

    private fun checkAnswer(userAnswer: Boolean) {
        val correctAnswer = quizViewModel.currentQuestionAnswer
        val messageResId = when {
            quizViewModel.isCheater -> R.string.judgment_toast
            userAnswer == correctAnswer -> R.string.correct_toast
            else -> R.string.incorrect_toast
        }

        if (messageResId == R.string.correct_toast) correct += 1
        quizViewModel.answerRecord[quizViewModel.currentIndex] = messageResId
        val toast = Toast.makeText(
                this,
                messageResId,
                Toast.LENGTH_SHORT
        )
        toast.setGravity(Gravity.TOP, 0, 0)
        toast.show()
    }
}