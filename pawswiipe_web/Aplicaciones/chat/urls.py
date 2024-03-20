from django.urls import path

from Aplicaciones.chat.views import *

urlpatterns = [
    path("", index, name="index"),
    path('<str:Sala>/', Salaa, name='chat')

]