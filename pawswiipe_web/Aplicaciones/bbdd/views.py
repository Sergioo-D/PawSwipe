from django.shortcuts import render, redirect, get_object_or_404
from django.http import HttpResponse
from Aplicaciones.bbdd.models import Usuario, RegistroInicioSession
from Aplicaciones.forms.formulario import UsuarioForm
from Aplicaciones.forms.formularioLogin import LoginForm
from django.contrib import messages
from django.contrib.auth.hashers import make_password
from django.contrib.auth import  login, logout, authenticate
from .decorators import redirigirUsuarios , validar_contrasena
from django.contrib.auth.decorators import login_required
from django.contrib.auth import update_session_auth_hash


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