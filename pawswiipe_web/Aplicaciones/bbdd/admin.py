from django.contrib import admin


from django.contrib import admin

from Aplicaciones.bbdd.models import Usuario, UsuarioAdmin, MensajeDirecto,Sala


admin.site.register(Usuario, UsuarioAdmin)
admin.site.register(MensajeDirecto)
admin.site.register(Sala)

#eladmin@gmail.com
#pass = 123456

# Register your models here.





