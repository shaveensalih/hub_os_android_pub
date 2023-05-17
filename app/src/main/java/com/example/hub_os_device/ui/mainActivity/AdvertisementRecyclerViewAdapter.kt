import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.RecyclerviewAdImageItemBinding
import com.example.hub_os_device.databinding.RecyclerviewAdVideoItemBinding
import com.example.hub_os_device.ui.mainActivity.viewModel.AdvertisementUIState

enum class ImageSize {
    SMALL,
    MEDIUM,
    LARGE
}

class AdvertisementRecyclerViewAdapter constructor(
    val context: Context,
    val imageSize: ImageSize = ImageSize.LARGE,
    val ads: List<AdvertisementUIState>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ImageAdViewHolder(private val itemBinding: RecyclerviewAdImageItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItem(advertisement: AdvertisementUIState?) {
            itemBinding.root.tag = this
            //Getting image from network and binding it to imageview
            when (imageSize) {
                ImageSize.LARGE -> {
                    loadImage(
                        itemView.context,
                        advertisement?.primaryImgUrl,
                        itemBinding.adImage
                    )
                }
                ImageSize.MEDIUM -> {
                    loadImage(itemView.context, advertisement?.secondaryImgUrl, itemBinding.adImage)
                }
                else -> {
                    loadImage(
                        itemView.context,
                        advertisement?.bannerImgUrl,
                        itemBinding.adImage
                    )
                }
            }
//
        }
    }

    inner class VideoAdViewHolder(val itemBinding: RecyclerviewAdVideoItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItem(advertisement: AdvertisementUIState?) {
            itemBinding.root.tag = this

            val interval: Long = 0
            val options: RequestOptions = RequestOptions().frame(interval)
            Glide.with(context).asBitmap()
                .load(advertisement?.videoUrl!!)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .apply(options)
                .into(itemBinding.thumbnail)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentAd = getCurrentAd(position)
        return if (currentAd?.videoUrl != null && imageSize == ImageSize.LARGE) 1 else 0
    }

    fun loadImage(context: Context, url: String?, adImage: ImageView) {
        Glide.with(context)
            .load(url)
            .transition(GenericTransitionOptions.with(R.anim.fade_in))
            .into(adImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> VideoAdViewHolder(
                RecyclerviewAdVideoItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> ImageAdViewHolder(
                RecyclerviewAdImageItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //Getting correct ad
        val currentAd = getCurrentAd(position)
        if (currentAd?.videoUrl != null && imageSize == ImageSize.LARGE) (holder as VideoAdViewHolder).bindItem(
            currentAd
        )
        else (holder as ImageAdViewHolder).bindItem(currentAd)
    }

    private fun getCurrentAd(position: Int): AdvertisementUIState? {
        //TODO - check this
        if (ads.isEmpty()) return null
        val positionInList: Int = position % (ads.size)
        return ads[positionInList]
    }

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }


}