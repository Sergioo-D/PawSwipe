from django.http import HttpResponse
from django.shortcuts import render
from Aplicaciones.templates import *

# Create your views here.
def index(request):
    return render(request,'index.html', {})

def Salaa(request, Sala):
    return render(request, 'chatroom.html', {
        'Sala': Sala
    })