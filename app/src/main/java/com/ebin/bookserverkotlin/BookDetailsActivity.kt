package com.ebin.bookserverkotlin

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_book_details.*
import kotlinx.android.synthetic.main.content_book_details.*
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL

class BookDetailsActivity : AppCompatActivity() {

    private lateinit var progressFetching: ProgressBar

//    private lateinit var tvBookTitle: TextView
//    private lateinit var tvBookAuthor: TextView
//    private lateinit var tvBookContent: TextView
//    private lateinit var tvBookPrice: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_details)
        setSupportActionBar(toolbar)

        progressFetching = findViewById<ProgressBar>(R.id.progressBar2)


        progressFetching.visibility = View.GONE

        var strIdBook:String = intent.getStringExtra("idBook")

        Log.d("Test", "Id from Intent "+strIdBook)

        val fetchingTask = BookDetailsActivity.MyAsyncTask(this)
        fetchingTask.execute(strIdBook)
    }

    //Asyntask is using for fetching the Book list details from the Url
    class MyAsyncTask internal constructor(context: BookDetailsActivity) : AsyncTask<String, String, String?>() {

        private var resp: String? = null
        private val activityReference: WeakReference<BookDetailsActivity> = WeakReference(context)

        override fun onPreExecute() {
            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return
            activity.progressFetching.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: String?): String? {

            Log.d("Test", "Id via Asyntask "+params[0])

            val url = URL("http://tpbookserver.herokuapp.com/book/"+params[0])
            val httpClient = url.openConnection() as HttpURLConnection
            if (httpClient.responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    val stream = BufferedInputStream(httpClient.inputStream)
                    val data: String = readStream(inputStream = stream)
                    return data
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    httpClient.disconnect()
                }
            } else {
                println("ERROR ${httpClient.responseCode}")
            }


            return null
        }

        fun readStream(inputStream: BufferedInputStream): String {
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            bufferedReader.forEachLine { stringBuilder.append(it) }
            return stringBuilder.toString()
        }


        override fun onPostExecute(result: String?) {

            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return

            parseDataFromJson(result, activity)

            activity.progressFetching.visibility = View.GONE

        }
        //Function used to parse the JSON
        fun parseDataFromJson(result: String?, activity: BookDetailsActivity){

            if(result!=null) {

                val myJsonObject = JSONObject(result)

                activity.tv_each_title.text = myJsonObject.getString("title")
                activity.tv_each_author.text = myJsonObject.getString("author")
                activity.tv_each_content.text = myJsonObject.getString("description")
                activity.tv_each_price.text =
                    myJsonObject.getString("price") + " " + myJsonObject.getString("currencyCode")
            }
            else{
                activity.tv_each_title.text = "Unexpected Error Retrieving book details"
            }

        }

        override fun onProgressUpdate(vararg text: String?) {

            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return

            Toast.makeText(activity, text.firstOrNull(), Toast.LENGTH_SHORT).show()

        }
    }
    // Asyntask Ending

}
