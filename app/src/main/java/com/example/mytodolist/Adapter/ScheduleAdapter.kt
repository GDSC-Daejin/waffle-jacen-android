package com.example.mytodolist.Adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mytodolist.R
import com.example.mytodolist.databinding.ItemScheduleBinding
import com.example.mytodolist.databinding.TodoItemBinding
import com.example.mytodolist.databinding.ViewitemScheduleBinding
import com.example.mytodolist.model.ScheduleData
import com.example.mytodolist.model.TodoListData

class ScheduleAdapter : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>(){
    private lateinit var binding : ViewitemScheduleBinding
    //page마다 값이 달라져야하니 , map으로 해서 key값으로 page, value로 tv1~4개가 맞는듯?
    var scheduleData = mutableListOf<ScheduleData?>()
    var testScheduleData = HashMap<String, ArrayList<ScheduleData?>>()
    private lateinit var context : Context

    inner class ScheduleViewHolder(private val binding : ViewitemScheduleBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        private val setDate : String = ""
        var tv_date = binding.scheduelTv
        fun bind(scheduleData: ScheduleData, position : Int) {
            this.position = position
            tv_date.text = scheduleData.scheduleText
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        context = parent.context
        binding = ViewitemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(scheduleData[position]!!, position)
        val content : ScheduleData = scheduleData[position]!!
        //holder.tv_date.text = content.scheduleText
    }

    override fun getItemCount(): Int {
        return scheduleData.size
    }

}