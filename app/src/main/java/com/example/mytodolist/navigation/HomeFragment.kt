package com.example.mytodolist.navigation

import android.animation.ObjectAnimator
import android.app.Activity.RESULT_OK
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.Button
import android.widget.ImageSwitcher
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mytodolist.*
import com.example.mytodolist.Adapter.TodoAdapter
import com.example.mytodolist.databinding.FragmentHomeBinding
import com.example.mytodolist.databinding.TodoItemBinding
import com.example.mytodolist.model.MyResponse
import com.example.mytodolist.model.TodoListData
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val RESULT_TEST = 2
private const val RESULT_SEARCH = 4

class HomeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var homeBinding: FragmentHomeBinding
    private lateinit var mainActivity: MainActivity
    private var todoAdapter : TodoAdapter? = null
    private var dataPosition = 0 //????????? ???????????? ?????????????????? ?????????
    private var checkBoxPosition = 0
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)
    //?????? ??? item??? ????????? ???????????? ?????????
    private var isLoading = false
    //?????? item
    private var data : MutableList<TodoListData?> = mutableListOf()
    //?????? ????????? ????????????
    var tempData : TodoListData? = null
    private lateinit var tempDataList : List<TodoListData> //reponse?????? ?????? ?????? ?????????
    private var tempDataList2 : MutableList<TodoListData?> = mutableListOf() //tempdatalist??? ???????????? ?????? ??????
    //?????????
    private lateinit var searchTempDataList : List<TodoListData>
    private var searchData : MutableList<TodoListData?> = mutableListOf()
    private var searchDataTest : MutableList<TodoListData?> = mutableListOf()
    private var filterData : MutableList<TodoListData?> = mutableListOf()
    lateinit var filterString : ArrayList<String>
    private var searchView: ImageView? = null
    private var firstSearchPage = 0
    private var lastSearchPage = 5
    //mode ?????????
    /*private var isSelect : Boolean = true //true-light, false-dark
    private var mode : ImageSwitcher? = null*/
    private var switch : SwitchCompat? = null
    //????????????
    var sharedPref : SharedPref? = null
    //fab ?????????
    private var isFabOpen = false
    //?????? ?????? ????????????
    private lateinit var callback : OnBackPressedCallback
    var backPressedTime : Long = 0
    //?????????
    val retrofit = Retrofit.Builder().baseUrl("https://waffle.gq")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service = retrofit.create(TodoInterface::class.java)
    //????????? ?????????
    var page = 0 //?????? ?????????
    var totalPages = 0
    var isLastPage = false
    var searchPage = 0 //?????? ?????????
    var lPage = 0
    //id = ??? ???????????? ????????? ????????? ??? ??????????????? ?????? ??????
    var idPosition = 0
    var isDataLoading = false
    var shimmer : ShimmerFrameLayout? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeBinding = FragmentHomeBinding.inflate(inflater,container,false)
        //tempData = data

        sharedPref = this.context?.let { SharedPref(it) }
        if (sharedPref!!.loadNightModeState()) {
            context?.setTheme(R.style.darktheme)
        } else {
            context?.setTheme(R.style.AppTheme)
        }


        //option = mainActivity.getSharedPreferences("option", MODE_PRIVATE)
        /*model.getAll().observe(this, Observer{
            noticeAdapter.setList(it.content)

            // ??? ???????????? ???????????? 10?????? ????????????.
            // ????????? ???????????? ?????????????????? ?????? ????????? (????????? ????????? ????????????)
            noticeAdapter.notifyItemRangeInserted((page - 1) * 10, 10)
        })*/

        //??? ?????? ??? ????????? ??????


        //????????? ?????? shimmer
        shimmer = homeBinding.rvShimmer
        //dataSet()
        //?????? ????????? ???????????? ????????????
        //?????????
        /*
        shimmer!!.startShimmer()
        do {
            dataSet()
        } while (data.size < 0)
        * ?????? ????????????
        */
        do {
            lifecycleScope.launch {
                shimmer!!.startShimmer()
                delay(3000)
                dataSet()
                //searchDataSet()
            }
        } while (data.size < 0)

        initRecyclerView()
        initSwipeRefrech()
        initScrollListener()
        //??????(search) ????????????
        setHasOptionsMenu(true)

        //fabmenu
        homeBinding.fabMenu.setOnClickListener {
            toggleFab()
        }

        //?????? ?????? ?????? ??????????????? ??????
        homeBinding.fabMode.setOnClickListener {
            val intent = Intent(activity, TemporaryStorageActivity::class.java).apply {
                putExtra("type", "delete")
                putExtra("item", todoAdapter!!.testData)
            }

            requestActivity.launch(intent)
        }

        //type?????? ???????????? ???????????? ????????????
        homeBinding.fabAdd.setOnClickListener {
            val intent = Intent(activity, EditActivity::class.java).apply {
                putExtra("type","ADD")
            }
            requestActivity.launch(intent)
            todoAdapter!!.notifyDataSetChanged()
        }

        //???????????? ?????? ??? ???????????????
        /*?????? ?????????????????? ??????*/
        val reflashBtn : Button = homeBinding.reflashButton
        reflashBtn.setOnClickListener {

            //?????????
            /*data.clear()

            //todoAdapter!!.listData = dataSet()

            todoAdapter!!.notifyDataSetChanged()

            homeBinding.recyclerView.startLayoutAnimation()*/
            //dataSet()
            println("data"+data)
            println("filter"+filterData)
            println("searchData $searchData")
            println("test $searchDataTest")
            println("totalpage $totalPages")
        }



        //???????????? ?????? ???
        todoAdapter!!.setItemCheckBoxClickListener(object : TodoAdapter.ItemCheckBoxClickListener{
            override fun onClick(view: View, position: Int, itemId: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    val todoListData = data[position]
                    checkBoxPosition = position
                    data[checkBoxPosition]!!.isChecked = todoListData!!.isChecked

                    todoListData.isChecked = !todoListData.isChecked
                }
                todoAdapter!!.notifyDataSetChanged()
            }
        })

        //recyclerview item?????? ???
        todoAdapter!!.setItemClickListener(object :TodoAdapter.ItemClickListener{
            override fun onClick(view: View, position: Int, itemId: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    val todo = data[position]
                    dataPosition = position
                    val intent = Intent(activity, EditActivity::class.java).apply {
                        putExtra("type", "EDIT")
                        putExtra("item", todo)
                    }
                    requestActivity.launch(intent)
                }
            }
        })

        val swipeHelperCallback = SwipeHelperCallback(todoAdapter!!).apply {
            //???????????? ??? ???????????? ??????
            setFix(resources.displayMetrics.widthPixels.toFloat() / 4)
        }
        ItemTouchHelper(swipeHelperCallback).attachToRecyclerView(homeBinding.recyclerView)

        //?????? ??? ?????? ??? ?????? ??? ??????
        homeBinding.recyclerView.setOnTouchListener { _,_ ->
            swipeHelperCallback.removePreviousFix(homeBinding.recyclerView)
            false
        }
        //???????????? ?????????
        /*val btn = homeBinding.test
        btn.setOnCheckedChangeListener {_, isChecked ->
            if (isChecked) {
                sharedPref!!.setNightModeState(true)
                restartApp()
            } else {
                sharedPref!!.setNightModeState(false)
                restartApp()
            }
        }*/

        return homeBinding.root
    }
    /*fragment ?????? ?????? ?????????*/
    //fragment??? ?????? ?????? ??? ??? ??????
    //Fragement ??? Activity ??? attach ??? ??? ??????
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (System.currentTimeMillis() - backPressedTime < 2500) {
                    activity?.finish()
                    return
                }
                Toast.makeText(activity, "??? ??? ??? ????????? ?????? ???????????????.",Toast.LENGTH_SHORT).show()
                backPressedTime = System.currentTimeMillis()
            }
        }
        activity?.onBackPressedDispatcher!!.addCallback(this, callback)
        //mainActivity = context as MainActivity
    }
    //??? ??????
    //Replace ????????? backward ??? Fragment ??? ???????????? ?????? ???????????? ??????
    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.toolbar_menu_item, menu)
        //val item = menu?.findItem(R.id.menu_action_search)



        //val searchImageView : MenuItem = menu.findItem(R.id.menu_action_search)
        val selectMode : MenuItem = menu.findItem(R.id.select_mode)

        /*if (searchImageView != null) {
            //val searchET = searchView!!.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            //searchET.hint = "Search.."
            //////////
            /*searchView = searchItem.actionView as SearchView
            searchView!!.queryHint = "Search.."
            searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                //???????????? ????????? ??????, ?????? ??????????????? ???????????? ??????x
                override fun onQueryTextSubmit(query: String?): Boolean {
                    //todoAdapter!!.filter.filter(query)
                    return false
                }

                //????????? ??????/?????? ??? ??????
                override fun onQueryTextChange(s: String?): Boolean {
                    if (s != null) {
                        if (s.isNotEmpty()) {
                            todoAdapter!!.filter.filter(s)
                        //todoAdapter!!.filter.filter(s)
                        } else {
                            //todoAdapter!!.filterContent.addAll(tempData)
                            //todoAdapter!!.filter.filter(s)
                            println(todoAdapter!!.filterContent)
                        }
                    }
                    return false
                }
            })
        }*/
            ///////////
            /*searchView = searchImageView.actionView as ImageView
            searchView!!.setOnClickListener {
                val intent = Intent(activity, SearchActivity::class.java).apply {
                    putExtra("page", totalPages)
                }
                requestActivity.launch(intent)
            }*/
        }*/
        //?????? ?????? ????????? ????????? ????????????
        switch = selectMode.actionView as SwitchCompat
        if (sharedPref!!.loadNightModeState()) {
            switch!!.isChecked = true
        }
        switch!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sharedPref!!.setNightModeState(true)
                restartApp()
            } else {
                sharedPref!!.setNightModeState(false)
                restartApp()
            }
        }

        /*mode = selectMode.actionView as ImageSwitcher
        if (sharedPref!!.loadNightModeState()) {
            mode!!.isSelected = true
        }
        mode!!.setOnClickListener {
            if (isSelect) {
                isSelect = false
                mode!!.setImageResource(R.drawable.ic_baseline_light_mode_24)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                restartApp()
            } else {
                isSelect = true
                mode!!.setImageResource(R.drawable.ic_baseline_nightlight_round_24)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                restartApp()
            }
        }*/

        /*mode!!.setOnClickListener {
            if (isSelect) {
                isSelect = false
                mode!!.setImageResource(R.drawable.ic_baseline_nightlight_round_24)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                mode!!.setImageResource(R.drawable.ic_baseline_light_mode_24)
                isSelect = true
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }*/


        return super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_action_search -> {
                //Toast.makeText(activity, "click", Toast.LENGTH_SHORT).show()
                val intent = Intent(activity, SearchActivity::class.java).apply {
                    putExtra("page", totalPages)
                }
                requestActivity.launch(intent)
                true
            }

            else -> false
        }
        return super.onOptionsItemSelected(item)
    }

    //?????? ?????? ??? ????????? ?????? ?????????
    fun restartApp() {
        val intent = Intent(context?.applicationContext, MainActivity::class.java)
        activity?.startActivity(intent)
        activity?.finish()
    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        when (it.resultCode) {
            RESULT_OK -> {
                //getSerializableExtra = intent??? ?????? ????????? ???????????????
                //?????? ????????? ????????? ????????? Serializable????????? ??????????????? as??? ??????????????????
                val todo = it.data?.getSerializableExtra("todo") as TodoListData

                when(it.data?.getIntExtra("flag", -1)) {
                    //add
                    0 -> {
                        dataSet()
                        /*CoroutineScope(Dispatchers.IO).launch {
                            //data.add(todo)
                            //????????? ????????? ??? ????????????

                        }*/
                        //todoAdapter!!.notifyDataSetChanged()
                        Toast.makeText(activity, "?????????????????????.", Toast.LENGTH_SHORT).show()
                    }
                    //edit
                    1 -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            data[dataPosition] = todo
                        }
                        dataSet()
                        Toast.makeText(activity, "?????????????????????.", Toast.LENGTH_SHORT).show()
                    }
                }
                todoAdapter!!.notifyDataSetChanged()

            }
            //??????????????? ?????? ????????? ?????????
            RESULT_TEST -> {
                val delete = it.data?.getSerializableExtra("DELETE") as ArrayList<TodoListData?>

                when(it.data?.getIntExtra("flag",-2)) {
                    2 -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            todoAdapter!!.testData = delete
                        }
                    }
                }
                todoAdapter!!.notifyDataSetChanged()
            }
            //search
            RESULT_SEARCH -> {
                shimmer!!.visibility = View.VISIBLE
                shimmer!!.startShimmer()
                val searchQuery = it.data?.getSerializableExtra("SEARCH") as String?

                searchDataSet()

                when(it.data?.getIntExtra("flag",-3)) {
                    4 -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            filterData.clear()

                            val filterString = searchQuery.toString().lowercase(Locale.getDefault()).trim {it < ' '}
                            /*for (item in searchData) {
                                if (item!!.content!!.lowercase(Locale.getDefault()).contains(filterString)) {
                                    filterData.add(item)
                                }
                            }*/
                            for (searchItem in searchDataTest) {
                                if (searchItem!!.content!!.lowercase(Locale.getDefault()).contains(filterString)) {
                                    //println("tem - $tem")
                                    filterData.add(searchItem)
                                }
                            }
                            /*filterData = searchData.filter {
                                it!!.content == searchQuery
                            } as MutableList<TodoListData?>*/
                            if (filterData.isEmpty()) {
                                homeBinding.searchNotTv.visibility = View.VISIBLE
                                todoAdapter!!.listData = filterData
                            } else {
                                homeBinding.searchNotTv.visibility = View.GONE
                                todoAdapter!!.listData = filterData
                            }
                        }
                        println("search"+searchData)
                    }
                }
                todoAdapter!!.notifyDataSetChanged()
            }
        }
    }
    private fun searchDataSet() {
        val retrofit = Retrofit.Builder().baseUrl("https://waffle.gq")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(TodoInterface::class.java)

        /*if (isLastPage) {
            println(lPage)
        } else {
            for (i in firstSearchPage..lastSearchPage) {
                service.getDataByPage(i, 5).enqueue(object : Callback<MyResponse> {
                    override fun onResponse(call: Call<MyResponse>, response: Response<MyResponse?>) {
                        if (response.isSuccessful) {
                            //????????? ??????????????? ????????????
                            //shimmer?????? ??? ????????????
                            shimmer!!.stopShimmer()
                            shimmer!!.visibility = View.GONE
                            //homeBinding.recyclerView.visibility = View.VISIBLE
                            //?????? ??????
                            searchTempDataList = response.body()!!.data.todos
                            isLastPage = response.body()!!.data.paging.is_last_page
                            println("$i $searchTempDataList")
                            println("f"+firstSearchPage)
                            println("l"+lastSearchPage)
                            searchTempDataList.forEach {
                                //data.add(it)
                                //searchData.add(it)
                                searchDataTest.add(it)
                            }
                            if (!isLastPage) {
                                firstSearchPage = lastSearchPage
                                lastSearchPage += 5
                            } else {
                                lPage = i
                            }
                        } else {
                            //?????? ??????
                            println("Fail")
                            isDataLoading = false
                        }
                    }

                    override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                        // ?????? ?????? (????????? ??????, ?????? ?????? ??? ??????????????? ??????)
                        isDataLoading = false
                        println("Error" + t.message.toString())
                    }
                })
            }
        }*/
        for (i in 0..totalPages) {
            service.getDataByPage(i, 5).enqueue(object : Callback<MyResponse> {
                override fun onResponse(call: Call<MyResponse>, response: Response<MyResponse?>) {
                    if (response.isSuccessful) {
                        //????????? ??????????????? ????????????
                        //shimmer?????? ??? ????????????
                        shimmer!!.stopShimmer()
                        shimmer!!.visibility = View.GONE
                        //homeBinding.recyclerView.visibility = View.VISIBLE
                        //?????? ??????
                        searchTempDataList = response.body()!!.data.todos
                        isLastPage = response.body()!!.data.paging.is_last_page
                        println("$i $searchTempDataList")
                        println(isLastPage)
                        searchTempDataList.forEach {
                            //data.add(it)
                            //searchData.add(it)
                            searchDataTest.add(it)
                        }
                    } else {
                        //?????? ??????
                        println("Fail")
                        isDataLoading = false
                    }
                }

                override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                    // ?????? ?????? (????????? ??????, ?????? ?????? ??? ??????????????? ??????)
                    isDataLoading = false
                    println("Error" + t.message.toString())
                }
            })
        }

    }

    //????????? ???????????? ???
    private fun dataSet() {
        val retrofit = Retrofit.Builder().baseUrl("https://waffle.gq")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(TodoInterface::class.java)

        //page?????? ??????
        service.getDataByPage(0, 5).enqueue(object : Callback<MyResponse> {
            override fun onResponse(call: Call<MyResponse>, response: Response<MyResponse?>) {
                if (response.isSuccessful) {
                    //????????? ??????????????? ????????????
                    //shimmer?????? ??? ????????????
                    shimmer!!.stopShimmer()
                    shimmer!!.visibility = View.GONE
                    //homeBinding.recyclerView.visibility = View.VISIBLE
                    //?????? ??????
                    data.clear()
                    tempDataList = response.body()!!.data.todos
                    totalPages = response.body()!!.data.paging.total_pages
                    val c = response.body()!!.data.paging.current_page
                    val l = response.body()!!.data.paging.is_last_page
                    tempDataList.forEach {
                        tempDataList2.add(it)
                        //data.add(it)
                        searchData.add(it)
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        for (list in tempDataList) {
                            data.add(list)
                        }
                    }
                    todoAdapter!!.notifyDataSetChanged()
                    println(data)
                    println("t"+tempDataList)
                    println("page $totalPages")
                    println("current $c")
                    println("last $l")
                    /*idPosition = tempDataList2.size
                    for (i in 0 until idPosition) {
                        data[i]!!.position = i
                    }*/

                    //println(tempDataList2)

                } else {
                    //?????? ??????
                    println("Fail")
                    isDataLoading = false
                }

            }

            override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                // ?????? ?????? (????????? ??????, ?????? ?????? ??? ??????????????? ??????)
                isDataLoading = false
                println("Error" + t.message.toString())
            }
        })

    }

    private fun initSwipeRefrech() {
        homeBinding.refreshSwipeLayout.setOnRefreshListener {
            //???????????? ??? ????????????????????????
            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) // ?????? ?????? ????????? ??????
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                dataSet()
                //?????? ????????? ???????????? ???????????? ????????????
                todoAdapter!!.listData = data
                homeBinding.recyclerView.startLayoutAnimation()
                homeBinding.refreshSwipeLayout.isRefreshing = false
                todoAdapter!!.notifyDataSetChanged()
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }, 1000)
            //??????????????? ??????ss
            //activity?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    private fun initRecyclerView() {
        todoAdapter = TodoAdapter()
        todoAdapter!!.listData = data
        todoAdapter!!.filterContent = data
        todoAdapter!!.tempServerData = tempDataList2
        homeBinding.recyclerView.adapter = todoAdapter
        //???????????? ????????? ??????
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        homeBinding.recyclerView.setHasFixedSize(true)
        homeBinding.recyclerView.layoutManager = manager
    }

    private fun initScrollListener(){
        homeBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutm = homeBinding.recyclerView.layoutManager as LinearLayoutManager
                //????????? ????????? ????????? ???????????? position
                // ???????????? ????????? ???????????? ??? ?????? -1
                //???????????? ???????????? ???????????? ????????? ???????????????
                if (!isLoading) {
                    if (!homeBinding.recyclerView.canScrollVertically(1)) {
                        //????????? data??? ????????? 5??? ?????? ?????? ??????
                        if (data.size == 5) {
                            isLoading = true
                            getMoreItem()
                        }
                    }
                }
                if (!isLoading) {
                    if (layoutm.findLastCompletelyVisibleItemPosition() == data.size-1) {
                        isLoading = true
                        getMoreItem()
                    }
                }
            }
        })
    }

    //fab ?????? ???????????????
    private fun toggleFab() {
        if (isFabOpen) {
            ObjectAnimator.ofFloat(homeBinding.fabAdd, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(homeBinding.fabMode, "translationY", 0f).apply { start() }
            homeBinding.fabMenu.setImageResource(android.R.drawable.ic_input_add)
        } else {
            ObjectAnimator.ofFloat(homeBinding.fabAdd, "translationY", -200f).apply { start() }
            ObjectAnimator.ofFloat(homeBinding.fabMode, "translationY", -400f).apply { start() }
            homeBinding.fabMenu.setImageResource(android.R.drawable.ic_delete)
        }
        isFabOpen = !isFabOpen
    }

    private fun getMoreItem() {
        data.add(null)
        //null??? ?????? ?????????
        //??? ????????? ????????????????????? ??????????????? ??????
        homeBinding.recyclerView.adapter!!.notifyItemInserted(data.size-1)
        /*data.removeAt(data.size-1)
        val currentSize = data.size
        for (i in currentSize + 1 until currentSize+5) {
            data.add(TodoListData(i, "test $i", false))
        }
        homeBinding.recyclerView.adapter!!.notifyDataSetChanged()
        isLoading = false*/
        /////////////////////
        /*val runnable = kotlinx.coroutines.Runnable {
            data.add(null) //null??? ???????????? ?????? ??????
            todoAdapter!!.notifyItemInserted(data.size-1)
        }
        homeBinding.recyclerView.post(runnable)

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            val runnable2 = Runnable{
                //null?????? ???
                data.removeAt(data.size-1)
                //??????????????? ????????????
                val scrollPos = data.size
                todoAdapter!!.notifyItemRemoved(scrollPos)
                var currentSize = scrollPos //?????? data??????
                var next = currentSize + 5 //?????? data????????? 5???????????? ?????????????????? ??????

                //?????? ?????????????????? data??? ?????????-5?????? ?????????
                if (currentSize < data.size-10) {
                    while (currentSize-1 < next) {
                        //??????????????? ?????? ?????????
                        data.add(tempData[currentSize])
                        currentSize++
                    }
                } else {
                    while (currentSize != tempData.size) {
                        data.add(tempData[currentSize])
                        currentSize++
                    }
                }

                if (data.get(dataPosition) == null) {
                    data.removeAt(dataPosition)
                }
                todoAdapter!!.update(data)
                isLoading = false
            }
            runnable2.run()
            println(data)
        }*/
        //??????//
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(java.lang.Runnable {
            //null????????? ??? ??????
            data.removeAt(data.size - 1)
            //val scrollPosition: Int = data.size
            //todoAdapter!!.notifyItemRemoved(scrollPosition)
            //var currentSize = data.size
            //nextLimit??? +5??? ????????? ???????????? ?????????
            page += 1
            //data.clear()
            service.getDataByPage(page, 5).enqueue(object : Callback<MyResponse> {
                override fun onResponse(call: Call<MyResponse>, response: Response<MyResponse?>) {
                    if (response.isSuccessful) {
                        //?????? ??????
                        tempDataList = response.body()!!.data.todos
                        tempDataList.forEach {
                            tempDataList2.add(it)
                            data.add(it)
                        }
                        todoAdapter!!.notifyDataSetChanged()
                        println("success")
                    } else {
                        //?????? ??????
                        println("Fail")
                    }
                }

                override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                    // ?????? ?????? (????????? ??????, ?????? ?????? ??? ??????????????? ??????)
                    println("Error" + t.message.toString())
                }
            })
            /*idPosition = tempDataList2.size
            for (i in 0 until idPosition) {
                data[i]!!.position = i
            }*/
            println(data)
            isLoading = false
        }, 2000)
    }








    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}