from django.urls import re_path
from . import consumers

""" websocket_urlpatterns = [
    re_path(r'ws/chat/(?P<user1_id>\w+)/(?P<user2_id>\w+)/$', consumers.ChatConsumer.as_asgi()),
] """


from django.urls import path
from Aplicaciones.bbdd import views

urlpatterns = [
    path('direct/inbox/', views.inbox, name='inbox'),
    path('direct/chat/<int:user1_id>/<int:user2_id>/', views.chat, name='chat'),
]