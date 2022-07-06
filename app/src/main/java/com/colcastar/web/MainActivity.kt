package com.colcastar.web

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.mazenrashed.printooth.Printooth
import com.mazenrashed.printooth.ui.ScanningActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val PERMISO_CAMARA = 0

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ScanningActivity.SCANNING_FOR_PRINTER && resultCode == Activity.RESULT_OK)
        {
            Log.e("TAG", "onActivityResult: impresonra listaaa")
            webView.getUrl()?.let {
                if(it.contains("config-impresion-plantillas")){
                    Log.i("TAG","en configuracion de boleta")
                    Printooth.init(this);
                    val arg1 = Printooth.getPairedPrinter()?.name;
                    webView.evaluateJavascript("mostrarNombreImpresora('${arg1}')", null);
                    val sharedPref: SharedPreferences = this.getPreferences(MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putString(
                        getString(R.string.saved_device_address_imp),
                        Printooth.getPairedPrinter()?.address
                    )
                    editor.putString(
                        getString(R.string.saved_device_name_imp),
                        Printooth.getPairedPrinter()?.name
                    )
                    editor.apply()
                }
            };
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ComprobarPermisos()
//        registerForActivityResult()


        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.domStorageEnabled = true
        webView.settings.builtInZoomControls = true
//        webView.loadUrl("http://smarterp.sreasons.com/auth/login")
//        webView.loadUrl("https://erpperu.smartclic.pe/auth/login")

        webView.settings.setAppCachePath(this.applicationContext.cacheDir.absolutePath)
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        webView.settings.databaseEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.addJavascriptInterface(JavaScriptInterface(this), "Android")
        webView.settings.pluginState = WebSettings.PluginState.ON

        val mValuegg = this;
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.i("mostrandoMensaje"," agregando funcion")
                view?.loadUrl("javascript:function inputClick(val){Android.printMobile(val);}")
                view?.evaluateJavascript("myMethod()", {returnValue ->
                    Toast.makeText(mValuegg,"mensjae",Toast.LENGTH_LONG);
                })
            }
        }
        webView.loadUrl("http://www.colcastar.com")
        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            try {
                webView.loadUrl(JavaScriptInterface.getBase64StringFromBlobUrl(url))
            } catch (e: Exception) {
                Logger().log(this,e)
//                Log.e("state", )

            }
            return@setDownloadListener
        }
        //startActivityForResult(Intent(this, ScanningActivity::class.java), ScanningActivity.SCANNING_FOR_PRINTER)
    }

    private fun ComprobarPermisos() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
            == PackageManager.PERMISSION_GRANTED
        ) {

            /* Ya se ha obtenido el permiso previamente
            Iniciamos Cámara*/
            IniciarCamara()
        } else {
            // No se tiene el permiso, es necesario pedirlo al usuario
            PedirPermisoCamara()
        }
    }

    private fun PedirPermisoCamara() {
        //Comprobación 'Racional'
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.INTERNET
            )
        ) {

            //Mostramos un AlertDialog al usuario explicándole la necesidad del permiso
            val AD: AlertDialog
            val ADBuilder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
            ADBuilder.setMessage("Para escanear un producto es necesario utilizar la cámara de tu dispositivo. Permite que 'nombre app' pueda acceder a la cámara.")
            ADBuilder.setPositiveButton("Continuar",
                DialogInterface.OnClickListener { dialog, which -> /*Cuando el usuario pulse sobre el botón del AlertDialog se procede a solicitar
                             el permiso con el siguiente código:*/
                    ActivityCompat.requestPermissions(
                        this@MainActivity, arrayOf(Manifest.permission.INTERNET),
                        PERMISO_CAMARA
                    )
                })

            //Mostramos el AlertDialog
            AD = ADBuilder.create()
            AD.show()
        } else {
            /*Si no hay necesidad de una explicación racional, pasamos a solicitar el
            permiso directamente*/
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(Manifest.permission.INTERNET),
                PERMISO_CAMARA
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISO_CAMARA) {
            /* Resultado de la solicitud para permiso de cámara
             Si la solicitud es cancelada por el usuario, el método .lenght sobre el array
             'grantResults' devolverá null.*/
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Permiso concedido, podemos iniciar camara
                IniciarCamara()
            } else {
                /* Permiso no concedido
                 Aquí habría que explicar al usuario el por qué de este permiso
                 y volver a solicitarlo .*/
//
            }
        }
    }

    private fun IniciarCamara() {
        Toast.makeText(this@MainActivity, "Ex cámara...", Toast.LENGTH_SHORT).show()
//        Intent intent = new Intent(this, ActivityCamara.class);
//        startActivity(intent);
    }

    override fun onBackPressed() {
        // if your webview can go back it will go back
        if (webView.canGoBack())
            webView.goBack()
        // if your webview cannot go back
        // it will exit the application
        else
            super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}