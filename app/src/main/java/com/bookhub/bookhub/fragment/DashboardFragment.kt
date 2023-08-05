package com.bookhub.bookhub.fragment

import android.app.Activity
import android.app.AlertDialog
import com.android.volley.Request

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bookhub.bookhub.R
import com.bookhub.bookhub.adapter.DashboardRecyclerAdapter
import com.bookhub.bookhub.model.Book
import com.bookhub.bookhub.util.ConnectionManager
import org.json.JSONException
import java.util.Collections


class DashboardFragment : Fragment() {
    lateinit var recyclerDashboard: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: DashboardRecyclerAdapter
    lateinit var progressBar: ProgressBar
    lateinit var layoutProgressBar: RelativeLayout
    val bookInfoList = arrayListOf<Book>(
    )
    val ratingComparator = Comparator<Book>{
        book1,book2 ->
       if( book1.rating.compareTo(book2.rating,true)==0){
           book1.rating.compareTo(book2.name,true)
       }else{
           book1.rating.compareTo(book2.rating,true)
       }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        setHasOptionsMenu(true)

        recyclerDashboard = view.findViewById(R.id.recyclerDashboard)
        progressBar = view.findViewById(R.id.progressBar)
        layoutProgressBar = view.findViewById(R.id.layoutProgressBar)
        layoutProgressBar.visibility = View.VISIBLE



        layoutManager = LinearLayoutManager(activity)

        try {
            layoutProgressBar.visibility = View.GONE
            if (ConnectionManager().checkConnectivity(activity as Context)) {
                val queue = Volley.newRequestQueue(activity as Context)
                val url = "http://13.235.250.119/v1/book/fetch_books/"

                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    { response ->
                        val success = response.getBoolean("success")
                        if (success) {
                            val data = response.getJSONArray("data")
                            for (i in 0 until data.length()) {
                                val bookJsonObject = data.getJSONObject(i)
                                val bookObject = Book(
                                    bookJsonObject.getString("book_id"),
                                    bookJsonObject.getString("name"),
                                    bookJsonObject.getString("author"),
                                    bookJsonObject.getString("rating"),
                                    bookJsonObject.getString("price"),
                                    bookJsonObject.getString("image")
                                )
                                bookInfoList.add(bookObject)

                                recyclerAdapter =
                                    DashboardRecyclerAdapter(activity as Context, bookInfoList)

                                recyclerDashboard.adapter = recyclerAdapter
                                recyclerDashboard.layoutManager = layoutManager

//                               recyclerDashboard.addItemDecoration(
//                                   DividerItemDecoration(
//                                       recyclerDashboard.context, (layoutManager as LinearLayoutManager).orientation
//                                   )
//                               )
                            }
                        } else {
                            Toast.makeText(
                                activity as Context,
                                "Some error occured",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    { error ->
                        if (activity != null) {
                            Toast.makeText(
                                activity as Context,
                                "Volley error occurred",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                ) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "46d5c7e970d114"
                        return headers
                    }
                }
                queue.add(jsonObjectRequest)
            } else {
                if (activity != null) {
                    //internet not available
                    val dialog = AlertDialog.Builder(activity as Context)
                    dialog.setTitle("Error")
                    dialog.setMessage("Internet Connection not Found")
                    dialog.setPositiveButton("Open Internet Settings") { text, listener ->
                        val settingIntent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                        startActivity(settingIntent)
                        activity?.finish()

                    }
                    dialog.setNegativeButton("Exit") { text, listener ->
                        ActivityCompat.finishAffinity(activity as Activity)
                    }
                    dialog.create()
                    dialog.show()
                }
            }
        } catch (E: JSONException) {
            Toast.makeText(
                activity as Context,
                "Some unexpected error occurred!!!",
                Toast.LENGTH_LONG
            ).show()
        }






        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater?.inflate(R.menu.menu_dashboard, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        var id  = item?.itemId
        if(id == R.id.action_sort){
            Collections.sort(bookInfoList,ratingComparator)
            bookInfoList.reverse()
        }
        recyclerAdapter.notifyDataSetChanged()

        return super.onOptionsItemSelected(item)
    }

}