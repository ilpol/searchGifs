package com.example.searchgifs

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.searchgifs.data.GifItem

class GridGiphyRecyclerGifsAdaptor(
                                   private var gifItems: List<GifItem>,
                                   private var context: Context,
                                   private val listener: (GifItem) -> Unit
                                       ) :
    RecyclerView.Adapter<GridGiphyRecyclerGifsAdaptor.MyViewHolder>()  {
    var gifItemsInner = gifItems
    var contextInner = context
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val titleTextView: TextView = itemView.findViewById(R.id.title)
        val gifImageView: ImageView = itemView.findViewById(R.id.gif)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.gif_item, parent, false)
        return MyViewHolder(itemView)
    }

    fun setData(gifItem: List<GifItem>, context: Context) {
        gifItemsInner = gifItem
        contextInner = context
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val curTitle = gifItemsInner?.get(position)?.title
        val curGifUrl= gifItemsInner?.get(position)?.url

        holder.titleTextView.text = curTitle

        holder.itemView.setOnClickListener {
            gifItemsInner?.get(position)?.let { it1 -> listener(it1) }
        }


        Glide.with(context).asDrawable()
            .load(curGifUrl)
            .thumbnail(Glide.with(context).load(R.drawable.loader_placeholder))
            .into(holder.gifImageView);
    }

    override fun getItemCount(): Int {
        if (gifItemsInner?.size!= null) {
            return gifItemsInner!!.size
        }

        return 0
    }
}

abstract class PaginationScrollListener(private val layoutManager: LinearLayoutManager) :
    RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        if (!isLoading && !isLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                && firstVisibleItemPosition >= 0
            ) {
                loadMoreItems()
            }
        }
    }

    protected abstract fun loadMoreItems()
    abstract var isLastPage: Boolean
    abstract var isLoading: Boolean
}
