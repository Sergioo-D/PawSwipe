import json
from django.utils import timezone
import os
from django.conf import settings
from django.shortcuts import render, redirect, get_object_or_404
from django.http import HttpResponse, JsonResponse
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
from django.views.decorators.http import require_POST
from dateutil.relativedelta import relativedelta
from datetime import datetime

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
            fotoPerfil=""
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

    es_propietario = True
    todas_las_mascotas = usuario.mascotas.all()
    perfil = mascota_actual.perfil
    publicaciones = perfil.publicaciones.all()
    for publicacion in publicaciones:
        publicacion.image_urls = [imagen.urlImagen.url for imagen in publicacion.imagenes.all()]

    context = {
        'mascota_actual': mascota_actual,
        'todas_las_mascotas': todas_las_mascotas,
        'perfil': perfil,
        'publicaciones': publicaciones,
        'es_propietario':es_propietario
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
    
def modificarDatosMascota(request, mascota_id):
    if request.method == 'POST':
        mascota = get_object_or_404(Mascota, id=mascota_id, usuario=request.user)
        fotoPerfil = request.FILES.get('img')
        nombre = request.POST.get('nombreUsuario')
        despcripcion = request.POST.get('descripcion')
        perfil = get_object_or_404(Perfil, mascota_id=mascota_id)
        
        if fotoPerfil:
            perfil.fotoPerfil = fotoPerfil
            perfil.save()

        if nombre:
            mascota.nombre = nombre

        if despcripcion:
            mascota.descripcion = despcripcion

        mascota.save()

        # Actualizar la sesión del usuario para que no se cierre la sesión después de cambiar la contraseña
        return redirect('perfil')
    else:
        formulario = MascotaForm(instance=mascota)
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
    mascota = get_object_or_404(Mascota, pk=mascota_id)

    if mascota.usuario == request.user:
        # Si el usuario es propietario de la mascota, actualiza la sesión y redirige a la vista 'perfil_mascota'
        request.session['mascota_actual_id'] = mascota_id
        return redirect('perfil_mascota', mascota_id=mascota_id)
    else:
        # Si el usuario no es propietario, actualiza la sesión y redirige a la vista 'perfil'
        request.session['mascota_actual_id'] = mascota_id
        return redirect('perfil-mascota', mascota_id=mascota_id)
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
     
def delete_comentario(request, comentario_id):
    try:
        comentario = Comentario.objects.get(id=comentario_id)
        comentario.delete()
        return JsonResponse({'success': 'Comentario eliminado correctamente'})
    except Comentario.DoesNotExist:
        return JsonResponse({'error': 'Comentario no existe'}, status=404)
     
@require_POST
def like_post(request, publicacion_id):
    if not request.user.is_authenticated:
        return JsonResponse({'error': 'Authentication required'}, status=401)
    
    publicacion = get_object_or_404(Publicacion, id=publicacion_id)

    try:
        # Obtener la mascota actual del usuario
        mascota_actual_id = request.session.get('mascota_actual_id')
        if mascota_actual_id:
            mascota_actual = get_object_or_404(Mascota, pk=mascota_actual_id)
            perfil_actual = mascota_actual.perfil
        else:
            return JsonResponse({'error': 'No se encontró la mascota actual en la sesión'}, status=404)

        try:
            # Intenta obtener el like del perfil actual para esta publicación
            like = Like.objects.get(perfil=perfil_actual, publicacion=publicacion)
            # Si el like existe, elimínalo y decrementa el conteo de likes
            like.delete()
            publicacion.likes -= 1
            publicacion.save()
            return JsonResponse({'message': 'Like removed', 'likes': publicacion.likes})
        except Like.DoesNotExist:
            # Si el like no existe, crea uno nuevo y aumenta el conteo de likes
            Like.objects.create(perfil=perfil_actual, publicacion=publicacion)
            publicacion.likes += 1
            publicacion.save()
            return JsonResponse({'message': 'Like added', 'likes': publicacion.likes})
    except Exception as e:
        return JsonResponse({'error': str(e)}, status=500)

    
def has_liked(request, publicacion_id):
    if not request.user.is_authenticated:
        return JsonResponse({'error': 'Authentication required'}, status=401)

    # Obtener la publicación y asegurarse de que existe
    publicacion = get_object_or_404(Publicacion, id=publicacion_id)

    # Obtener la mascota actual del usuario
    mascota_actual_id = request.session.get('mascota_actual_id')
    if mascota_actual_id:
        mascota_actual = get_object_or_404(Mascota, pk=mascota_actual_id)
        perfil_actual = mascota_actual.perfil
    else:
        return JsonResponse({'error': 'No se encontró la mascota actual en la sesión'}, status=404)

    # Comprobar si el perfil del usuario actual ha dado "like" a esta publicación
    has_liked = Like.objects.filter(perfil=perfil_actual, publicacion=publicacion).exists()

    return JsonResponse({'has_liked': has_liked})
    


def get_likes(request, publicacion_id):
    if not request.user.is_authenticated:
        return JsonResponse({'error': 'Authentication required'}, status=401)
    
    publicacion = get_object_or_404(Publicacion, id=publicacion_id)
    es_propietario = request.user == publicacion.perfil.mascota.usuario
    likes = Like.objects.filter(publicacion=publicacion).select_related('perfil')
    likes_list = [{
        'mascotaNombre': like.perfil.mascota.nombre,
        'mascotaId': like.perfil.mascota.id,
        'fotoPerfil': like.perfil.fotoPerfil.url if like.perfil.fotoPerfil else None,
        'perfilUrl': reverse('perfil-mascota', args=[like.perfil.mascota.id]),
        'es_propietario': es_propietario
        
    } for like in likes if like.perfil.mascota]
    return JsonResponse({'likes': publicacion.likes, 'lista_likes': likes_list})

# @require_POST
# def get_likes(request, publicacion_id):
#     if not request.user.is_authenticated:
#         return JsonResponse({'error': 'Authentication required'}, status=401)

#     publicacion = get_object_or_404(Publicacion, id=publicacion_id)

#     # Suponiendo que cada usuario puede tener varias mascotas y que Mascota tiene un campo 'usuario'
#     # que relaciona directamente a la mascota con el usuario.
#     perfiles_excluir = Mascota.objects.filter(usuario=request.user).values_list('perfil_id', flat=True)
    
#     # Filtrar los likes para excluir los perfiles de las mascotas del usuario actual
#     likes = Like.objects.filter(publicacion=publicacion).exclude(perfil_id__in=perfiles_excluir).select_related('perfil')

#     # Lista de nombres de mascotas de los perfiles que dieron like, excluyendo los del usuario actual
#     likes_list = [like.perfil.mascota.nombre for like in likes if like.perfil.mascota]

#     return JsonResponse({'likes': publicacion.likes, 'lista_likes': likes_list})


@require_POST
def comentario_post(request, publicacion_id):
    print(request.body)
    if not request.user.is_authenticated:
        return JsonResponse({'error': 'Authentication required'}, status=401)

    # Recuperar el ID de la mascota activa desde la sesión
    mascota_actual_id = request.session.get('mascota_actual_id')
    if not mascota_actual_id:
        return JsonResponse({'error': 'No active pet selected'}, status=400)

    # Obtener la mascota y perfil actual basado en la sesión
    mascota_actual = get_object_or_404(Mascota, pk=mascota_actual_id)
    perfil_actual = mascota_actual.perfil

    data = json.loads(request.body)
    texto = data.get('texto', '').strip()

    if not texto:
        return JsonResponse({'error': 'El texto del comentario no puede estar vacío'}, status=400)

    try:
        publicacion = get_object_or_404(Publicacion, id=publicacion_id)
        nuevo_comentario = Comentario.objects.create(publicacion=publicacion, perfil=perfil_actual, texto=texto)
        return JsonResponse({
            'nuevoComentario': {
                'autor': perfil_actual.mascota.nombre,
                'texto': texto, 
                'fotoPerfil': perfil_actual.fotoPerfil.url,
                'fecha': formatear_datetime(timezone.now())   
            }
            }, status=201)
    except Exception as e:
        return JsonResponse({'error': str(e)}, status=500)
    
def get_comments(request, publicacion_id):
    if not request.user.is_authenticated:
        return JsonResponse({'error': 'Authentication required'}, status=401)
    publicacion = get_object_or_404(Publicacion, id=publicacion_id)
    es_propietario = request.user == publicacion.perfil.mascota.usuario
    comentarios = Comentario.objects.filter(publicacion=publicacion).select_related('perfil')
    comentarios_list = [{
        'id': comentario.id,
        'autor': comentario.perfil.mascota.nombre,
        'urlPerfil': reverse('perfil-mascota', args=[comentario.perfil.mascota.id]),
        'texto': comentario.texto, 
        'fecha': formatear_datetime(comentario.fecha_creacion),
        'fotoPerfil': comentario.perfil.fotoPerfil.url if comentario.perfil.fotoPerfil else None,
        'esPropiertario': es_propietario
        
        } for comentario in comentarios]
    return JsonResponse({'comentarios': comentarios_list})

def formatear_datetime(dt):
    # Asegúrate de que 'dt' sea una fecha "aware"
    now = timezone.now()
    delta = relativedelta(now, dt)
    if delta.years > 0:
        return f'hace {delta.years} años' if delta.years > 1 else 'hace un año'
    if delta.months > 0:
        return f'hace {delta.months} meses' if delta.months > 1 else 'hace un mes'
    if delta.days > 0:
        return f'hace {delta.days} días' if delta.days > 1 else 'hace un día'
    if delta.hours > 0:
        return f'hace {delta.hours} horas' if delta.hours > 1 else 'hace una hora'
    if delta.minutes > 0:
        return f'hace {delta.minutes} minutos' if delta.minutes > 1 else 'hace un minuto'
    return 'hace unos segundos'

def buscar_perfiles(request):
    query = request.GET.get('q', '')
    if query:
        # Utiliza 'select_related' para recuperar la información de perfil en la misma consulta
        mascotas = Mascota.objects.filter(nombre__icontains=query).exclude(usuario=request.user).select_related('perfil')
        resultados = [
            {
                'id': mascota.id,
                'nombre': mascota.nombre,
                'foto_url': mascota.perfil.fotoPerfil.url if mascota.perfil and mascota.perfil.fotoPerfil else None,
                'perfil_url': reverse('perfil-mascota', args=[mascota.id])
            }
            for mascota in mascotas
        ]
    else:
        resultados = []
    return JsonResponse({'resultados': resultados})

def perfil_mascota(request, mascota_id):
    mascota_actual = get_object_or_404(Mascota, pk=mascota_id)
    es_propietario = mascota_actual.usuario == request.user
    todas_las_mascotas = mascota_actual.usuario.mascotas.all()
    mascota_session_id = request.session.get('mascota_actual_id')
    mascota_sesion = Mascota.objects.get(pk=mascota_session_id, usuario=request.user)
    perfil_usuario = mascota_sesion.perfil
    siguiendo = perfil_usuario.siguiendo.filter(id=mascota_actual.perfil.id).exists()
    
    context = {
        'mascota_actual': mascota_actual,
        'es_propietario': es_propietario,
        'publicaciones': mascota_actual.perfil.publicaciones.all(),
        'perfil': mascota_actual.perfil,
        'todas_las_mascotas': todas_las_mascotas,
        'siguiendo': siguiendo 
    }
    return render(request, 'perfilUsuario.html', context)

@login_required
@require_POST  # Asegura que esta vista sólo acepte peticiones POST
def seguir_perfil(request, perfil_id):
    # Asegúrate de que la sesión tiene una mascota seleccionada
    mascota_actual_id = request.session.get('mascota_actual_id')
    perfil_a_seguir = get_object_or_404(Perfil, id=perfil_id)
    perfil_usuario = get_object_or_404(Perfil, mascota__id=mascota_actual_id)
    
    if perfil_usuario == perfil_a_seguir:
        return JsonResponse({'error': 'No puedes seguirte a ti mismo'}, status=400)

    # Comprobar si ya está siguiendo al perfil
    if perfil_a_seguir in perfil_usuario.siguiendo.all():
        perfil_usuario.siguiendo.remove(perfil_a_seguir)
        accion = 'Dejado de seguir'
    else:
        perfil_usuario.siguiendo.add(perfil_a_seguir)
        accion = 'Seguido'
        
    numSeguidores = perfil_a_seguir.seguidores.count()

    return JsonResponse({
        'success': f'Has {accion} a {perfil_a_seguir.mascota.nombre} correctamente',
        'numSeguidores': numSeguidores
        })



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