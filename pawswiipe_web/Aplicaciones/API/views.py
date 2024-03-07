from django.forms import ImageField
from django.shortcuts import render, redirect, get_object_or_404
from django.http import HttpResponse
from requests import Response
from Aplicaciones.bbdd.models import *
from Aplicaciones.forms.formulario import UsuarioForm
from Aplicaciones.forms.formularioLogin import LoginForm
from django.contrib import messages
from django.contrib.auth.hashers import make_password
from django.contrib.auth import  login, logout, authenticate
from django.contrib.auth.decorators import login_required
from django.contrib.auth import update_session_auth_hash
from rest_framework import status
from rest_framework.decorators import api_view
from Aplicaciones.bbdd.serializers import UsuarioSerializer , LoginSerializer
from rest_framework.response import Response
from rest_framework.authtoken.models import Token
from django.db.models import Q
from django.core.exceptions import ObjectDoesNotExist


@api_view(['POST'])
def registrar_usuario(request):
    print(request.data)
    if request.method == 'POST':
        serializer = UsuarioSerializer(data=request.data)
        if serializer.is_valid():
            password = serializer.validated_data.get('password')
            """ if not validar_contrasena(password=password):
                return Response({"error": 'La contraseña debe contener al menos una letra mayúscula, una minúscula, un número y un carácter especial'}, status=status.HTTP_400_BAD_REQUEST) """
            encriptar = make_password(password)
            serializer.validated_data['password'] = encriptar
            user = serializer.save()
            respuesta_data = serializer.data
            respuesta_data["message"] = 1
            return Response(respuesta_data, status=status.HTTP_201_CREATED)
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
        return Response({
            'username': user.nombreUsuario,
            'fullname': user.nombreReal,
            'email': user.mail,
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
    fotoPerfil = request.FILES.get('fotoPerfil')  # Obtén la imagen de perfil del request
    print(email, username, fullname)
    print("foto de perfil:", fotoPerfil)
    try:
        user = Usuario.objects.get(mail=email)
        
        # Solo actualiza los campos si se proporcionan
        if username:
            user.nombreUsuario = username
        if fullname:
            user.nombreReal = fullname
        user.save()

        """ # Actualiza la imagen de perfil
        perfil_usuario = perfil.objects.get(usuario=user)
        if fotoPerfil is not None:
            perfil_usuario.fotoPerfil = ImageField(fotoPerfil)
            perfil_usuario.save() """

        return Response({'message': '1'})
    except ObjectDoesNotExist:
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