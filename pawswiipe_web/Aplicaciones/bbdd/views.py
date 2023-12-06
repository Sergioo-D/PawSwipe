from django.shortcuts import render, redirect, get_object_or_404
from django.http import HttpResponse
from Aplicaciones.bbdd.models import Usuario
from Aplicaciones.forms.formulario import UsuarioForm
from Aplicaciones.forms.formularioLogin import LoginForm
from django.contrib import messages
from django.contrib.auth.hashers import make_password
from django.contrib.auth import  login, logout, authenticate
from .decorators import redirigirUsuarios
from django.contrib.auth.decorators import login_required

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
        if user is not None:
            login(request, user)
            print("Usuario autenticado:", request.user.is_authenticated)
            return redirect('feed')
        else:
            print("Usuario no encontrado")
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
    identificador = request.user.identificador
    print("Usuario autenticado:" ,request.user.is_authenticated)
    return render(request, 'perfilUsuario.html', {'identificador': identificador})


def eliminarCuenta(request):
    from django.core.exceptions import ObjectDoesNotExist
    try:
        identificador = request.user.identificador
        user = Usuario.objects.get(identificador=identificador)
        logout(request)
        user.delete()
    except ObjectDoesNotExist:
        print("Usuario no existe")

    return redirect('home')

    