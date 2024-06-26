import os
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "pawswiipe.settings")
import django
django.setup()

from channels.auth import AuthMiddlewareStack
from channels.routing import ProtocolTypeRouter, URLRouter
from django.core.asgi import get_asgi_application
import Aplicaciones.chat.routing



application = ProtocolTypeRouter({
    "http": get_asgi_application(),
    "websocket": AuthMiddlewareStack(
         URLRouter(
        Aplicaciones.chat.routing.websocket_urlpatterns
    )),
})