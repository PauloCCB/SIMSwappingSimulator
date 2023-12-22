package com.pe.simswappingsimulator.components
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient

class GetAdvertisingIdTask(private val context: Context,
                           private val listener: GetAdvertisingIdListener) : AsyncTask<Void, Void, String>() {

    override fun doInBackground(vararg params: Void?): String {
        return try {
            val advertisingId =  AdvertisingIdClient.getAdvertisingIdInfo(context)
            advertisingId.toString()
        } catch (e: Exception) {
            // Manejar la excepción según tus necesidades
            ""
        }
    }

    override fun onPostExecute(result: String) {
        // Aquí puedes manejar el resultado después de obtener el ID de publicidad
        // Ten en cuenta que este método se ejecutará en el hilo principal
        // y puedes actualizar la interfaz de usuario aquí si es necesario.
        listener.onGetAdvertisingId(result)
    }
}
interface GetAdvertisingIdListener {
    fun onGetAdvertisingId(advertisingId: String)
}