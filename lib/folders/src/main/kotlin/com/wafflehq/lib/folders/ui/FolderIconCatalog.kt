package com.wafflehq.lib.folders.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.wafflehq.lib.folders.R
import com.wafflehq.lib.uicore.components.IconPickerOption

data class FolderIconEntry(
    val key: String,
    val icon: ImageVector,
    @param:StringRes val labelRes: Int
)

object FolderIconCatalog {

    val entries: List<FolderIconEntry> = listOf(
        FolderIconEntry("folder", Icons.Default.Folder, R.string.folder_icon_folder),
        FolderIconEntry("folder_special", Icons.Default.FolderSpecial, R.string.folder_icon_folder_special),
        FolderIconEntry("star", Icons.Default.Star, R.string.folder_icon_star),
        FolderIconEntry("favorite", Icons.Default.Favorite, R.string.folder_icon_favorite),
        FolderIconEntry("bookmark", Icons.Default.Bookmark, R.string.folder_icon_bookmark),
        FolderIconEntry("label", Icons.AutoMirrored.Filled.Label, R.string.folder_icon_label),
        FolderIconEntry("flag", Icons.Default.Flag, R.string.folder_icon_flag),
        FolderIconEntry("lightbulb", Icons.Default.Lightbulb, R.string.folder_icon_lightbulb),
        FolderIconEntry("home", Icons.Default.Home, R.string.folder_icon_home),
        FolderIconEntry("work", Icons.Default.Work, R.string.folder_icon_work),
        FolderIconEntry("school", Icons.Default.School, R.string.folder_icon_school),
        FolderIconEntry("book", Icons.Default.Book, R.string.folder_icon_book),
        FolderIconEntry("description", Icons.Default.Description, R.string.folder_icon_description),
        FolderIconEntry("checklist", Icons.Default.Checklist, R.string.folder_icon_checklist),
        FolderIconEntry("event", Icons.Default.Event, R.string.folder_icon_event),
        FolderIconEntry("payments", Icons.Default.Payments, R.string.folder_icon_payments),
        FolderIconEntry("savings", Icons.Default.Savings, R.string.folder_icon_savings),
        FolderIconEntry("shopping_cart", Icons.Default.ShoppingCart, R.string.folder_icon_shopping_cart),
        FolderIconEntry("store", Icons.Default.Store, R.string.folder_icon_store),
        FolderIconEntry("receipt", Icons.Default.Receipt, R.string.folder_icon_receipt),
        FolderIconEntry("fitness", Icons.Default.FitnessCenter, R.string.folder_icon_fitness),
        FolderIconEntry("games", Icons.Default.SportsEsports, R.string.folder_icon_games),
        FolderIconEntry("health", Icons.Default.LocalHospital, R.string.folder_icon_health),
        FolderIconEntry("medication", Icons.Default.Medication, R.string.folder_icon_medication),
        FolderIconEntry("psychology", Icons.Default.Psychology, R.string.folder_icon_psychology),
        FolderIconEntry("science", Icons.Default.Science, R.string.folder_icon_science),
        FolderIconEntry("code", Icons.Default.Code, R.string.folder_icon_code),
        FolderIconEntry("build", Icons.Default.Build, R.string.folder_icon_build),
        FolderIconEntry("palette", Icons.Default.Palette, R.string.folder_icon_palette),
        FolderIconEntry("music", Icons.Default.MusicNote, R.string.folder_icon_music),
        FolderIconEntry("movie", Icons.Default.Movie, R.string.folder_icon_movie),
        FolderIconEntry("photo", Icons.Default.Photo, R.string.folder_icon_photo),
        FolderIconEntry("travel", Icons.Default.Flight, R.string.folder_icon_travel),
        FolderIconEntry("car", Icons.Default.DirectionsCar, R.string.folder_icon_car),
        FolderIconEntry("map", Icons.Default.Map, R.string.folder_icon_map),
        FolderIconEntry("pets", Icons.Default.Pets, R.string.folder_icon_pets),
        FolderIconEntry("park", Icons.Default.Park, R.string.folder_icon_park),
        FolderIconEntry("language", Icons.Default.Language, R.string.folder_icon_language),
        FolderIconEntry("group", Icons.Default.Group, R.string.folder_icon_group),
        FolderIconEntry("lock", Icons.Default.Lock, R.string.folder_icon_lock),
        FolderIconEntry("key", Icons.Default.Key, R.string.folder_icon_key),
        FolderIconEntry("cloud", Icons.Default.Cloud, R.string.folder_icon_cloud),
        FolderIconEntry("email", Icons.Default.Email, R.string.folder_icon_email),
        FolderIconEntry("trophy", Icons.Default.EmojiEvents, R.string.folder_icon_trophy),
        FolderIconEntry("breakfast", Icons.Default.FreeBreakfast, R.string.folder_icon_breakfast),
        FolderIconEntry("main_course", Icons.Default.DinnerDining, R.string.folder_icon_main_course),
        FolderIconEntry("soup", Icons.Default.RamenDining, R.string.folder_icon_soup),
        FolderIconEntry("salad", Icons.Default.Eco, R.string.folder_icon_salad),
        FolderIconEntry("snack", Icons.Default.LunchDining, R.string.folder_icon_snack),
        FolderIconEntry("dessert", Icons.Default.Icecream, R.string.folder_icon_dessert),
        FolderIconEntry("baking", Icons.Default.Cake, R.string.folder_icon_baking),
        FolderIconEntry("drink", Icons.Default.LocalBar, R.string.folder_icon_drink),
        FolderIconEntry("sauce", Icons.Default.WaterDrop, R.string.folder_icon_sauce),
        FolderIconEntry("grill", Icons.Default.OutdoorGrill, R.string.folder_icon_grill),
        FolderIconEntry("restaurant", Icons.Default.Restaurant, R.string.folder_icon_restaurant),
        FolderIconEntry("fastfood", Icons.Default.Fastfood, R.string.folder_icon_fastfood),
        FolderIconEntry("cookie", Icons.Default.Cookie, R.string.folder_icon_cookie),
        FolderIconEntry("bakery", Icons.Default.BakeryDining, R.string.folder_icon_bakery),
        FolderIconEntry("seafood", Icons.Default.SetMeal, R.string.folder_icon_seafood),
        FolderIconEntry("hot_beverage", Icons.Default.EmojiFoodBeverage, R.string.folder_icon_hot_beverage)
    )

    private val byKey: Map<String, FolderIconEntry> = entries.associateBy { it.key }

    fun resolve(key: String?): ImageVector? = key?.let { byKey[it]?.icon }

    @Composable
    fun pickerOptions(): List<IconPickerOption> = entries.map {
        IconPickerOption(key = it.key, icon = it.icon, label = stringResource(it.labelRes))
    }
}
