from django.shortcuts import redirect, render
from django.contrib import messages
from django.urls import reverse  # Add this import
from django.core.mail import send_mail  # Add this import
from Aplicaciones.forms.formularioLogin import mailForm , passwordResetForm
from Aplicaciones.bbdd.models import Usuario
from django.contrib.auth.tokens import default_token_generator  # Add this import
from django.utils.http import urlsafe_base64_encode  # Add this import
from django.utils.encoding import force_bytes  # Add this import
from django.template.loader import render_to_string  # Add this import
from django.utils.http import urlsafe_base64_decode

def restablecer_pass(request):
    if request.method == 'POST':
        form = mailForm(request.POST)
        if form.is_valid():
            email = form.cleaned_data['mail']
            
            try:
                user = Usuario.objects.get(mail=email)
            except Usuario.DoesNotExist:
                user = None
            if user is not None:
                token = default_token_generator.make_token(user)
                uid = urlsafe_base64_encode(force_bytes(user.pk))
                context = {
                    'nombreUsuario': user.nombreUsuario,
                    'mail': email,
                    'domain': request.get_host(),
                    'protocol': 'https' if request.is_secure() else 'http',
                    'uid': uid,
                    'token': token,
                }

                email_body = render_to_string('pass_reset_mail.html', context)
                
                send_mail(
                    'Instrucciones para restablecer tu contraseña',
                    email_body,
                    'PawSwipeoficial@outlook.com',
                    [email],
                    fail_silently=False,
                )
                return redirect(reverse('password_reset_done'))
            else:
                # Manejo del caso en que no se encuentra el usuario
                messages.error(request, 'No hay usuario con ese correo electrónico.')
    else:
        form = mailForm()

    return render(request, 'pass_reset.html', {'mailForm': form})


def password_reset_done(request):
    return render(request, 'pass_reset_done.html')

def password_reset_mail(request):
    return render(request, 'pass_reset_mail.html')

def password_reset_confirm(request, uidb64, token):
    try:
        uid = urlsafe_base64_decode(uidb64).decode()
        user = Usuario.objects.get(pk=uid)
    except (TypeError, ValueError, OverflowError, Usuario.DoesNotExist):
        user = None

    if user is not None and default_token_generator.check_token(user, token):
        if request.method == 'POST':
            form = passwordResetForm(request.POST)
            if form.is_valid():
                password = form.cleaned_data['password1']
                user.set_password(password)
                user.save()
                return redirect(reverse('pass_reset_complete'))  # Asegúrate de que 'login' es el nombre correcto de la URL para iniciar sesión
        else:
            form = passwordResetForm()
        return render(request, 'pass_reset_confirm.html', {'form': form, 'validlink': True})
    else:
        return render(request, 'pass_reset_confirm.html', {'error': 'El enlace de restablecimiento es inválido o ha caducado'})

def password_reset_complete(request):
    return render(request, 'pass_reset_completado.html')