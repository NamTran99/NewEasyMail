package app.k9mail.core.android.common.data

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

object DataStoreHelper {
    private var context: Application? = null

    fun init(context: Application) {
        this.context = context
    }

    private val Context.dataStore by preferencesDataStore(name = "app_preferences")

    /**
     * Saves any type of data in DataStore.
     * @param key The key to save the value.
     * @param value The value to save, which can be String, Int, Boolean, Float, or Long.
     */
    suspend fun <T> saveData(key: String, value: T) {
        context?.dataStore?.edit { preferences ->
            when (value) {
                is String -> preferences[stringPreferencesKey(key)] = value
                is Int -> preferences[intPreferencesKey(key)] = value
                is Boolean -> preferences[booleanPreferencesKey(key)] = value
                is Float -> preferences[floatPreferencesKey(key)] = value
                is Long -> preferences[longPreferencesKey(key)] = value
                else -> throw IllegalArgumentException("Unsupported type")
            }
        }
    }

    /**
     * Reads any type of data from DataStore as a suspend function.
     * @param key The key to read the value.
     * @param defaultValue The default value if the key does not exist.
     * @return The value of the key if it exists, or the default value otherwise.
     */
    suspend fun <T> readData(key: String, defaultValue: T): T {
        val preferences = context?.dataStore?.data?.first()
        return when (defaultValue) {
            is String -> preferences?.get(stringPreferencesKey(key)) as T ?: defaultValue
            is Int -> preferences?.get(intPreferencesKey(key)) as T ?: defaultValue
            is Boolean -> preferences?.get(booleanPreferencesKey(key)) as T ?: defaultValue
            is Float -> preferences?.get(floatPreferencesKey(key)) as T ?: defaultValue
            is Long -> preferences?.get(longPreferencesKey(key)) as T ?: defaultValue
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    object Key{
        const val IS_APP_PURCHASED = "key_is_app_purchased"
    }

}
