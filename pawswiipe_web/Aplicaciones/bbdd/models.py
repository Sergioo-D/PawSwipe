
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
    nombreUsuario = models.CharField(max_length=40, unique=True)
    mail = models.EmailField(unique=True,primary_key=True)
    nombreReal = models.CharField(max_length=50)
    fecha = models.DateField(auto_now_add=True)
    last_login = models.DateTimeField(auto_now=True)
    is_staff = models.BooleanField(default=False)
    is_active = models.BooleanField(default=True)

    
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
        return f'{self.mail}'

class UsuarioAdmin(admin.ModelAdmin):
    fields = [ 'nombreUsuario', 'password', 'mail', 'nombreReal','is_active', 'is_staff']
   # exclude = ("fecha", "last_login", "is_staff", "is_superuser")
# Desregistrar el modelo si ya está registrado
    try:
        admin.site.unregister(Usuario)
    except admin.sites.NotRegistered:
        pass

# Registrar el modelo
    
class RegistroInicioSession(models.Model):
    mail = models.ForeignKey(Usuario, on_delete=models.CASCADE)
    timestamp = models.DateTimeField(auto_now_add=True)
    login_exitoso = models.BooleanField(default=False)
    sistema = models.CharField(max_length=255, blank=True)

    class Meta:
        db_table = "registroLogin"

    def __str__(self):
        return f'{self.mail} {self.timestamp} {self.login_exitoso} {self.sistema}'


    
 