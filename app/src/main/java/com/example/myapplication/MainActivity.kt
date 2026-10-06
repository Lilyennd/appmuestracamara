package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors
import android.content.Intent
import android.net.Uri

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "pantalla1") {

        composable(
            route = "pantalla1",
            enterTransition = { fadeIn(animationSpec = tween(1500)) },
            exitTransition = { fadeOut(animationSpec = tween(2500)) },
            popEnterTransition = { fadeIn(animationSpec = tween(1500)) },
            popExitTransition = { fadeOut(animationSpec = tween(1500)) },
        ) {
            PantallaUno(
                onNavegarSiguiente = { navController.navigate("pantalla2") },
                onIrCamara = { navController.navigate("camara") },
                onIrEscaner = { navController.navigate("escaner") }
            )
        }

        composable(
            route = "pantalla2",
            enterTransition = {
                scaleIn(
                    animationSpec = tween(500),
                    initialScale = 0.5f
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(2400)
                )
            }
        ) {
            PantallaDos(onVolver = { navController.popBackStack() })
        }

        composable("camara") {
            PantallaCamara(onVolver = { navController.popBackStack() })
        }

        composable("escaner") {
            PantallaEscanerQr(onVolver = { navController.popBackStack() })
        }
    }
}



@Composable
fun PantallaUno(
    onNavegarSiguiente: () -> Unit,
    onIrCamara: () -> Unit,
    onIrEscaner: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¡Estás en la Pantalla Uno!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onNavegarSiguiente) {
                Text(text = "Ir a Pantalla Dos")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onIrCamara) {
                Text(text = "Abrir cámara")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onIrEscaner) {
                Text(text = "Escanear QR")
            }
        }
    }
}

@Composable
fun PantallaDos(onVolver: () -> Unit) {
    Surface (
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¡Estás en la Pantalla Dos!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onVolver) {
                Text(text = "Volver")
            }
        }
    }
}


@Composable
fun PantallaCamara(onVolver: () -> Unit) {
    val context = LocalContext.current
    var foto by remember { mutableStateOf<Bitmap?>(null) }

    // Launcher que abre la cámara y devuelve un Bitmap
    val tomarFotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap -> foto = bitmap }

    // Launcher que pide el permiso
    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) tomarFotoLauncher.launch(null)
        else Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
    }

    fun verificarYAbrirCamara() {
        val tienePermiso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (tienePermiso) tomarFotoLauncher.launch(null)
        else permisoLauncher.launch(Manifest.permission.CAMERA)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        foto?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Foto capturada",
                modifier = Modifier.size(250.dp)
            )
            Spacer(Modifier.height(16.dp))
        }
        Button(onClick = { verificarYAbrirCamara() }) { Text("Abrir cámara") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onVolver) { Text("Volver") }
    }
}

@Composable
fun VistaCamaraQr(onQrDetectado: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // 1. Vista previa en pantalla
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // 2. Análisis de fotogramas para detectar el QR
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(
                            Executors.newSingleThreadExecutor(),
                            QrCodeAnalyzer { texto -> onQrDetectado(texto) }
                        )
                    }

                // 3. Cámara trasera + ciclo de vida
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("CameraX", "Error al iniciar la cámara", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )

    // Apagar la cámara cuando este composable sale de pantalla
    DisposableEffect(Unit) {
        onDispose {
            ProcessCameraProvider.getInstance(context).get().unbindAll()
        }
    }
}
@Composable
fun PantallaEscanerQr(onVolver: () -> Unit) {
    val context = LocalContext.current
    var resultado by remember { mutableStateOf<String?>(null) }
    var tienePermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> tienePermiso = concedido }

    // Pedir permiso al entrar a la pantalla si no lo tiene
    LaunchedEffect(Unit) {
        if (!tienePermiso) permisoLauncher.launch(Manifest.permission.CAMERA)
    }

    when {
        !tienePermiso -> {
            Column(
                Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Se necesita permiso de cámara para escanear")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { permisoLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Dar permiso")
                }
            }
        }

        resultado == null -> {
            VistaCamaraQr(onQrDetectado = { resultado = it })
        }

        else -> {
            Column(
                Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("QR Detectado:", style = MaterialTheme.typography.titleMedium)
                Text(resultado!!, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))

                // Si es un enlace web, ofrecer abrirlo
                if (resultado!!.startsWith("http")) {
                    Button(onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(resultado))
                        )
                    }) { Text("Abrir enlace") }
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedButton(onClick = { resultado = null }) { Text("Escanear otro") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onVolver) { Text("Volver") }
            }
        }
    }
}