package com.broto.projtracker.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.broto.projtracker.R
import com.broto.projtracker.models.Board
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.item_board.view.*

open class BoardItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Board>
): RecyclerView.Adapter<BoardItemsAdapter.BoardItemViewHolder>() {

    inner class BoardItemViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var boardImageView: CircleImageView = view.iv_item_board_image
        var boardName: TextView = view.tv_item_board_name
        var boardOwner: TextView = view.tv_item_board_created
    }

    interface ItemClickedListener {
        fun onClick(position: Int, model: Board)
    }

    private var mOnClickListener: ItemClickedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardItemViewHolder {
        return BoardItemViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_board, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BoardItemViewHolder, position: Int) {
        val model = list[position]

        Glide.with(context)
            .load(model.imageData)
            .centerCrop()
            .placeholder(R.drawable.ic_board_place_holder)
            .into(holder.boardImageView)
        holder.boardName.text = model.name
        holder.boardOwner.text = "Created By: ${model.createdBy}"

        holder.itemView.setOnClickListener {
            mOnClickListener?.onClick(position, model)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}