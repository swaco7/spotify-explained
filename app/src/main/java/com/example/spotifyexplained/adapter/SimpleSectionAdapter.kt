package com.example.spotifyexplained.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.cruxlab.sectionedrecyclerview.lib.SectionAdapter
import com.example.spotifyexplained.R
import com.example.spotifyexplained.databinding.StickyAdapterItemRowBinding
import com.example.spotifyexplained.model.enums.RecommendSeedType
import com.example.spotifyexplained.ui.recommend.spotify.combined.CombinedRecommendViewModel


class SimpleSectionAdapter(
    isHeaderVisible: Boolean = true,
    isHeaderPinned: Boolean = true,
    var items: List<Pair<String, String>>,
    val title: String,
    val viewModel: CombinedRecommendViewModel,
    val recommendSeedType: RecommendSeedType
) :
    SectionAdapter<SimpleSectionAdapter.MyItemViewHolder, SimpleSectionAdapter.MyHeaderViewHolder>(
        isHeaderVisible,
        isHeaderPinned,
    ) {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateItemViewHolder(parent: ViewGroup, type: Short): MyItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = StickyAdapterItemRowBinding.inflate(inflater, parent, false)
        return MyItemViewHolder(binding)
    }

    override fun onBindItemViewHolder(holder: MyItemViewHolder?, position: Int) {
        holder?.bindItem(items[position], viewModel, recommendSeedType)
    }

    class MyItemViewHolder(val binding: StickyAdapterItemRowBinding): ItemViewHolder(binding.root){
        var textView: TextView = itemView.findViewById(R.id.text)
        var imageView : ImageView = itemView.findViewById(R.id.selection_indicator_image)
        fun bindItem(item: Pair<String,String>, viewModel: CombinedRecommendViewModel, recommendSeedType: RecommendSeedType) {
            binding.name = item.first
            binding.selected = viewModel.selectedIds.value!!.contains(Pair(recommendSeedType, item.second))
            itemView.setOnClickListener {
                val selected = binding.selected
                if (selected!!) {
                    binding.selected = false
                    val selectedIds = viewModel.selectedIds.value!!
                    selectedIds.remove(Pair(recommendSeedType, item.second))
                    viewModel.selectedIds.value = selectedIds
                } else {
                    binding.selected = true
                    val selectedIds = viewModel.selectedIds.value!!
                    selectedIds.add(Pair(recommendSeedType, item.second))
                    viewModel.selectedIds.value = selectedIds
                }
            }
        }

    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): MyHeaderViewHolder? {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.sticky_adapter_header, parent, false)
        return MyHeaderViewHolder(view)
    }

    override fun onBindHeaderViewHolder(holder: MyHeaderViewHolder) {
        holder.bindHeader(title)
    }

    class MyHeaderViewHolder(itemView: View) : HeaderViewHolder(itemView) {
        var textView: TextView = itemView.findViewById(R.id.text)
        fun bindHeader(text: String?) {
            textView.text = text
        }

    }
}