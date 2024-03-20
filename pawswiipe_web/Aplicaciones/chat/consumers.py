import json
from channels.generic.websocket import AsyncWebsocketConsumer
from asgiref.sync import sync_to_async
from django.contrib.auth.models import User
from Aplicaciones.bbdd.models import Sala

class ChatConsumer(AsyncWebsocketConsumer):

    async def connect(self):
        self.slug = self.scope['url_route']['kwargs']['slug']
        self.room_group_name = 'chat_%s' % self.slug

        # Obtener la sala por el slug
        room = await self.get_room(self.slug)

        # Verificar si el usuario es parte de la sala
        user = self.scope["user"]
        if user == room.emisor or user == room.receptor:
            await self.channel_layer.group_add(
                self.room_group_name,
                self.channel_name
            )
            await self.accept()
        else:
            await self.close()

    async def disconnect(self, close_code):
        await self.channel_layer.group_discard(
            self.room_group_name,
            self.channel_name
        )

    async def receive(self, text_data):
        text_data_json = json.loads(text_data)
        message = text_data_json['message']

        await self.channel_layer.group_send(
            self.room_group_name,
            {
                'type': 'chat_message',
                'message': message
            }
        )

    async def chat_message(self, event):
        message = event['message']

        await self.send(text_data=json.dumps({
            'message': message
        }))

    @sync_to_async
    def get_room(self, slug):
        return Sala.objects.get(slug=slug)