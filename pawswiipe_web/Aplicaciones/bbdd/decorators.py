from django.shortcuts import redirect
from .models import Usuario
import re


def redirigirUsuarios(function):
    def comprobarUsuario(request, *args, **kwargs):
        if request.user.is_authenticated:
            return redirect('feed')
        else:
            return function(request, *args, **kwargs)
    return comprobarUsuario

def validar_contrasena(password):
    if re.search(r'[A-Z]', password) is None:  # Verifica si hay al menos una letra mayúscula
        return False
    if re.search(r'[a-z]', password) is None:  # Verifica si hay al menos una letra minúscula
        return False
    if re.search(r'\d', password) is None:  # Verifica si hay al menos un número
        return False
    if re.search(r'\W', password) is None:  # Verifica si hay al menos un carácter especial
        return False
    return True