package io.github.usefulness.rootbeer.sample.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.usefulness.rootbeer.sample.R
import io.github.usefulness.rootbeer.sample.RootItemResult
import io.github.usefulness.rootbeer.sample.databinding.ItemRootCheckBinding

class RootItemAdapter : ListAdapter<RootItemResult, RootItemAdapter.RootItemVH>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RootItemVH {
        val inflater = LayoutInflater.from(parent.context)
        return RootItemVH(ItemRootCheckBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: RootItemVH, position: Int) = holder.bind(getItem(position))

    class RootItemVH(private val binding: ItemRootCheckBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RootItemResult) {
            binding.rootItemText.setText(item.text)
            binding.rootItemResultIcon.update(isRooted = item.result)
        }

        private fun ImageView.showPass() = setImageResource(R.drawable.ic_check_circle_green_24dp)

        private fun ImageView.showFail() = setImageResource(R.drawable.ic_error_circle_outline_red_24dp)

        private fun ImageView.update(isRooted: Boolean) {
            if (isRooted) {
                showFail()
            } else {
                showPass()
            }
        }
    }

    object Diff : DiffUtil.ItemCallback<RootItemResult>() {
        override fun areItemsTheSame(oldItem: RootItemResult, newItem: RootItemResult) = oldItem.text == newItem.text

        override fun areContentsTheSame(oldItem: RootItemResult, newItem: RootItemResult) = oldItem == newItem
    }
}
