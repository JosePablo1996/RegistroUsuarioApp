package com.example.registrousuarioapp

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
 * Función que contiene la estructura principal de la aplicación con un menú desplegable.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun RegistroAppConMenu(isDarkTheme: MutableState<Boolean>) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    // Inicia siempre en la pantalla de Registro
    var currentScreen by remember { mutableStateOf(Screen.Registrar) }

    val onUserSaved: () -> Unit = {
        // Al guardar el usuario, redirige a la pantalla de Usuario
        currentScreen = Screen.Usuario
    }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                // Encabezado del menú con información
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "User Icon",
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Menú Principal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Divider()
                Spacer(Modifier.height(12.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Registrar Icono") },
                    label = { Text("Registrar Usuario") },
                    selected = currentScreen == Screen.Registrar,
                    onClick = {
                        scope.launch { drawerState.close() }
                        currentScreen = Screen.Registrar
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "Acerca de Icono") },
                    label = { Text("Acerca de...") },
                    selected = currentScreen == Screen.AcercaDe,
                    onClick = {
                        scope.launch { drawerState.close() }
                        currentScreen = Screen.AcercaDe
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Delete, contentDescription = "Borrar Icono") },
                    label = { Text("Borrar datos") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        borrarDatos(context)
                        // Al borrar, regresa a la pantalla de registro
                        currentScreen = Screen.Registrar
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentScreen) {
                                Screen.Registrar -> "Registro de Usuario"
                                Screen.Usuario -> "Perfil del Usuario"
                                Screen.AcercaDe -> "Acerca de la App"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                isDarkTheme.value = !isDarkTheme.value
                                sharedPreferences.edit().putBoolean("dark_theme", isDarkTheme.value).apply()
                            }
                        ) {
                            AnimatedContent(targetState = isDarkTheme.value) { isDark ->
                                if (isDark) {
                                    Icon(
                                        imageVector = Icons.Default.LightMode,
                                        contentDescription = "Modo claro"
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.DarkMode,
                                        contentDescription = "Modo oscuro"
                                    )
                                }
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        slideInHorizontally(animationSpec = tween(300)) { fullWidth -> -fullWidth } + fadeIn(animationSpec = tween(300)) with
                                slideOutHorizontally(animationSpec = tween(300)) { fullWidth -> fullWidth } + fadeOut(animationSpec = tween(300))
                    }
                ) { targetScreen ->
                    when (targetScreen) {
                        Screen.Registrar -> RegistroUsuarioApp(onUserSaved = onUserSaved)
                        Screen.Usuario -> MostrarUsuarioGuardado(context)
                        Screen.AcercaDe -> AcercaDeScreen()
                    }
                }
            }
        }
    }
}

/**
 * Pantalla de registro de usuario con validación de campos.
 */
@Composable
fun RegistroUsuarioApp(onUserSaved: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    var nombreError by remember { mutableStateOf(false) }
    var correoError by remember { mutableStateOf(false) }
    var telefonoError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    // Determina si es una tableta en función del ancho de la pantalla en dp
    val isTablet = context.resources.configuration.screenWidthDp > 600

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Registro de Usuario", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(horizontal = if (isTablet) 64.dp else 0.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Campo de Nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        nombreError = false // Se borra el error al escribir
                    },
                    label = { Text("Nombre") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nombreError,
                    supportingText = { if (nombreError) Text("El nombre es requerido") }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Disposición de campos para tabletas
                if (isTablet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = correo,
                            onValueChange = {
                                correo = it
                                correoError = !android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
                                if (it.isBlank()) correoError = false
                            },
                            label = { Text("Correo") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Correo") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.weight(1f),
                            isError = correoError,
                            supportingText = { if (correoError) Text("Correo inválido") }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = {
                                telefono = it
                                // Validación de teléfono en tiempo real
                                telefonoError = it.isNotBlank() && !it.matches(Regex("[0-9]+"))
                            },
                            label = { Text("Teléfono") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Teléfono") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f),
                            isError = telefonoError,
                            supportingText = {
                                if (telefonoError) Text("El teléfono solo debe contener números")
                                else if (telefono.isBlank()) Text("El teléfono es requerido")
                            }
                        )
                    }
                } else {
                    // Disposición de campos para teléfonos
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = correo,
                            onValueChange = {
                                correo = it
                                correoError = !android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
                                if (it.isBlank()) correoError = false
                            },
                            label = { Text("Correo") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Correo") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            isError = correoError,
                            supportingText = { if (correoError) Text("Correo inválido") }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = {
                                telefono = it
                                // Validación de teléfono en tiempo real
                                telefonoError = it.isNotBlank() && !it.matches(Regex("[0-9]+"))
                            },
                            label = { Text("Teléfono") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Teléfono") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            isError = telefonoError,
                            supportingText = {
                                if (telefonoError) Text("El teléfono solo debe contener números")
                                else if (telefono.isBlank()) Text("El teléfono es requerido")
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                ElevatedButton(
                    onClick = {
                        nombreError = nombre.isBlank()
                        telefonoError = telefono.isBlank() || !telefono.matches(Regex("[0-9]+"))
                        correoError = correo.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()

                        if (!nombreError && !telefonoError && !correoError) {
                            val usuarioData = UserData(
                                nombre = nombre,
                                correo = correo,
                                telefono = telefono,
                                fechaRegistro = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                            )
                            guardarUsuario(context, usuarioData)
                            onUserSaved()
                            Toast.makeText(context, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Por favor, completa los campos correctamente", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar")
                }
            }
        }
    }
}

/**
 * Pantalla que muestra los datos del usuario guardado.
 */
@Composable
fun MostrarUsuarioGuardado(context: Context) {
    val usuario by remember { mutableStateOf(leerUsuario(context)) }

    if (usuario != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Datos del Usuario",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Nombre: ${usuario!!.nombre}", fontSize = 18.sp)
                    Text(text = "Correo: ${usuario!!.correo}", fontSize = 18.sp)
                    Text(text = "Teléfono: ${usuario!!.telefono}", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Fecha de Registro: ${usuario!!.fechaRegistro}", fontSize = 18.sp)
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No hay datos de usuario guardados", style = MaterialTheme.typography.bodyLarge)
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

/**
 * Objeto de datos del usuario, para guardar en JSON.
 */
data class UserData(
    val nombre: String,
    val correo: String,
    val telefono: String,
    val fechaRegistro: String
)

/**
 * Guarda los datos del usuario en un archivo interno en formato JSON.
 */
fun guardarUsuario(context: Context, usuario: UserData) {
    try {
        val gson = Gson()
        val jsonString = gson.toJson(usuario)
        val file = File(context.filesDir, "usuario_data.txt")
        file.writeText(jsonString)
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Lee los datos del usuario desde un archivo interno.
 */
fun leerUsuario(context: Context): UserData? {
    try {
        val file = File(context.filesDir, "usuario_data.txt")
        if (!file.exists()) {
            return null
        }
        val jsonString = file.readText()
        val gson = Gson()
        return gson.fromJson(jsonString, UserData::class.java)
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}

/**
 * Borra los datos del usuario desde el archivo interno.
 */
fun borrarDatos(context: Context) {
    val file = File(context.filesDir, "usuario_data.txt")
    if (file.exists()) {
        file.delete()
        Toast.makeText(context, "Datos borrados", Toast.LENGTH_SHORT).show()
    }
}
