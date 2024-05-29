from django.urls import path

from Aplicaciones.chat.views import *

urlpatterns = [
    path("iinbox/", iinbox, name="iinbox"),
    path("guardar_dm/", guardar_dm, name="guardar_dm"),  # Esta ruta necesita ser definida antes de la ruta genérica de slug si tienen formatos que pueden confundirse.
    path('<slug:slug>/', Salaa, name='chatroom'),
    path("iinbox/<slug:slug>", iinbox, name="iinbox"),
    # path('datos_mascota/<int:mascota_id>/', datos_mascota, name='datos_mascota'),
    
    
]