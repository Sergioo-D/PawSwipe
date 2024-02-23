from django.shortcuts import render, redirect, get_object_or_404
from django.http import HttpResponse
from requests import Response
from Aplicaciones.bbdd.models import *
from Aplicaciones.forms.formulario import UsuarioForm
from Aplicaciones.forms.formularioLogin import LoginForm
from django.contrib import messages
from django.contrib.auth.hashers import make_password
from django.contrib.auth import  login, logout, authenticate
from .decorators import redirigirUsuarios , validar_contrasena
from django.contrib.auth.decorators import login_required
from django.contrib.auth import update_session_auth_hash
from rest_framework import status
from rest_framework.decorators import api_view
from .serializers import UsuarioSerializer , LoginSerializer
from rest_framework.response import Response
from rest_framework.authtoken.models import Token
from django.db.models import Q
from django.core.exceptions import ObjectDoesNotExist

@login_required(login_url='home')
def feed(request):
    print("Usuario autenticado:" ,request.user.is_authenticated)
    return render(request, 'feed.html')

@redirigirUsuarios
def home(request):
    if request.method == 'POST':
        mail = request.POST.get('mail')
        password = request.POST.get('password')
        user = authenticate(request, username=mail, password=password)
        sistema = request.META['HTTP_USER_AGENT']
        if user is not None:
            login(request, user)
            request.session['failed_login_attempts'] = 0
            try:
                usuario = Usuario.objects.get(mail=mail)
                RegistroInicioSession.objects.create(mail=usuario, login_exitoso=True, sistema=sistema)
            except Usuario.DoesNotExist:
                print("Usuario no encontrado en la base de datos")
            print("Usuario autenticado:", request.user.is_authenticated)
            if user.is_superuser:  # comprobar si el usuario es un administrador
                return redirect('/admin/')  # si es administrador, redirigir a la página de administración
            else:
                return redirect('feed')  # los usuarios normales son redirigidos a la página de inicio
        else:
            usuario = Usuario.objects.get(mail=mail)
            RegistroInicioSession.objects.create(mail=usuario, login_exitoso=False, sistema=sistema)
            failed_attempts = request.session.get('failed_login_attempts', 0) + 1
            request.session['failed_login_attempts'] = failed_attempts
            if failed_attempts >= 3:
                try: # bloquear la cuenta si el usuario ha intentado iniciar sesión 3 veces sin éxito
                    usuario = Usuario.objects.get(mail=mail)
                    usuario.is_active = False
                    usuario.save()
                    messages.error(request, 'Se ha bloqueado su cuenta por intentar iniciar sesión 3 veces sin éxito')
                except Usuario.DoesNotExist:
                    print("Usuario no encontrado en la base de datos")    
    return render(request, 'login.html')




@redirigirUsuarios
def registro(request):
    if request.method == 'POST':
        formulario = UsuarioForm(request.POST)
        print(formulario.errors)
        if formulario.is_valid():
            nombreUsuario = formulario.cleaned_data.get('nombreUsuario')
            if Usuario.objects.filter(nombreUsuario=nombreUsuario).exists():
                messages.error(request, 'Este nombre de usuario ya existe')
                return redirect('registro')
            else:
                if not validar_contrasena(password=formulario.cleaned_data.get('password')):
                    messages.error(request, 'La contraseña debe contener al menos una letra mayúscula, una minúscula, un número y un carácter especial')
                    return redirect('registro')
                password = make_password(formulario.cleaned_data.get('password'))
                usuario = Usuario(
                    nombreUsuario=nombreUsuario,
                    password=password,
                    nombreReal=formulario.cleaned_data.get('nombreReal'),
                    mail=formulario.cleaned_data.get('mail'),
                    fecha=formulario.cleaned_data.get('fecha')
                )
                
                usuario.save()
                return redirect(home)
        else:
            return render(request, "formRegistro.html", {"form": formulario})  # Devuelve una respuesta si el formulario no es válido
    else:
        formulario = UsuarioForm()
        return render(request, "formRegistro.html", {"form": formulario})
    

def logOut(request):
    logout(request)
    return redirect(home)

@login_required(login_url='home')
def perfil(request):
    mail = request.user.mail
    print("Usuario autenticado:" ,request.user.is_authenticated)
    return render(request, 'perfilUsuario.html', {'mail': mail})


def eliminarCuenta(request):
    from django.core.exceptions import ObjectDoesNotExist
    try:
        mail = request.user.mail
        user = Usuario.objects.get(mail=mail)
        logout(request)
        user.delete()
    except ObjectDoesNotExist:
        print("Usuario no existe")

    return redirect('home')

def modificarDatos(request):
    if request.method == 'POST':
        user = request.user
        nombreUsuario = request.POST.get('nombreUsuario')
        password = request.POST.get('password')

        if nombreUsuario:
            user.nombreUsuario = nombreUsuario

        if password:
            user.set_password(password)

        user.save()

        # Actualizar la sesión del usuario para que no se cierre la sesión después de cambiar la contraseña
        update_session_auth_hash(request, user)
        return redirect('perfil')
    else:
        formulario = UsuarioForm(instance=user)
        return render(request, 'perfilUsuario.html', {'form': formulario})
    
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
    print(email, username, fullname)
    try:
        user = Usuario.objects.get(mail=email)
        user.nombreUsuario = username
        user.nombreReal = fullname
        user.save()
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



@login_required
def inbox(request):
    user = request.user
    msgs = MensajeDirecto.getMessages(user = user)
    active_direct = None
    direct = None
    salas = Sala.objects.all()  # Obtiene todas las salas

    if msgs:
        msg = msgs[0]
        active_direct = msg['user'].mail
        direct = MensajeDirecto.objects.filter(user=user, receptor = msg['user'])
        direct.update(is_read=True)

        for msg in msgs:
            if msg['user'].mail == active_direct:
                msg['unread'] = 0
        context = {
            'directs': direct,
            'active_direct': active_direct,
            'msgs': msgs,
            'salas': salas,  # Añade las salas al contexto
        }
    else:
        context = {
            'directs': None,
            'active_direct': None,
            'msgs': [],
            'salas': salas,  # Añade las salas al contexto
        }

    return render(request, 'inbox.html', context)     

def search_users(request):
    query = request.GET.get('q')
    object_list = Usuario.objects.filter(
        Q(mail__icontains=query)
    )
    context = {
        'users': object_list
    }
    return render(request, 'buscarUsuario.html', context)

def send_direct(request):
    emisor = request.user
    print(emisor)
    receptor_username = request.POST.get('receptor')
    print(receptor_username)
    mensaje = request.POST.get('mensaje')

    if request.method == 'POST':
        receptor = Usuario.objects.get(mail=receptor_username)
        print(receptor)
        print(emisor)
        print(mensaje)
        MensajeDirecto.sendMessage(emisor=request.user, receptor=receptor, mensaje=mensaje)
        return render(request, 'inbox.html')
    print(request.POST)



def iniciar_chat(request, receptor):
    emisor = request.user
    #receptor = Usuario.objects.get(mail=receptor)

    sala, created = Sala.objects.get_or_create(
         Q(emisor=emisor), #receptor=receptor),  #Q(emisor=receptor, receptor=emisor),
        defaults={'emisor': emisor, 'receptor': receptor, 'slug': uuid.uuid4()}
    )
    salas = Sala.objects.filter(Q(emisor=emisor) | Q(receptor=emisor))
    print(salas,"hola")
    return render(request, 'inbox.html','chat.html' ,{'salas': salas})

def chat(request,slug):
    sala = Sala.objects.get(slug=slug)
    if request.user not in [sala.emisor, sala.receptor]:
        return redirect('inbox')
    mensajes = MensajeDirecto.objects.filter(Q(emisor=sala.emisor, receptor=sala.receptor) | Q(emisor=sala.receptor, receptor=sala.emisor))
    salas = Sala.objects.filter(Q(emisor=sala.emisor) | Q(receptor=sala.emisor))
    return render(request, 'chat.html', {'sala': sala, 'mensajes': mensajes,'salas': salas})

def enviar_mensaje(request,slug):
    sala = get_object_or_404(Sala, slug=slug)
    mensaje = request.POST.get('mensaje')

    if request.method == 'POST':
        MensajeDirecto.objects.create(
            user=request.user,
            emisor=request.user,
            receptor=sala.receptor if request.user == sala.emisor else sala.emisor,
            mensaje=mensaje
        )
        return redirect('chat', slug=sala.slug)