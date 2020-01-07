package com.ebin.bookserverkotlin

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import androidx.core.content.ContextCompat.startActivity
import com.ebin.bookserverkotlin.objects.BookDetails

import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {


    private lateinit var listBooks: ListView

    private lateinit var progressFetching: ProgressBar

    lateinit var bookArray: ArrayList<BookDetails>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        progressFetching = findViewById<ProgressBar>(R.id.progressBarFetching)

        progressFetching.visibility = View.GONE

        listBooks = findViewById<ListView>(R.id.list_books)

        //For fetching data using Asyntask
        val fetchingTask = MyAsyncTask(this)
        fetchingTask.execute()


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    //Asyntask is using for fetching the Book list details from the Url
    class MyAsyncTask internal constructor(context: MainActivity) : AsyncTask<Int, String, String?>() {

        private var resp: String? = null
        private val activityReference: WeakReference<MainActivity> = WeakReference(context)

        override fun onPreExecute() {
            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return
            activity.progressFetching.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Int?): String? {

            val url = URL("http://tpbookserver.herokuapp.com/books")
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

//            parseDataFromJson(result, activity)
            parseDataFromJson(result, activity)

            val simpeAdapter = OurAdapter(activity)
            activity.listBooks.adapter = simpeAdapter

            activity.progressFetching.visibility = View.GONE

        }
//Function used to parse the JSON
        fun parseDataFromJson(result: String?, activity: MainActivity){

            val myJsonArray= JSONArray(result)

            activity.bookArray = ArrayList<BookDetails>()

            for (i in 0..myJsonArray.length()-1){

                val myJsonObject = myJsonArray.getJSONObject(i)

                val bookDetails = BookDetails()

                bookDetails.id = myJsonObject.getString("id")
                bookDetails.title = myJsonObject.getString("title")
                bookDetails.price = myJsonObject.getString("price")
                bookDetails.currencyCode = myJsonObject.getString("currencyCode")
                bookDetails.author = myJsonObject.getString("author")

                activity.bookArray.add(bookDetails)

            }
        }

        override fun onProgressUpdate(vararg text: String?) {

            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return

            Toast.makeText(activity, text.firstOrNull(), Toast.LENGTH_SHORT).show()

        }
    }
    // Asyntask Ending


    // Adapter is used to showing the data into the listView with LayoutInflator
    private class OurAdapter(context: MainActivity) : BaseAdapter() {
        private val mInflator: LayoutInflater

        private val activityReference: WeakReference<MainActivity> = WeakReference(context)
        val activity = activityReference.get()

        val arrayBooks = activity?.bookArray

        init {
            this.mInflator = LayoutInflater.from(context)
        }

        override fun getCount(): Int {
            if (arrayBooks != null) {
                return arrayBooks.size
            }
            else{
                return 0
            }
        }

        override fun getItem(position: Int): Any {
            return arrayBooks?.get(position)!!
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            val view: View?
            if (convertView == null) {
                view = this.mInflator.inflate(R.layout.each_book, parent, false)

                val tvTitle = view.findViewById<TextView>(R.id.tv_title)
                val tvAuthor = view.findViewById<TextView>(R.id.tv_author)
                val tvPrice = view.findViewById<TextView>(R.id.tv_price)

                if (arrayBooks != null) {
                    tvTitle.text = arrayBooks.get(position).title
                    tvAuthor.text = arrayBooks.get(position).author
                    tvPrice.text = arrayBooks.get(position).price+" "+arrayBooks.get(position).currencyCode
                }

                view.setOnClickListener {
                    val intent = Intent(activity, BookDetailsActivity::class.java)
                    intent.putExtra("idBook", arrayBooks?.get(position)?.id)
                    activity?.startActivity(intent)

                }

            } else {
                view = convertView
            }

            return view
        }
    }


}
