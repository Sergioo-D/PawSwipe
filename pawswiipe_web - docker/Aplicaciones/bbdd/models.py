
from django.db import models
from django.contrib.auth.hashers import check_password
from django.contrib.auth.models import BaseUserManager, AbstractBaseUser, PermissionsMixin, Group
from django.shortcuts import get_object_or_404
from django.contrib import admin
from django.db.models import Max
import uuid
from django.utils.text import slugify

from django.urls import reverse



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


    
class MensajeDirecto(models.Model):
    user = models.ForeignKey(Usuario, on_delete=models.CASCADE, related_name="user")
    emisor = models.ForeignKey(Usuario, on_delete=models.CASCADE, related_name="emisor")
    receptor = models.ForeignKey(Usuario, on_delete=models.CASCADE, related_name="receptor")
    timestamp = models.DateTimeField(auto_now_add=True)
    mensaje = models.TextField()
    is_read = models.BooleanField(default=False)

    def sendMessage(user,emisor,receptor,mensaje):
        sender_message = MensajeDirecto(user=emisor,emisor=emisor, receptor=receptor, mensaje=mensaje, is_read=True)
        sender_message.save()

        receiver_message = MensajeDirecto(user=receptor,emisor=emisor, receptor=receptor, mensaje=mensaje, is_read=False)
        receiver_message.save()

        return receiver_message, sender_message

    def getMessages(user):
        users = []
        msgs = MensajeDirecto.objects.filter(user=user).values('receptor', 'emisor', 'mensaje').annotate(last=Max('timestamp')).order_by('-last')
        for msg in msgs:
            print(msg)
            message = MensajeDirecto.objects.filter(user=user, receptor__pk=msg['receptor']).latest('timestamp')
            users.append({
                'user': Usuario.objects.get(pk=msg['receptor']),
                'last': msg['last'],
                'unread': MensajeDirecto.objects.filter(user=user, receptor__pk=msg['receptor'], is_read=False).count(),
                'mensaje': message.mensaje
            })
        return users

    class Meta:
        db_table = "mensajeDirecto"


def user_directory_path(instance, filename):
    return 'user_{0}/{1}'.format(instance.user.id, filename)


class Sala(models.Model):
    nombre = models.CharField(max_length = 50)
    slug = models.SlugField(null = False , unique = True)
    emisor = models.ForeignKey(Usuario, on_delete=models.CASCADE, related_name="sala_emisor")
    receptor = models.ForeignKey(Usuario, on_delete=models.CASCADE, related_name="sala_receptor")

    def save(self, *args, **kwargs):
        if not self.slug:
            self.slug = slugify(self.nombre)
        super().save(*args, **kwargs)

    def _str_(self):
        return self.nombre    

""" class Tag(models.Model):
    title = models.CharField(max_length=75, verbose_name="Título")
    slug = models.SlugField(null = False , unique = True, default = uuid.uuid4)

    class Meta:
        verbose_name = "Tag"
        verbose_name_plural = "Tags"

    def get_absolute_url(self):
        return reverse('tags', args=[self.slug])
    
    def __str__(self):
        return self.title
    
    def save (self, *args, **kwargs):
        if not self.slug:
            self.slug = slugify(self.title)
            return  super().save(*args, **kwargs)

class Post(models.Model):
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    foto = models.ImageField(upload_to = user_directory_path, verbose_name="Foto")
    caption = models.CharField(max_length=10000, verbose_name="Caption")
    posted = models.DateTimeField(auto_now_add=True, verbose_name="Fecha de publicación")
    tags = models.ManyToManyField(Tag, verbose_name="Tags")
    usuario = models.ForeignKey(Usuario, on_delete=models.CASCADE)
    likes = models.IntegerField(default=0)

    def get_absolute_url(self):
        return reverse('post-detail', args=[str(self.id)])  """ 



class perfil(models.Model):
    usuario = models.OneToOneField(Usuario, on_delete=models.CASCADE)
    fotoPerfil = models.ImageField(upload_to = user_directory_path, verbose_name="Foto de perfil")

    def __str__(self):
        return self.usuario.mail

      