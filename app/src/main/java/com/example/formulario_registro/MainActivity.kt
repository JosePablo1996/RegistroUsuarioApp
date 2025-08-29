package com.example.registrousuarioapp

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Se lee la preferencia de tema desde SharedPreferences.
            val context = LocalContext.current
            val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val isDarkTheme = remember { mutableStateOf(sharedPreferences.getBoolean("dark_theme", false)) }

            MaterialTheme(
                colorScheme = if (isDarkTheme.value) darkColorScheme() else lightColorScheme()
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RegistroAppConMenu(isDarkTheme)
                }
            }
        }
    }
}

/** Pantallas disponibles en el menú */
enum class Screen { Registrar, Usuario, AcercaDe }

/**
 * Clase de datos para el usuario, que será guardada como JSON.
 * Se añade la propiedad de fecha de registro.
 */
data class UserData(val nombre: String, val correo: String, val telefono: String, val registroFecha: String)

/**
 * Función para guardar el objeto de usuario en la memoria interna.
 */
fun guardarUsuario(context: Context, user: UserData) {
    try {
        val gson = Gson()
        val json = gson.toJson(user)
        val file = File(context.filesDir, "usuario_data.txt")
        file.writeText(json)
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Función para leer el objeto de usuario desde la memoria interna.
 */
fun leerUsuario(context: Context): UserData? {
    val file = File(context.filesDir, "usuario_data.txt")
    if (!file.exists()) {
        return null
    }
    return try {
        val json = file.readText()
        val gson = Gson()
        gson.fromJson(json, UserData::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Función para borrar el archivo de usuario.
 */
fun borrarUsuario(context: Context) {
    val file = File(context.filesDir, "usuario_data.txt")
    if (file.exists()) {
        file.delete()
    }
}

/**
 * Este es el punto de entrada principal para el flujo de la aplicación.
 * Gestiona el estado y decide qué pantalla mostrar al usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroAppConMenu(isDarkTheme: MutableState<Boolean>) {
    // Estado que controla qué pantalla se muestra.
    var currentScreen by remember { mutableStateOf(Screen.Registrar) }

    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    // Estado del menú desplegable.
    var showMenu by remember { mutableStateOf(false) }

    // Función para borrar los datos y reiniciar la aplicación.
    val onClearData: () -> Unit = {
        borrarUsuario(context)
        Toast.makeText(context, "Datos borrados", Toast.LENGTH_SHORT).show()
        currentScreen = Screen.Registrar
        showMenu = false
    }

    // Este LaunchedEffect se ejecuta una sola vez cuando el composable se carga por primera vez.
    // Se usa para verificar si ya existe un usuario guardado.
    LaunchedEffect(Unit) {
        val userData = leerUsuario(context)
        if (userData != null) {
            // Si el nombre existe, cambia la pantalla a la de mostrar usuario.
            currentScreen = Screen.Usuario
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Usuario") },
                actions = {
                    IconButton(onClick = {
                        isDarkTheme.value = !isDarkTheme.value
                        sharedPreferences.edit().putBoolean("dark_theme", isDarkTheme.value).apply()
                    }) {
                        // Icono animado para el modo claro/oscuro
                        AnimatedContent(targetState = isDarkTheme.value) { targetIsDark ->
                            if (targetIsDark) {
                                Icon(Icons.Default.DarkMode, contentDescription = "Modo Oscuro")
                            } else {
                                Icon(Icons.Default.LightMode, contentDescription = "Modo Claro")
                            }
                        }
                    }
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Borrar datos") },
                            onClick = onClearData
                        )
                        DropdownMenuItem(
                            text = { Text("Acerca de") },
                            onClick = {
                                currentScreen = Screen.AcercaDe
                                showMenu = false
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                Screen.Registrar -> {
                    RegistroUsuarioApp(onRegistroExitoso = {
                        currentScreen = Screen.Usuario
                    })
                }
                Screen.Usuario -> {
                    MostrarUsuarioGuardado(onCerrarSesion = {
                        borrarUsuario(context)
                        currentScreen = Screen.Registrar
                    })
                }
                Screen.AcercaDe -> {
                    AcercaDeScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroUsuarioApp(onRegistroExitoso: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Variables de estado para los errores de validación
    var nombreError by remember { mutableStateOf(false) }
    var correoError by remember { mutableStateOf(false) }
    var telefonoError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Formulario de Registro", style = MaterialTheme.typography.headlineLarge)
                Spacer(modifier = Modifier.height(24.dp))
                // Campo de Nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        nombreError = it.isBlank()
                    },
                    label = { Text("Nombre") },
                    isError = nombreError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (nombreError) {
                    Text(text = "El nombre no puede estar vacío", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Campo de Correo
                OutlinedTextField(
                    value = correo,
                    onValueChange = {
                        correo = it
                        correoError = !android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
                    },
                    label = { Text("Correo") },
                    isError = correoError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (correoError) {
                    Text(text = "Correo inválido", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Campo de Teléfono
                OutlinedTextField(
                    value = telefono,
                    onValueChange = {
                        telefono = it
                        telefonoError = !(it.all { char -> char.isDigit() } && it.length in 8..9)
                    },
                    label = { Text("Teléfono") },
                    isError = telefonoError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (telefonoError) {
                    Text(text = "Teléfono inválido. Debe tener 8 o 9 dígitos.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    // Validar todos los campos antes de guardar
                    nombreError = nombre.isBlank()
                    correoError = !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()
                    telefonoError = !(telefono.all { it.isDigit() } && telefono.length in 8..9)

                    if (!nombreError && !correoError && !telefonoError) {
                        // Guardar en la memoria interna
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                        val registroFecha = dateFormat.format(Date())
                        val userData = UserData(nombre, correo, telefono, registroFecha)
                        guardarUsuario(context, userData)
                        onRegistroExitoso()
                    } else {
                        Toast.makeText(context, "Por favor, corrige los errores del formulario", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Registrar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MostrarUsuarioGuardado(onCerrarSesion: () -> Unit) {
    // Obtener datos del usuario desde la memoria interna
    val context = LocalContext.current
    val userData = leerUsuario(context)
    val nombre = userData?.nombre ?: ""
    val correo = userData?.correo ?: ""
    val telefono = userData?.telefono ?: ""
    val registroFecha = userData?.registroFecha ?: "No disponible"

    // Envuelve el contenido en un Card con un diseño similar al formulario
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "¡Usuario registrado con éxito!", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Nombre: $nombre", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Correo: $correo", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Teléfono: $telefono", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Fecha de registro: $registroFecha", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onCerrarSesion) {
                    Text("Cerrar Sesión")
                }
            }
        }
    }
}

/**
 * Pantalla que muestra información "Acerca de".
 */
@Composable
fun AcercaDeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Acerca de la App",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Nombre de la App: RegistroApp",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Autor: Pablo Miranda",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Versión: 1.0",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 18.sp
                )
            }
        }
    }
}
