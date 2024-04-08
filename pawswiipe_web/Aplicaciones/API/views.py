import os
from django.conf import settings
from django.forms import ImageField
from django.shortcuts import render, redirect, get_object_or_404
from django.http import HttpResponse
from requests import Response
from Aplicaciones.bbdd.models import *
from django.contrib.auth.hashers import make_password
from django.contrib.auth import  login, logout, authenticate
from rest_framework import status
from rest_framework.decorators import api_view, parser_classes
from Aplicaciones.bbdd.serializers import UsuarioSerializer , LoginSerializer
from rest_framework.response import Response
from rest_framework.authtoken.models import Token
from django.db.models import Q
from django.core.exceptions import ObjectDoesNotExist
import base64
import pdb
from django.core.files.base import ContentFile
from PIL import Image
from io import BytesIO


def crear_perfil_default(user):
    try:
        imgd = os.path.join(settings.MEDIA_ROOT,'perfil_images/default.jpg')
        perfil_default = perfil.objects.create(usuario=user, fotoPerfil=imgd)
        perfil_default.save()
    except Exception as e:
        print(f"No se pudo crear el perfil predeterminado para {user.mail}: {e}")

@api_view(['POST'])
def registrar_usuario(request):
    print(request.data)
    if request.method == 'POST':
        serializer = UsuarioSerializer(data=request.data)
        if serializer.is_valid():
            # email = request.data.get('email')
            # user = Usuario.objects.get(mail=email)
            # if user.DoesNotExist:
            password = serializer.validated_data.get('password')
            encriptar = make_password(password)
            serializer.validated_data['password'] = encriptar
            user = serializer.save()
            crear_perfil_default(user)
            respuesta_data = serializer.data
            respuesta_data["message"] = 1
            return Response(respuesta_data, status=status.HTTP_201_CREATED)
            # else: 
            #     respuesta_data["message"] = 0
            #     return Response (respuesta_data, status=status.HTTP_226_IM_USED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    

@api_view(['POST'])
def login_usuario(request):
   
    if request.method == 'POST':
        serializer = LoginSerializer(data=request.data)
        print("Datos de la solicitud:", request.data)
        if serializer.is_valid():
            mail = serializer.validated_data.get('mail')
            password = serializer.validated_data.get('password')
            user = authenticate(request, username=mail, password=password)

            if user is not None:
                token, created = Token.objects.get_or_create(user=user)
                return Response({'token': token.key, 'message': '1'}, status=status.HTTP_200_OK)
            else:
                return Response({'error': 'Usuario o contraseña incorrectos','message': '0'})
        else:
            print("Datos de la solicitud no válidos:", serializer.errors)
            return Response({'error': 'Datos de la solicitud no válidos', 'message': serializer.errors}, status=status.HTTP_400_BAD_REQUEST)

@api_view(['POST'])
def cerrarSesion(request):
    if request.method == 'POST':
        request.user.auth_token.delete()
        return Response({'message': '1'}, status=status.HTTP_200_OK)
    return Response({'message': '0'}, status=status.HTTP_400_BAD_REQUEST)


@api_view(['POST'])
def obtener_datos_usuario(request):
    mail = request.data.get('email')
    print(mail)
    try:
        user = Usuario.objects.get(mail=mail)
        try: 
            perfil_usuario = perfil.objects.get(usuario=user)
            image_url = perfil_usuario.fotoPerfil
            with open(image_url, 'rb') as f:
                image_bytes = f.read()
                image_base64 = base64.b64encode(image_bytes).decode('utf-8')
        except ObjectDoesNotExist:
            return Response({'message': 'Perfil no encontrado'}, status=404)
        return Response({
            'username': user.nombreUsuario,
            'fullname': user.nombreReal,
            'email': user.mail,
            'fotoPerfil': image_base64,
            'message': '1'
        })
    except ObjectDoesNotExist:
        return Response({'message': 'Usuario no encontrado'}, status=404)


@api_view(['POST'])
def borrar_usuario(request):
    email = request.data.get('email')
    try:
        user = Usuario.objects.get(mail=email)
        user.delete()
        return Response({'message': '1'})
    except ObjectDoesNotExist:
        return Response({'message': 'Usuario no encontrado'}, status=404)


@api_view(['POST'])
def modificar_usuario(request):
    email = request.data.get('email')
    username = request.data.get('username')
    fullname = request.data.get('fullname')
    foto_base64 = request.data.get('fotoPerfil') # Obtén la imagen de perfil del request
    # foto_bytes = base64.b64decode(foto_base64)
    print(email, username, fullname)
    # print("foto de perfil:", foto_base64)
    try:    
        user = Usuario.objects.get(mail=email)

        # Solo actualiza los campos si se proporcionan
        if username:
            user.nombreUsuario = username
        if fullname:
            user.nombreReal = fullname

        user.save()
    
        #  Actualiza la imagen de perfil
        if foto_base64:
            foto_bytes = base64.b64decode(foto_base64)
            image_dir = os.path.join(settings.MEDIA_ROOT, 'perfil_images')
            os.makedirs(image_dir, exist_ok=True)  # Crea la carpeta si no existe
            image_path = os.path.join(image_dir, f'{user.mail}.jpg')
            with open(image_path, 'wb') as f:
                f.write(foto_bytes)

            # Guarda la URL de la imagen en la base de datos
            imagen_url = os.path.join(settings.MEDIA_ROOT, 'perfil_images', f'{user.mail}.jpg')
            try:
                perfil_usuario = perfil.objects.get(usuario=user)
                perfil_usuario.fotoPerfil = imagen_url
                perfil_usuario.save()
            # Decodificar el string base64 y crear un objeto Image
            # Guardar la imagen en el campo fotoPerfil
            except perfil.DoesNotExist:
                perfil_usuario = perfil.objects.create(usuario=user, fotoPerfil=imagen_url)
        return Response({'message': '1'})
    except ObjectDoesNotExist as e:
        # return Response({'message': str(e)}, status=404)
        return Response({'message': 'Usuario no encontrado'}, status=404)



@api_view(['POST'])
def bloquear_cuenta(request):
    email = request.data.get('email')
    print(email)
    try:
        user = Usuario.objects.get(mail=email)
        user.is_active = False
        user.save()
        return Response({'message': '1'})
    except ObjectDoesNotExist:
        return Response({'message': 'Usuario no encontrado'}, status=404)