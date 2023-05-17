import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.example.hub_os_device.R

object ThemeManager {

    private val listeners = mutableSetOf<ThemeChangedListener>()
    var theme = Theme.LIGHT
        set(value) {
            field = value
            listeners.forEach { listener -> listener.onThemeChanged(value) }
        }

    interface ThemeChangedListener {
        fun onThemeChanged(theme: Theme)
        {
            setInitTheme(theme)
        }
        fun setInitTheme(theme: Theme = ThemeManager.theme)
        {

        }
    }

    fun addListener(listener: ThemeChangedListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: ThemeChangedListener) {
        listeners.remove(listener)
    }
}

enum class Theme(
    val mainGradientTheme: MainGradientTheme,
    val hubSettingsAppGradientTheme: HubSettingsAppGradientTheme,
    val menuAppsColorTheme: MenuAppsColorTheme,
    val dialogColorTheme: DialogColorTheme
) {
    LIGHT(
        mainGradientTheme = MainGradientTheme(R.drawable.main_gradient),
        hubSettingsAppGradientTheme = HubSettingsAppGradientTheme(R.drawable.hub_app_background_gradient),
        menuAppsColorTheme = MenuAppsColorTheme(R.color.hub_apps_background_color, R.color.black),
        dialogColorTheme = DialogColorTheme(R.color.dialogColor, R.color.black)
    ),
    DARK(
        mainGradientTheme = MainGradientTheme(R.drawable.main_gradient_night),
        hubSettingsAppGradientTheme = HubSettingsAppGradientTheme(R.drawable.hub_app_background_gradient_night),
        menuAppsColorTheme = MenuAppsColorTheme(R.color.hub_apps_background_color_night, R.color.white),
        dialogColorTheme = DialogColorTheme(R.color.dialogColorNight, R.color.white)
    ),
}

data class MainGradientTheme(
    @DrawableRes
    val drawableGradient: Int
)

data class HubSettingsAppGradientTheme(
    @DrawableRes
    val drawableGradient: Int
)

data class MenuAppsColorTheme(
    @DrawableRes
    val backgroundColor: Int,
    @ColorRes
    val textColor: Int
)

data class DialogColorTheme(
    @DrawableRes
    val backgroundColor: Int,
    @ColorRes
    val textColor: Int
)

