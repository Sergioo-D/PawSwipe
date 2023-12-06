from django.shortcuts import redirect
from .models import Usuario


def redirigirUsuarios(function):
    def comprobarUsuario(request, *args, **kwargs):
        if request.user.is_authenticated:
            return redirect('feed')
        else:
            return function(request, *args, **kwargs)
    return comprobarUsuario