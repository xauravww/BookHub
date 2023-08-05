package com.bookhub.bookhub.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bookhub.bookhub.R
import com.bookhub.bookhub.database.BookDatabase
import com.bookhub.bookhub.database.BookEntity
import com.bookhub.bookhub.util.ConnectionManager
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.lang.Exception

class DescriptionActivity : AppCompatActivity() {

    lateinit var txtBookName: TextView
    lateinit var txtBookAuthor: TextView
    lateinit var txtBookPrice: TextView
    lateinit var txtBookRating: TextView
    lateinit var txtBookDesc: TextView
    lateinit var imgBookImage: ImageView
    lateinit var btnAddToFav: Button
    lateinit var progressBar: ProgressBar
    lateinit var progressLayout: RelativeLayout
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    var book_id: String? = "100"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        txtBookName = findViewById(R.id.txtBookName)
        txtBookAuthor = findViewById(R.id.txtBookAuthor)
        txtBookPrice = findViewById(R.id.txtBookPrice)
        txtBookRating = findViewById(R.id.txtBookRating)
        imgBookImage = findViewById(R.id.imgBookImage)
        btnAddToFav = findViewById(R.id.btnAddToFav)
        progressBar = findViewById(R.id.progressBar)
        progressLayout = findViewById(R.id.progressLayout)
        txtBookDesc = findViewById(R.id.txtBookDesc)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Book Details"

        progressBar.visibility = View.VISIBLE
        progressLayout.visibility = View.VISIBLE

        if (intent != null) {
            book_id = intent.getStringExtra("book_id")
        } else {
            finish()
            Toast.makeText(
                this@DescriptionActivity,
                "Some unexpected error occurred",
                Toast.LENGTH_SHORT
            ).show()
        }
        if (book_id == "100") {
            finish()
            Toast.makeText(
                this@DescriptionActivity,
                "Some unexpected error occurred",
                Toast.LENGTH_SHORT
            ).show()
        }

        val queue = Volley.newRequestQueue(this@DescriptionActivity)
        val url = "http://13.235.250.119/v1/book/get_book/"
        val jsonParams = JSONObject()
        jsonParams.put("book_id", book_id)

        if (ConnectionManager().checkConnectivity(this@DescriptionActivity)) {
            val jsonRequest = object : JsonObjectRequest(
                Method.POST,
                url,
                jsonParams,
                Response.Listener { response ->
                    try {
                        val success = response.getBoolean("success")
                        if (success) {
                            val bookJsonObject = response.getJSONObject("book_data")
                            progressBar.visibility = View.GONE
                            progressLayout.visibility = View.GONE
                            val bookImageUrl = bookJsonObject.getString("image")
                            Picasso.get().load(bookJsonObject.getString("image"))
                                .error(R.drawable.default_book_cover).into(imgBookImage)
                            txtBookName.text = bookJsonObject.getString("name")
                            txtBookAuthor.text = bookJsonObject.getString("author")
                            txtBookRating.text = bookJsonObject.getString("rating")
                            txtBookPrice.text = bookJsonObject.getString("price")
                            txtBookDesc.text = bookJsonObject.getString("description")

                            //object of the BookEntity class to conduct database operations
                            val bookEntity = BookEntity(
                                book_id?.toInt() as Int,
                                txtBookName.text.toString(),
                                txtBookAuthor.text.toString(),
                                txtBookRating.text.toString(),
                                txtBookPrice.text.toString(),
                                txtBookDesc.text.toString(),
                                bookImageUrl
                            )
                            //in mode 1 we check for the entity present or not that we already defined in bottom of our code
                            val checkFav = DBAsyncTask(applicationContext,bookEntity,1).execute()
                            //now getting boolean value from the checkFav
                            val isFav = checkFav.get()

                            //below code is just to set Color and Text when we get first time response from the server
                            //but we need to add a btn to do it manually also
                            if(!isFav){
                                btnAddToFav.text ="Add to Favourites"
                                //we pass this object favColor in the setBackgroundColor( ) method
                                val favColor = ContextCompat.getColor(applicationContext,R.color.colorFavourite)
                               btnAddToFav.setBackgroundColor(favColor)
                            }else{
                                btnAddToFav.text ="Remove from Favourites"
                                val noFavColor = ContextCompat.getColor(applicationContext,R.color.colorPrimary)
                                btnAddToFav.setBackgroundColor(noFavColor)
                            }
                            //now doing the color/text change using clickListener
                            btnAddToFav.setOnClickListener {
                                //when book not in favourites
                                if(!DBAsyncTask(applicationContext,bookEntity,1).execute().get()){
                                       val async = DBAsyncTask(applicationContext,bookEntity,2).execute()
                                        val result = async.get()

                                        if(result){
                                            Toast.makeText(this@DescriptionActivity,"Book added to favourites",Toast.LENGTH_SHORT).show()
                                            btnAddToFav.text = "Remove from favourites"
                                            val favColor =ContextCompat.getColor(applicationContext,R.color.colorPrimary)
                                            btnAddToFav.setBackgroundColor(favColor)
                                        }else{
                                            Toast.makeText(this@DescriptionActivity,"Some error occurred",Toast.LENGTH_SHORT).show()
                                        }
                                }
                                //if book was already added to favourites
                                else{
                                    //mode 3 will remove the book from favourites
                                    val async = DBAsyncTask(applicationContext,bookEntity,3).execute()
                                    val result = async.get()

                                    if(result){
                                        Toast.makeText(this@DescriptionActivity,"Book removed from favourites",Toast.LENGTH_SHORT).show()
                                        btnAddToFav.text = "Add to favourites"
                                        val favColor =ContextCompat.getColor(applicationContext,R.color.colorFavourite)
                                        btnAddToFav.setBackgroundColor(favColor)
                                    }else{
                                        Toast.makeText(this@DescriptionActivity,"Some error occurred",Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(
                                this@DescriptionActivity,
                                "Some error occurred",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (E: Exception) {
                        Toast.makeText(
                            this@DescriptionActivity,
                            "Some error occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                Response.ErrorListener { error ->
                    Toast.makeText(
                        this@DescriptionActivity,
                        "Volley error $error",
                        Toast.LENGTH_SHORT
                    ).show()
                }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "46d5c7e970d114"
                    return headers
                }

            }
            queue.add(jsonRequest)
        } else {
            //internet not available
            val dialog = AlertDialog.Builder(this@DescriptionActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection not Found")
            dialog.setPositiveButton("Open Internet Settings") { text, listener ->
                val settingIntent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                startActivity(settingIntent)
                finish()
                dialog.setNegativeButton("Exit") { text, listener ->
                    ActivityCompat.finishAffinity(this@DescriptionActivity)
                }
                dialog.create()
                dialog.show()
            }

        }
    }

    class DBAsyncTask(val context: Context, val bookEntity: BookEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {
        /*
        Mode 1 -> Check DB if the Book is favourite or not
        Mode 2-> Save the book into DB as favourite
        Mode 3-> Remove the favourite book

         */

        val db = Room.databaseBuilder(context, BookDatabase::class.java, "books-db").build()
        override fun doInBackground(vararg params: Void?): Boolean {

            when (mode) {
                1 -> {
                    //check DB if the book is favourite or not
                    val book: BookEntity? = db.bookDao().getBookById(bookEntity.book_id.toString())
                    db.close()
                    return book != null
                }

                2 -> {
                    //save the book into DB as favourite
                    db.bookDao().insertBook(bookEntity)
                    db.close()
                    return true
                }

                3 -> {
                    //remove the favourite book
                    db.bookDao().deleteBook(bookEntity)
                    db.close()
                    return true
                }
            }
            return false
        }

    }
}