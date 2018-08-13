package com.example.martin.myapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity(), LogView {

    companion object {
        private const val fileName: String = "log.txt"
    }

    override fun displayError(message: String) {
        Toast.makeText(
                this,
                "Stop putting in empty or blank text",
                Toast.LENGTH_LONG)
                .show()
    }

    private val logAdapter: LogAdapter = LogAdapter()
    private val presenter: LogPresenter  by lazy {
        LogPresenter(this, filesDir.absolutePath.plus("/$fileName"))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        log_recycler_view.adapter = logAdapter
        log_button.setOnClickListener {
            presenter.log(log_edit_text.text.toString(), MainActivity::class.java.simpleName)
        }
    }

    override fun addToScreen(message: String) {
        logAdapter.update(message)
        log_edit_text.text.clear()
    }
}

class LogAdapter: RecyclerView.Adapter<StringViewHolder>(){
    private var logMessageList = mutableListOf<String>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): StringViewHolder =
            StringViewHolder(TextView(viewGroup.context))

    override fun getItemCount(): Int = logMessageList.size

    override fun onBindViewHolder(viewHolder: StringViewHolder, position: Int) {
        viewHolder.bind(logMessageList[position])
    }

    fun update(message: String){
        logMessageList.add(message)
        notifyItemInserted(logMessageList.size - 1)
    }
}

class StringViewHolder(private val textView: TextView): RecyclerView.ViewHolder(textView){
    fun bind(message: String){
        textView.text = message
    }
}

class LogPresenter(private val view: LogView,
                   private val assetFilePath: String,
                   private val fileLogger: Logger = FileLogger(File(assetFilePath))
){

    fun log(message: String, className: String) {
        if(message.isBlank()){
            view.displayError("You are a bad person")
        } else {
            val loggerMessage = LoggerMessage(
                    className = className,
                    message = message,
                    event = LoggerEvent.INFO)
            fileLogger.log(loggerMessage)
            view.addToScreen(message)
        }
    }
}

interface LogView{
    fun addToScreen(message: String)
    fun displayError(message: String)
}

interface Logger {
    fun log(message: LoggerMessage)
}

class ConsoleLogger(private val tag: String = ConsoleLogger::class.java.simpleName) : Logger {
    override fun log(message: LoggerMessage) {
        with(message) {
            when (event) {
                LoggerEvent.DEBUG -> Log.d(tag, toString())
                LoggerEvent.INFO -> Log.i(tag, toString())
                LoggerEvent.ERROR -> Log.e(tag, toString())
            }
        }
    }
}

class FileLogger(private val file: File): Logger {

    override fun log(message: LoggerMessage) {
        try {
            file.bufferedWriter().use { out -> out.append(message.toString()) }
        } catch (exceptions: Exception){
            exceptions.printStackTrace()
        }
    }
}

enum class LoggerEvent {
    DEBUG,
    INFO,
    ERROR;
}

data class LoggerMessage(val timeStamp: Long = System.currentTimeMillis(),
                         val className: String,
                         val message: String,
                         val event: LoggerEvent){

    override fun toString(): String = "@$timeStamp, class: $className, message: $message"
}