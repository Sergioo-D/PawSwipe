from django.utils import timezone
import os
from django.conf import settings
from django.shortcuts import render, redirect, get_object_or_404
from django.http import HttpResponse
from requests import Response
from Aplicaciones.bbdd.models import *
from Aplicaciones.forms.formulario import ImagenForm, MascotaForm, PublicacionForm, UsuarioForm
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
                messages.error(request, 'Usuario o contraseña incorrectos')
                print("Usuario no encontrado en la base de datos")
            print("Usuario autenticado:", request.user.is_authenticated)
            if user.is_superuser:  # comprobar si el usuario es un administrador
                return redirect('/admin/')  # si es administrador, redirigir a la página de administración
            else:
                if user.mascotas.exists():  # para verificar si hay mascotas
                    return redirect('feed')  # Redirigir al feed si hay mascotas
                else:
                    return redirect('registro_mascota_initial', mail=user.mail, initial=1)  # Redirigir a registro de mascota si no hay mascotas
        else:
            try:
                usuario = Usuario.objects.get(mail=mail)
                RegistroInicioSession.objects.create(mail=usuario, login_exitoso=False, sistema=sistema)
                failed_attempts = request.session.get('failed_login_attempts', 0) + 1
                request.session['failed_login_attempts'] = failed_attempts
                if failed_attempts >= 3:
                    usuario.is_active = False
                    usuario.save()
                    messages.error(request, 'Se ha bloqueado su cuenta por intentar iniciar sesión 3 veces sin éxito')
            except Usuario.DoesNotExist:
                messages.error(request, 'Usuario o contraseña incorrectos')
                print("Usuario no encontrado en la base de datos")    
    return render(request, 'login.html')

def crear_perfil_default(mascota):
    try:
        perfil_default = Perfil.objects.create(
            mascota=mascota, 
            fotoPerfil="",
            numSeguidores=0, 
            numSeguidos=0, 
            totalPublicaciones=0
        )
        perfil_default.save()
    except Exception as e:
        print(f"No se pudo crear el perfil predeterminado para {mascota.nombre}: {e}")


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
                return redirect(reverse('registro_mascota_initial', args=[usuario.mail, 1]))
        else:
            return render(request, "formRegistro.html", {"form": formulario})  # Devuelve una respuesta si el formulario no es válido
    else:
        formulario = UsuarioForm()
        return render(request, "formRegistro.html", {"form": formulario})
    
def registro_mascota(request, mail, initial=None):
    usuario = Usuario.objects.get(mail=mail)
    if request.method == 'POST':
        form = MascotaForm(request.POST)
        if form.is_valid():
            nombre = form.cleaned_data.get('nombre')
            if Mascota.objects.filter(nombre=nombre).exists():
                messages.error(request, 'El nombre de perfil ya existe')
                return redirect('registro_mascota')
            else:
                mascota= Mascota(
                    usuario=usuario,
                    nombre=nombre,
                    descripcion=form.cleaned_data.get('descripcion'),
                )
                mascota.save()
                crear_perfil_default(mascota)
                if initial:
                    return redirect('home')
                else:
                    return redirect('perfil')
                    
    else:
        form = MascotaForm()
    context = {'form': form, 'usuario': usuario, 'initial': initial}
    return render(request, 'formRegistroMascota.html', context)

def logOut(request):
    logout(request)
    return redirect(home)

@login_required(login_url='home')
def perfil(request, mascota_id=None):
    usuario = request.user
    if mascota_id:
        mascota_actual = get_object_or_404(Mascota, pk=mascota_id, usuario=usuario)
    else:
        mascota_actual = usuario.mascotas.first()
        guardar_mascota_actual(request, mascota_actual.id)

    todas_las_mascotas = usuario.mascotas.all()
    perfil = mascota_actual.perfil
    publicaciones = perfil.publicaciones.all()
    for publicacion in publicaciones:
        publicacion.image_urls = [imagen.urlImagen.url for imagen in publicacion.imagenes.all()]

    context = {
        'mascota_actual': mascota_actual,
        'todas_las_mascotas': todas_las_mascotas,
        'perfil': perfil,
        'publicaciones': publicaciones
    }
    return render(request, 'perfilUsuario.html', context)

def eliminar_mascota(request, mascota_id):
    mascota = get_object_or_404(Mascota, id=mascota_id, usuario=request.user)  # Asegúrate de que solo el dueño pueda eliminar la mascota
    mascota.delete()
    return redirect('perfil') 

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
    
def create_post_view(request):
    mascota = get_object_or_404(Mascota, pk=request.session['mascota_actual_id'])  # Asegura que la mascota existe
    if request.method == 'POST':
        descripcion = request.POST.get('descripcion', '')
        images = request.FILES.getlist('images')
        if images:
            publicacion = Publicacion(perfil=mascota.perfil, descripcion=descripcion, fechaPublicacion=timezone.now(), likes=0)
            publicacion.save()
            perfil = Perfil.objects.get(mascota_id=mascota.id)
            perfil.totalPublicaciones += 1
            perfil.save()
            for image in images:
                Imagen.objects.create(publicacion=publicacion, urlImagen=image)
            return redirect('feed')  # Redirige a la página de inicio o feed
    return redirect('perfil') 

def guardar_mascota_actual(request, mascota_id):
    request.session['mascota_actual_id'] = mascota_id
    return redirect('perfil_mascota', mascota_id=mascota_id)
 
# @require_http_methods(["POST"])   
# @csrf_protect 
def delete_post(request, publicacion_id):
    try:
        post = Publicacion.objects.get(id=publicacion_id)
        id_mascota = post.perfil.mascota_id
        perfil = Perfil.objects.get(mascota_id=id_mascota)
        perfil.totalPublicaciones -= 1
        perfil.save()
        post.delete()
        return redirect('perfil_mascota', mascota_id=id_mascota)
    except Publicacion.DoesNotExist:
         return HttpResponse("Publicación no existe", status=404)
        

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
    
def chattt(request):
    return render(request, 'chatprueba3.html')    