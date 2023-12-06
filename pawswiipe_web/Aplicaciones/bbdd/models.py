
from django.db import models
from django.contrib.auth.hashers import check_password
from django.contrib.auth.models import BaseUserManager, AbstractBaseUser, PermissionsMixin, Group
from django.shortcuts import get_object_or_404
from django.contrib import admin



class CustomUserManager(BaseUserManager):
    def create_user(self, mail, password=None, **extra_fields):
        if not mail:
            raise ValueError('The Email field must be set')
        mail = self.normalize_email(mail)
        user = self.model(mail=mail, **extra_fields)
        user.set_password(password)
        user.save(using=self._db)
        return user

    def create_superuser(self, mail, password=None, **extra_fields):
        extra_fields.setdefault('is_staff', True)
        extra_fields.setdefault('is_superuser', True)
        return self.create_user(mail, password, **extra_fields)

class Usuario(AbstractBaseUser, PermissionsMixin):
    identificador = models.AutoField(primary_key=True)
    nombreUsuario = models.CharField(max_length=40, unique=True)
    mail = models.EmailField(unique=True)
    nombreReal = models.CharField(max_length=50)
    fecha = models.DateField(auto_now_add=True)
    last_login = models.DateTimeField(auto_now=True)
    is_staff = models.BooleanField(default=False)

    # Agregar related_name a los campos groups y user_permissions
    groups = models.ManyToManyField(
        'auth.Group',
        blank=True,
        help_text='The groups this user belongs to. A user will get all permissions granted to each of their groups.',
        related_name="usuario_groups",
        related_query_name="usuario",
        verbose_name='groups'
    )
    user_permissions = models.ManyToManyField(
        'auth.Permission',
        blank=True,
        help_text='Specific permissions for this user.',
        related_name="usuario_user_permissions",
        related_query_name="usuario",
        verbose_name='user permissions'
    )

    objects = CustomUserManager()

    USERNAME_FIELD = 'mail'
    REQUIRED_FIELDS = ['nombreUsuario', 'nombreReal']

    class Meta:
        db_table = "usuario"

    def __str__(self):
        return f'{self.mail} {self.identificador}'

class UsuarioAdmin(admin.ModelAdmin):
    fields = ['identificador', 'nombreUsuario', 'password', 'mail', 'nombreReal', 'fecha', 'last_login', 'is_staff', 'is_superuser']
   # exclude = ("fecha", "last_login", "is_staff", "is_superuser")
# Desregistrar el modelo si ya está registrado
    try:
        admin.site.unregister(Usuario)
    except admin.sites.NotRegistered:
        pass

# Registrar el modelo
    

""" def autentificador(mail, password):
    try:
        user = get_object_or_404(Usuario, mail=mail)
        if check_password(password, user.password):
            return user
        else:
            return None
    except Usuario.DoesNotExist:
        return None """
    
 