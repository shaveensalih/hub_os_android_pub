package com.example.hub_os_device.ui.mainActivity

import Theme
import ThemeManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.recyclerview.widget.RecyclerView
import com.example.hub_os_device.databinding.SplitTheBillRecyclerviewItemBinding
import com.example.hub_os_device.ui.components.NumberPicker
import com.example.hub_os_device.ui.mainActivity.viewModel.BillItemSelectedUIState
import com.example.hub_os_device.ui.mainActivity.viewModel.BillItemUIState
import com.example.hub_os_device.ui.mainActivity.viewModel.SplitTheBillFragmentViewModel

class SplitTheBillRecyclerViewAdapter(
    private val items: List<BillItemUIState>,
    private val viewModel: SplitTheBillFragmentViewModel
) : RecyclerView.Adapter<SplitTheBillRecyclerViewAdapter.SplitTheBillItemViewHolder>() {

    inner class SplitTheBillItemViewHolder(private val itemBinding: SplitTheBillRecyclerviewItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root), ThemeManager.ThemeChangedListener {
        fun bindItem(item: BillItemUIState) {
            ThemeManager.addListener(this)
            setInitTheme()
            itemBinding.itemNameTv.text =
                if (item.quantity == 1) item.name else item.name + " (x${item.quantity})"
            itemBinding.quantityNp.setMinMaxNumber(0, item.quantity)
            itemBinding.splitNp.setMinMaxNumber(1, 8)
            if(item.gifted == 1) {
                itemBinding.giftedTv.visibility = View.VISIBLE
                itemBinding.quantityVs.visibility = View.INVISIBLE
            }
            assignExtraTextviews(item.extras?.joinToString(), itemBinding.itemExtrasTv)
            assignExtraTextviews(item.notes, itemBinding.itemNotesTv)
            if (item.discountedPercentage != null)
                assignExtraTextviews("* ${item.discountedPercentage}% Discount", itemBinding.itemDiscountTv)

            itemBinding.quantityNp.setEventListener(object : NumberPicker.EventListener {
                override fun onCountChangedListener(count: Int) {
                    updateSelectedItems(item, quantityCount = count)
                    if (count == 0) {
                        itemBinding.quantityVs.showPrevious()
                        itemBinding.splitVs.visibility = View.INVISIBLE
                    }
                }
            })
            itemBinding.splitNp.setEventListener(object : NumberPicker.EventListener {
                override fun onCountChangedListener(count: Int) {
                    updateSelectedItems(item, splitCount = count)
                    if (count == 1) itemBinding.splitVs.showPrevious()
                }
            })

            switchVs(itemBinding.quantityAddButton, itemBinding.quantityVs, itemBinding.quantityNp)
            switchVs(itemBinding.splitButton, itemBinding.splitVs, itemBinding.splitNp)
        }

        private fun assignExtraTextviews(text: String?, tv: TextView) {
            if (text != null) {
                tv.text = text
                tv.visibility = View.VISIBLE
            }
        }

        private fun switchVs(view: View, vs: ViewSwitcher, np: NumberPicker) {
            view.setOnClickListener {
                vs.showNext()
                np.incrementCount(1)
                itemBinding.splitVs.visibility = View.VISIBLE
            }
        }

        private fun updateSelectedItems(
            item: BillItemUIState,
            splitCount: Int = itemBinding.splitNp.count,
            quantityCount: Int = itemBinding.quantityNp.count,
        ) {
            viewModel.changeSelectedItems(
                BillItemSelectedUIState(item.id, item.costSummary, quantitySelected = quantityCount, splitBetween = splitCount)
            )
        }

        override fun setInitTheme(theme: Theme) {
            super.setInitTheme(theme)
            itemBinding.itemNameTv.setTextColor(itemView.resources.getColor(theme.menuAppsColorTheme.textColor))
            itemBinding.itemExtrasTv.setTextColor(itemView.resources.getColor(theme.menuAppsColorTheme.textColor))
            itemBinding.itemNotesTv.setTextColor(itemView.resources.getColor(theme.menuAppsColorTheme.textColor))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SplitTheBillItemViewHolder {
        return SplitTheBillItemViewHolder(
            SplitTheBillRecyclerviewItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SplitTheBillItemViewHolder, position: Int) {
        val item = items[position]
        holder.bindItem(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }


}