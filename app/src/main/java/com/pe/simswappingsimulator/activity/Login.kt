package com.pe.simswappingsimulator.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pe.simswappingsimulator.components.GetAdvertisingIdListener
import com.pe.simswappingsimulator.components.GetAdvertisingIdTask
import com.pe.simswappingsimulator.databinding.ActivityLoginBinding
import com.pe.simswappingsimulator.model.Loginn
import com.pe.simswappingsimulator.model.ResponseAccount
import com.pe.simswappingsimulator.module.ApiClient
import com.pe.simswappingsimulator.services.SimSwappingService
import com.pe.simswappingsimulator.util.Utils
import com.pe.simswappingsimulator.util.UtilsShared
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


class Login : AppCompatActivity(),AuthenticationResultListener, GetAdvertisingIdListener {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var simSwappingService: SimSwappingService

    private lateinit var fingerprintManager: FingerprintManager
    private lateinit var keyguardManager: KeyguardManager

    lateinit var fingerprintDialog: FingerprintDialogFragment

    private var latitude: String = ""
    private var longitude: String = ""

    var imei = ""
    var phoneNumber = ""

    val requestCode = 1


    val permissionsToCheck = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        val view = binding.root
        setContentView(view)
        val retrofit = Retrofit.Builder()
            .baseUrl(Utils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        simSwappingService = retrofit.create(SimSwappingService::class.java)

        fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!checkLocationPermission()) {
            requestPermission()
        } else {
            Log.d("DEBUG_LOCATION", "Permisos OK, solicitando ubicación...")
        phoneNumber = UtilsShared.getPhoneNumber(this)
        requestLocation()
        }

        phoneNumber = UtilsShared.getPhoneNumber(this)
        Log.d("telephonyManager",phoneNumber)
        //imei = UtilsShared.getSimulatedImei()
        //Log.d("IMEI-AdvertisingId",imei)
        imei = getDeviceId(this)
        Log.d("IMEI-DeviceId",imei)
        if (imei.isNullOrEmpty()) {
            val obtenerAdvertisingIdTask = GetAdvertisingIdTask(applicationContext,this@Login)
            obtenerAdvertisingIdTask.execute()
        }

        setOnClickListener()

    }


    private fun setOnClickListener() {
        binding.tvCreateAccount.setOnClickListener {
            val intent = Intent(this@Login, RegisterAccount::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            binding.btnLogin.isEnabled = false

            keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager

            with(UtilsShared){
                if (checkFingerprintCompatibility(this@Login,fingerprintManager,keyguardManager)) {
                    val keyGenerator = getKeyGenerator()
                    val cipher = getCipher(keyGenerator)
                    val cryptoObject = FingerprintManager.CryptoObject(cipher)
                    val authenticationCallback = MyAuthenticationCallback(this@Login,this@Login)

                    fingerprintDialog = FingerprintDialogFragment.newInstance(cryptoObject, authenticationCallback,this@Login)
                    fingerprintDialog.show(supportFragmentManager, FingerprintDialogFragment.TAG)

                    fingerprintDialog.startAuthentication(cipher, fingerprintManager)

                }else {
                    CustomConfirmationDialog(this@Login).showConfirmationDialog(
                        UtilsShared.CONFIRMATION_TITLE,
                        "Es necesario que cuente con un sensor de huella digital.",
                        "Ok"
                    ) {
                        binding.btnLogin.isEnabled = true
                    }
                }
            }

        }
    }

    private fun showFingerAuthentication() {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        //val keyStore = KeyStore.getInstance("key_simswapping")
        keyStore.load(null)

        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            "key_simswapping",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setUserAuthenticationRequired(true)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()

        val cipher = Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/" +
                    KeyProperties.BLOCK_MODE_CBC + "/" +
                    KeyProperties.ENCRYPTION_PADDING_PKCS7
        )
        cipher.init(Cipher.ENCRYPT_MODE, keyStore.getKey("key_simswapping", null) as SecretKey)

        val cryptoObject = FingerprintManager.CryptoObject(cipher)
        val authenticationCallback =
            MyAuthenticationCallback(applicationContext,this@Login) // Crea tu propia implementación de FingerprintManager.AuthenticationCallback

        val fingerprintDialog = FingerprintDialogFragment.newInstance(
            cryptoObject,
            authenticationCallback,
            this@Login
        )
        fingerprintDialog.show(supportFragmentManager, FingerprintDialogFragment.TAG)

    }

    private fun doLogin() {
        val bodyLogin =Loginn(
            binding.etCreditCard.text.toString(),
            binding.etPassword.text.toString(),
            latitude,
            longitude,
            imei,
        )

        val call = ApiClient.simSwappingService.validateLogin(bodyLogin)

        call!!.enqueue(object : Callback<ResponseAccount> {
            override fun onResponse(call: Call<ResponseAccount>, response: Response<ResponseAccount>) {
                Log.d("onResponse validateLogin",response.body().toString())
                try{
                    fingerprintDialog.dismiss()
                    if (response.isSuccessful && response.body()!!.success) {
                        //val result = .usuario
                        startHomeActivity(response.body()!!)
                    } else {
                        if(response.code() == 500) {
                            CustomConfirmationDialog(this@Login)
                                .showConfirmationDialog(
                                    UtilsShared.CONFIRMATION_TITLE,
                                    "Error interno del servidor, por favor reintente luego",
                                    "Ok"){}
                        }else {
                            CustomConfirmationDialog(this@Login)
                                .showConfirmationDialog(
                                    UtilsShared.CONFIRMATION_TITLE,
                                    "${response.body()!!.message}",
                                    "Ok"){}
                        }

                        //Toast.makeText(applicationContext,"${response.body()!!.message}",Toast.LENGTH_SHORT).show()
                    }

                }catch (e: Exception){
                    Log.d("ERRvalidateLogin",e.printStackTrace().toString())
                }
                binding.btnLogin.isEnabled = true
            }

            override fun onFailure(call: Call<ResponseAccount>, t: Throwable) {
                Log.e("Error:",t.printStackTrace().toString())
                Toast.makeText(applicationContext,"Error:${t.message}",Toast.LENGTH_LONG).show()
                binding.btnLogin.isEnabled = true
            }
        })
    }

    private fun startHomeActivity(result: ResponseAccount) {
        val intent = Intent(this@Login,Home::class.java)
        val bundle = Bundle()
        val objUsuario = result.usuario
        val objCuenta = result.cuenta
        bundle.putInt("idUsuario", objUsuario.id_usuario!!)
        bundle.putString("nombre", objUsuario.nombre)
        bundle.putString("apellido", objUsuario.apellido)
        bundle.putString("dni", objUsuario.dni)
        bundle.putString("cc", objUsuario.cc)
        bundle.putString("telefono", objUsuario.telefono)
        bundle.putString("imei", objUsuario.imei)
        bundle.putString("latitud", objUsuario.latitud)
        bundle.putString("longitud", objUsuario.longitud)

        bundle.putInt("idCuenta", objCuenta.id_cuenta)
        bundle.putDouble("saldo", objCuenta.saldo)


        intent.putExtras(bundle)
        startActivity(intent)
    }

    private fun checkLocationPermission(): Boolean {

        for (permission in permissionsToCheck) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            permissionsToCheck,
            PERMISSION_REQUEST_CODE
        )
    }



    @SuppressLint("MissingPermission")
    private fun requestLocation() {

        Log.d("DEBUG_LOCATION", "Solicitando ubicación...")
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    latitude = String.format("%.8f", location.latitude)
                    longitude = String.format("%.8f", location.longitude)


                    Log.d("DEBUG_LOCATION", "✅ Ubicación obtenida: LAT=$latitude, LON=$longitude")
                    //latitude = location.latitude
                    //longitude = location.longitude

                } ?: run {
                    Log.d("DEBUG_LOCATION", "❌ Ubicación es null")
                    Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Todos los permisos fueron concedidos, puedes proceder con las operaciones
                    // que requieran estos permisos
                    phoneNumber = UtilsShared.getPhoneNumber(this)  //obtainPhoneNumber()
                    requestLocation()
                } else {
                    // Al menos un permiso fue denegado, puedes informar al usuario o tomar otras medidas
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }

    private fun getDeviceId(context: Context): String{
        if (checkAndRequestPermission(context, android.Manifest.permission.READ_PHONE_STATE,requestCode)) {
            // Si no se tienen los permisos, solicítalos
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_PHONE_STATE), 1)
            return ""
        }
        val telephonyManager = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return telephonyManager.imei ?: ""
        } else {
            // Antes de Android Oreo, puedes obtener el IMEI de la siguiente manera
            return telephonyManager.deviceId ?: ""
        }

    }


    fun checkAndRequestPermission(context: Context, permission: String, requestCode: Int): Boolean {
        // Verificar si el permiso ya ha sido otorgado
        val permissionCheck = ContextCompat.checkSelfPermission(context, permission)

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            // El permiso ya ha sido otorgado
            return true
        } else {
            ActivityCompat.requestPermissions(
                context as Login,  // Reemplaza YourActivity con el nombre de tu actividad
                arrayOf(permission),
                requestCode
            )
            return false
        }
    }

    override fun onAuthenticationSuccess() {
        Toast.makeText(this@Login,"Huella reconocida.",Toast.LENGTH_SHORT).show()
        doLogin()

    }

    override fun onAuthenticationError(errorMessage: String) {
        binding.btnLogin.isEnabled = true
        Toast.makeText(this@Login,"Error de autenticación: ${errorMessage}",Toast.LENGTH_SHORT).show()
        fingerprintDialog.dismiss()
    }

    override fun onAuthenticationHelp(helpMessage: String) {
        Toast.makeText(this@Login,"onAuthenticationHelp",Toast.LENGTH_SHORT).show()

    }

    override fun onAuthenticationFailed() {
        runOnUiThread {
            binding.btnLogin.isEnabled = true
            Toast.makeText(this, "Autenticación fallida", Toast.LENGTH_SHORT).show()

        }
    }

    override fun onGetAdvertisingId(advertisingId: String) {
        imei = advertisingId
    }

}