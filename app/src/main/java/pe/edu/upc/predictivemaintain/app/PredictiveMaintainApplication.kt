package pe.edu.upc.predictivemaintain.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class PredictiveMaintainApplication : Application(), ImageLoaderFactory {

    @Inject
    lateinit var okHttpClient: Lazy<OkHttpClient>

    /**
     * Work order evidence (TK-19) is served only with `Authorization: Bearer`, there is no public URL. Coil
     * therefore uses the app's OkHttp client, whose AuthInterceptor adds the token to every request.
     */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .okHttpClient { okHttpClient.get() }
            .build()
}
