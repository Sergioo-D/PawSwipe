from django.urls import path

from Aplicaciones.chat.views import *

urlpatterns = [
    path("iinbox/", iinbox, name="iinbox"),
    path('<slug:slug>/', Salaa, name='chatroom'),
    

]