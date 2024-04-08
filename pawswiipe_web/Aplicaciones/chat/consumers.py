import json
from channels.generic.websocket import AsyncWebsocketConsumer
from asgiref.sync import sync_to_async

class ChatConsumer(AsyncWebsocketConsumer):

    # Funcion para conectarse a la sala del chat
    async def connect(self):
        # Obtener los identificadores de los usuarios
        user1 = self.scope['url_route']['kwargs']['user1_id']
        user2 = self.scope['url_route']['kwargs']['user2_id']
        #Obterner la sala
        self.room_name = f'chat_{user1}_{user2}'
        self.room_group_name = 'chat_%s' % self.room_name

        await self.channel_layer.group_add(
            self.room_name,
            self.room_group_name
        )

        await self.accept()
    
    # Funcion para desconectarse de la sala del chat
    async def disconnect(self, close_code):
        await self.channel_layer.discard(
            self.room_name,
            self.room_group_name
        )

    # Funcion para recibir mensajes de la sala

    async def receive(self, text_data):
        text_data_json = json.loads(text_data) #Convertir de Json a python(diccionario)
        message = text_data_json['message']

        await self.channel_layer.group_send(
            self.room_group_name,
            {
                'type': 'chat_message',
                'message': message
            }
        )
    # Funcion para enviar mensajes a la sala
    async def chat_message(self, event):
        message = event['message']

        await self.send(text_data=json.dumps({ #Convertir de python(diccionario) a Json
            'message': message
        }))     