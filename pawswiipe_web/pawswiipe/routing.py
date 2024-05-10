# from channels.auth import AuthMiddlewareStack
# from channels.routing import ProtocolTypeRouter, URLRouter
# from django.core.asgi import get_asgi_application
# from django.urls import path

# from Aplicaciones.chat import *




# application = ProtocolTypeRouter({
#     "http": get_asgi_application(),
#     "websocket": AuthMiddlewareStack(
#          URLRouter(
#         Aplicaciones.chat.routing.websocket_urlpatterns
#     )),
# })