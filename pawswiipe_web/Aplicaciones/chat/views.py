import datetime
import uuid
from django.http import HttpResponse
from django.shortcuts import get_object_or_404, redirect, render
from django.urls import reverse
from Aplicaciones.bbdd.models import Sala, MensajeDirecto, Usuario,Mascota
from Aplicaciones.templates import *
from django.template.loader import render_to_string
from django.http import JsonResponse
from django.contrib.auth.decorators import login_required
from django.views.decorators.http import require_POST
import json
from django.views.decorators.csrf import csrf_exempt
from django.core import serializers

# Create your views here.
def iinbox(request):
    if not request.user.is_authenticated:
        return redirect('login')
    
    salas = Sala.objects.filter(emisor=request.user) | Sala.objects.filter(receptor=request.user)
    nombreMascota = request.session.get('mascota_actual_nombre')
    print(nombreMascota)

    return render(request, 'index.html', {'salas': salas, 'nombreMascota': nombreMascota})


@login_required
def Salaa(request, slug):
    sala = Sala.objects.get(slug=slug)
    emisor = sala.emisor
    receptor = sala.receptor

    usuario_actual = request.user

    if usuario_actual == emisor:
        el_otro = receptor
    else:
        el_otro = emisor

    mensajes = MensajeDirecto.objects.filter(sala=sala).order_by('-timestamp')  # Asegúrate de que tienes un campo timestamp en tus modelos

    mensajes_data = [
        {
            'emisor': mensaje.emisor.mail,
            'receptor': mensaje.receptor.mail,
            'mensaje': mensaje.mensaje,
            'timestamp': mensaje.timestamp.strftime('%Y-%m-%d %H:%M:%S'),  # Asegúrate de que el modelo tiene 'timestamp'
            'es_emisor': mensaje.emisor == usuario_actual  # Determina si el usuario actual es el emisor
        }
        for mensaje in mensajes
    ]
    print("aaaa:",sala.nombre_mascota_receptor)
    data = {
        'sala': slug,
        'usuario_actual': usuario_actual.mail,
        'el_otro': el_otro.mail,
        'el_otro_nombre': el_otro.nombreUsuario,
        'mensajes': mensajes_data,
        'nombre_mascota_receptor': sala.nombre_mascota_receptor,
        'nombre_mascota_emisor': sala.nombre_mascota_emisor
    }
    
    
    return JsonResponse(data) 


@require_POST
@login_required
def guardar_dm(request):
    if request.method == 'POST':
        try:
            data = json.loads(request.body)

            emisor_mail = data.get('emisor')
            receptor_mail = data.get('receptor')
            mensaje = data.get('message')
            slug = data.get('slug')

            try:
                emisor = Usuario.objects.get(mail=emisor_mail)
                receptor = Usuario.objects.get(mail=receptor_mail)
                sala = Sala.objects.get(slug=slug)

            except Usuario.DoesNotExist:
                return JsonResponse({'status': 'failed', 'message': 'Usuario no encontrado'}, status=404)
            except Sala.DoesNotExist:
                return JsonResponse({'status': 'failed', 'message': 'Sala no encontrada'}, status=404)

            nuevo_dm = MensajeDirecto(
                emisor=emisor,
                receptor=receptor,
                mensaje=mensaje,
                is_read=False,
                sala=sala,
                # timestamp=datetime.now()
            )
            nuevo_dm.save()
            return JsonResponse({'status': 'success'})
        except Exception as e:
            print("Error:", str(e))
            return JsonResponse({'status': 'failed', 'message': str(e)}, status=500)
    else:
        print("No es un POST")

def obtener_dm(sala):
    mensajes = MensajeDirecto.objects.filter(sala=sala).values()
    return list(mensajes)



# def datos_mascota(request, mascota_id):
#     mascota_actual = get_object_or_404(Mascota, pk=mascota_id)
#     es_propietario = mascota_actual.usuario == request.user
#     todas_las_mascotas = mascota_actual.usuario.mascotas.all()
#     mascota_session_id = request.session.get('mascota_actual_id')
#     mascota_sesion = Mascota.objects.get(pk=mascota_session_id, usuario=request.user)
#     perfil_usuario = mascota_sesion.perfil
#     siguiendo = perfil_usuario.siguiendo.filter(id=mascota_actual.perfil.id).exists()
    
#     context = {
#         'mascota_actual': mascota_actual,
#         'es_propietario': es_propietario,
#         'publicaciones': mascota_actual.perfil.publicaciones.all(),
#         'perfil': mascota_actual.perfil,
#         'todas_las_mascotas': todas_las_mascotas,
#         'siguiendo': siguiendo,
#         'usuario': request.user,
#         'user_mascota' : mascota_actual.usuario.mail
#     }
#     return render(request, 'index.html', context)    
