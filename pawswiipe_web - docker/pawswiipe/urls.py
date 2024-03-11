"""
URL configuration for pawswipe project.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/4.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path
from Aplicaciones.bbdd.views import *
from Aplicaciones.API.views import *


urlpatterns = [
    path('admin/', admin.site.urls, name="admin"),
    path("", home,name="home"),
    path("registro/",registro, name="registro"),
    path("feed/",feed, name="feed"),
    path("logout/",logOut, name="logout"),
    path("perfil/",perfiil, name="perfil"),
    path("eliminarCuenta/",eliminarCuenta, name="eliminarCuenta"),
    path("modificarDatos/",modificarDatos, name="modificarDatos"),
    #path('api/login/', api_login, name='api_login'),
    path('registrar/', registrar_usuario, name='registrar_usuario'),
    path('logear/', login_usuario, name='login_usuario'),
    path('cerrar_sesion/', cerrarSesion, name='cerrar_sesion'),
    path('iniciar_chat/<str:receptor>/', iniciar_chat, name='iniciar_chat'),
    path('chat/<str:slug>/', chat, name='chat'),
    path('enviar_mensaje/<str:slug>/', enviar_mensaje, name='enviar_mensaje'),
    path('inbox/', inbox, name='inbox'),
    path('obtener_datos/',obtener_datos_usuario, name='obtener_datos'),
    path('borrar_usuario/',borrar_usuario, name='borrar_usuario'),
    path('modificar_usuario/',modificar_usuario, name='modificar_usuario'),
    path('cerrar_sesion/',cerrarSesion, name='cerrar_sesion'),
    path('bloquear_cuenta/',bloquear_cuenta, name='bloquear_cuenta'),
    path('chattt/', chattt, name='chattt'),
]

"""  ,
    path('search-users/', search_users, name='search_users'),
    path('send_direct/', send_direct, name='send_direct'), """