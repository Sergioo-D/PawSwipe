from django.http import HttpResponse
from django.shortcuts import redirect, render
from Aplicaciones.bbdd.models import Sala, MensajeDirecto
from Aplicaciones.templates import *
from django.template.loader import render_to_string
from django.http import JsonResponse
from django.contrib.auth.decorators import login_required

# Create your views here.
def iinbox(request):
    if not request.user.is_authenticated:
        return redirect('login')
    
    salas = Sala.objects.filter(emisor=request.user) | Sala.objects.filter(receptor=request.user)
    print(salas)
    return render(request,'index.html', {'salas': salas})


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

    data = {
    'sala': str(sala), 
    'usuario_actual': usuario_actual.nombreUsuario, 
    'el_otro': el_otro.nombreUsuario,
    'mensajes': obtener_dm(sala)
}
    
    print(data)

    return JsonResponse(data)

def guardar_dm(emisor, receptor, mensaje):
    nuevo_dm = MensajeDirecto(emisor=emisor, receptor=receptor, mensaje=mensaje)
    nuevo_dm.save()

def obtener_dm(sala):
    mensajes = MensajeDirecto.objects.filter(sala=sala)
    return mensajes

