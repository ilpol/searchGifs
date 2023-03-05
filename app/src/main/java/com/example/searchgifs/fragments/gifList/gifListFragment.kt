package com.example.searchgifs.fragments.gifList

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.searchgifs.GifInfoViewModel
import com.example.searchgifs.GridGiphyRecyclerGifsAdaptor
import com.example.searchgifs.PaginationScrollListener
import com.example.searchgifs.R
import com.example.searchgifs.constants.NetworkConstants.Companion.giphyApiKey
import com.example.searchgifs.constants.NetworkConstants.Companion.giphyBaseUrl
import com.example.searchgifs.data.GifItem
import com.example.searchgifs.databinding.FragmentGifListBinding
import com.example.searchgifs.network.Network.Companion.fetchGifs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class ViewState {
    isError, isLoading, isNormal
}
class gifListFragment : Fragment() {

    private var _binding: FragmentGifListBinding? = null
    private val binding get() = _binding!!

    lateinit var gridGiphyRecyclerView: RecyclerView
    lateinit var errorView: LinearLayout
    lateinit var loadingView: ImageView

    var gifItems: List<GifItem> = listOf<GifItem>()
    var querySaved = "happy"

    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    var curOffset = 0
    val limit = 10
    var maxOffset = 25

    var isPageLoading = false

    private val gifInfoViewModel: GifInfoViewModel by activityViewModels()

    fun errorHandler() {
        lifecycleScope.launch(Dispatchers.Main) {
            swipeRefreshLayout.isRefreshing = false
            setView(ViewState.isError)
        }
    }

    fun onItemClick(gifItem: GifItem) {
        gifInfoViewModel.selectItem(gifItem)
        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
    }

    fun onGifsLoaded(gifItemsToSet: List<GifItem>, totalCount: Int) {
        maxOffset = totalCount
        setView(ViewState.isNormal)
        if (gifItemsToSet != null) {
            gifItems = gifItems?.plus(gifItemsToSet)!!
        }

        if (gridGiphyRecyclerView!= null) {
            activity?.let {
                (gridGiphyRecyclerView.adapter as GridGiphyRecyclerGifsAdaptor).setData(gifItems,
                    it
                )
            }
            (gridGiphyRecyclerView.adapter as GridGiphyRecyclerGifsAdaptor).notifyDataSetChanged()
        }
        isPageLoading = false
        swipeRefreshLayout.isRefreshing = false
    }

    fun loadNewItems(query: String) {
        fetchGifs(giphyBaseUrl + "search?q=" + query + "&api_key=" + giphyApiKey + "&fmt=json&offset=" + curOffset + "&limit=" + limit, callback = this::onGifsLoaded, onError=this::errorHandler)
    }

    fun setView(state: ViewState) {
        if (state == ViewState.isError) {
            gridGiphyRecyclerView.visibility = View.INVISIBLE
            loadingView.visibility = View.INVISIBLE
            errorView.visibility = View.VISIBLE
        } else if ((state == ViewState.isLoading)) {
            loadingView.visibility = View.VISIBLE
            gridGiphyRecyclerView.visibility = View.INVISIBLE
            errorView.visibility = View.INVISIBLE
        } else {
            gridGiphyRecyclerView.visibility = View.VISIBLE
            errorView.visibility = View.INVISIBLE
            loadingView.visibility = View.INVISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)
        loadNewItems(querySaved)

        _binding = FragmentGifListBinding.inflate(inflater, container, false)

        gridGiphyRecyclerView = binding.gifsGrid
        errorView = binding.errorView
        loadingView = binding.loading
        swipeRefreshLayout = binding.swipeToRefresh
        setView(ViewState.isLoading)

        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            deletePreviousItems()
            loadNewItems(querySaved)
        }

        gridGiphyRecyclerView.layoutManager = GridLayoutManager(activity, 2)


        gridGiphyRecyclerView.adapter =
            activity?.let { GridGiphyRecyclerGifsAdaptor(gifItems=gifItems, context = it, listener = this::onItemClick) }


        gridGiphyRecyclerView.addOnScrollListener(object :
            PaginationScrollListener(gridGiphyRecyclerView.layoutManager as GridLayoutManager) {
            override fun loadMoreItems() {
                curOffset += limit
                if (curOffset <= maxOffset) {
                    isPageLoading = true
                    loadNewItems(querySaved)
                }
            }

            override var isLastPage: Boolean = false
              get() = curOffset >= maxOffset
            override var isLoading: Boolean = false
              get() = isPageLoading
            })

            return binding.root
    }


    fun deletePreviousItems() {

        gifItems= listOf<GifItem>()
        curOffset = 0

        activity?.let {
            (gridGiphyRecyclerView.adapter as GridGiphyRecyclerGifsAdaptor).setData(gifItems,
                it
            )
        }
        (gridGiphyRecyclerView.adapter as GridGiphyRecyclerGifsAdaptor).notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        super.onCreateOptionsMenu(menu,inflater)

        val searchViewItem = menu.findItem(R.id.search_bar)
        val searchView: SearchView = MenuItemCompat.getActionView(searchViewItem) as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
          override fun onQueryTextSubmit(query: String?): Boolean {
              deletePreviousItems()
              if (query != null) {
                  querySaved = query
                  setView(ViewState.isLoading)
                  loadNewItems(querySaved)
              }

              return false
          }


          override fun onQueryTextChange(newText: String?): Boolean {
            return false
          }
    })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}