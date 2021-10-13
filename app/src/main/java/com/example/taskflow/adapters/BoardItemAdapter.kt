package com.example.taskflow.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.taskflow.R
import com.example.taskflow.models.Board
import kotlinx.android.synthetic.main.item_board.view.*


open class BoardItemAdapter(private val context: Context,
                             private var list: ArrayList<Board>):
RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyviewHolder(LayoutInflater.from(context).inflate(R.layout.item_board,parent,false))
    }

    override fun getItemCount(): Int {
      return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model= list[position]
        if(holder is MyviewHolder) {
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.image_holder)
                .into(holder.itemView.iv_board_image)

            holder.itemView.tv_board_name.text= model.name
            holder.itemView.tv_created_by.text="Created by: ${model.createdBy}"

            holder.itemView.setOnClickListener{
                    if(onClickListener != null){
                        onClickListener!!.onClick(position,model)
                    }
            }
        }
    }
    interface OnClickListener{
        fun onClick(position: Int,model:Board)
    }
    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }
    private class MyviewHolder(view: View):RecyclerView.ViewHolder(view)
}