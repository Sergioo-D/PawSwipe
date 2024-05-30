from django.urls import path
from . import consumers

websocket_urlpatterns = [
    path('ws/chatt/<slug>', consumers.ChatConsumer.as_asgi()),
]