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
from django.urls import include, path
from Aplicaciones.bbdd.views import *
from Aplicaciones.API.views import *
from django.conf.urls.static import static
from django.conf import settings
from Aplicaciones.chat.views import *


urlpatterns = [
    path('admin/', admin.site.urls, name="admin"),
    path("", home,name="home"),
    path("registro/",registro, name="registro"),
    path("feed/",feed, name="feed"),
    path("logout/",logOut, name="logout"),
    path("perfil/", perfil, name="perfil"),
    path("perfil/<int:mascota_id>/", perfil, name='perfil_mascota'),
    path('perfil-mascota/<int:mascota_id>/', perfil_mascota, name='perfil-mascota'),
    path("eliminarCuenta/",eliminarCuenta, name="eliminarCuenta"),
    path('eliminar-mascota/<int:mascota_id>/', eliminar_mascota, name='eliminar_mascota'),
    path("modificarDatos/",modificarDatos, name="modificarDatos"),
    path("create_post/", create_post_view, name="create_post"),
    path('has_liked/<int:publicacion_id>/', has_liked, name='has_liked'),
    path('like_post/<int:publicacion_id>/', like_post, name='like_post'),
    path('get_likes/<int:publicacion_id>/', get_likes, name='get_likes'),
    path('feed/', feed, name='feed'),
    path('comentario_post/<int:publicacion_id>/', comentario_post, name='comentario_post'),
    path('get_comments/<int:publicacion_id>/', get_comments, name='get_comments'),
    path('delete_comentario/<int:comentario_id>', delete_comentario, name='delete_comentario'),
    path("delete_post/<int:publicacion_id>", delete_post, name="delete_post"),
    path("guardar_mascota/<int:mascota_id>/", guardar_mascota_actual, name="guardar_mascota"),
    path("editar_datos_mascota/<int:mascota_id>", modificarDatosMascota, name="editar_datos_mascota"),
    path("seguir_perfil/<int:perfil_id>", seguir_perfil, name="seguir_perfil"),
    #path('api/login/', api_login, name='api_login'),
    path('registro_mascota/<str:mail>/', registro_mascota, name='registro_mascota'),
    path('registro_mascota_initial/<str:mail>/<int:initial>/', registro_mascota, name='registro_mascota_initial'),
    
    # API ---------------------------------------------------------------------
    path('api/logear/', login_usuario, name='login_usuario'),
    path('api/bloquear_cuenta/',bloquear_cuenta, name='bloquear_cuenta'),
    path('api/registrar/', registrar_usuario, name='registrar_usuario'),
    path('api/borrar_usuario/',borrar_usuario, name='borrar_usuario'),
     path('api/muro/',muro, name='muro'),
    path('api/dar_like/',dar_like, name='dar_like'),
    path('api/follow_perfil/',follow_perfil, name='follow_perfil'),
    path('api/modificar_usuario/',modificar_usuario, name='modificar_usuario'),
    path('api/registrar_mascota/', registrar_mascota, name="registro_mascota"),
    path('api/perfil_completo/', perfil_completo, name='perfil_completo'),
    path('api/perfil_completo_visitado/', perfil_completo_visitado, name='perfil_completo_visitado'),
    path('api/cerrar_sesion/', cerrar_sesion, name='cerrar_sesion'),
    path('api/insert_comentario/', insert_comentario, name='insert_comentario'),
    path('api/comentarios/<int:publicacion_id>/', comments, name='comments'),
    path('api/actualizar_mascota/', actualizar_mascota, name='actualizar_mascota'),
    path('api/delete_mascota/', delete_mascota, name="delete_mascota"),
    path('api/eliminar_publicacion/', eliminar_publicacion, name="eliminar_publicacion"),
    path('api/eliminar_comentario/', eliminar_comentario, name="eliminar_comentario"),
    path('api/crear_publicacion/', crear_publicacion, name="crear_publicacion"),
    path('api/search_perfiles/', search_perfiles, name='search_perfiles'),
    # --------------------------------------------------------------------------
    
    
    path('buscar_perfiles/', buscar_perfiles, name='buscar_perfiles'),
    path('iniciar_chat/<str:receptor>/', iniciar_chat, name='iniciar_chat'),
    path('chat/<str:slug>/', chat, name='chat'),
    path('enviar_mensaje/<str:slug>/', enviar_mensaje, name='enviar_mensaje'),
    #path('inbox/', inbox, name='inbox'),
    path('obtener_datos/',obtener_datos_usuario, name='obtener_datos'),
    path('borrar_usuario/',borrar_usuario, name='borrar_usuario'),
    path('modificar_usuario/',modificar_usuario, name='modificar_usuario'),
    # path('inbox/', inbox, name='inbox'),
    
    
    
    path('cerrar_sesion/',cerrar_sesion, name='cerrar_sesion'),
    # path('cerrar_sesion/',cerrarSesion, name='cerrar_sesion'),
    path('crear_sala/', crear_sala, name='crear_sala'),
    
    path('reset/', include('Aplicaciones.reset.urls')),
    path('lista_seguidores/<int:mascota_id>', obtener_lista_seguidores, name='lista_seguidores'),
    path('lista_seguidos/<int:mascota_id>', obtener_lista_seguidos, name='lista_seguidos'),
    path('chattt/', chattt, name='chattt'),
    path('chatt/',include('Aplicaciones.chat.urls')),
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
if settings.DEBUG:
    urlpatterns += static(settings.STATIC_URL, document_root=settings.STATIC_ROOT)


"""  ,
    path('search-users/', search_users, name='search_users'),
    path('send_direct/', send_direct, name='send_direct'), """