RegistroUsuarioApp

Esta es una aplicación móvil para Android desarrollada en Kotlin con Jetpack Compose. Su objetivo es proporcionar un formulario simple para que los usuarios registren sus datos y los guarden de forma local en el dispositivo. La aplicación demuestra el uso de componentes de UI modernos, manejo de estado y persistencia de datos básicos.



🌟 Características Principales

Formulario de Registro: Un formulario intuitivo para que los usuarios ingresen su nombre, correo electrónico y número de teléfono.



Validación de Datos: El formulario valida los campos en tiempo real para asegurar que los datos ingresados son correctos.



Persistencia de Datos: Los datos del usuario se guardan localmente en el almacenamiento interno del dispositivo en formato JSON, utilizando la biblioteca GSON.



Gestión de Estado: La aplicación gestiona dinámicamente qué pantalla mostrar (registro o perfil) en función de si ya existe un usuario guardado.



Menú de Opciones: Un menú superior que permite al usuario borrar sus datos o ver la pantalla "Acerca de".



Modo Claro/Oscuro: Interfaz adaptable que permite al usuario cambiar entre el modo de tema claro y oscuro.



🚀 Tecnologías Utilizadas

Lenguaje: Kotlin



Framework de UI: Jetpack Compose



Persistencia de datos:



Serialización: GSON



Almacenamiento: Archivos locales en el sistema de archivos de Android



Gestión del Proyecto: Gradle con Kotlin DSL



📂 Estructura del Proyecto

MainActivity.kt: Contiene toda la lógica de la aplicación, incluyendo el manejo de la navegación entre pantallas, el formulario de registro y la visualización del perfil del usuario.



build.gradle.kts: Define las dependencias del proyecto, incluyendo Jetpack Compose, GSON y otras librerías necesarias.



🏃 Cómo Empezar

Para clonar y ejecutar este proyecto en Android Studio, sigue estos pasos:



Clona el repositorio: Abre la terminal en Android Studio y ejecuta el siguiente comando:



git clone \[https://github.com/JosePablo1996/RegistroUsuarioApp.git](https://github.com/JosePablo1996/RegistroUsuarioApp.git)



Abre el proyecto: En Android Studio, ve a File > Open y selecciona la carpeta del proyecto que acabas de clonar.



Sincroniza Gradle: Espera a que Android Studio sincronice automáticamente las dependencias de Gradle. Si no lo hace, haz clic en el botón Sync Project with Gradle Files.



Ejecuta la aplicación: Conecta tu dispositivo Android o usa un emulador y haz clic en el botón Run.

