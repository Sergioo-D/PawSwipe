from django import forms

from Aplicaciones.bbdd.models import Imagen, Mascota, Publicacion, Usuario


class UsuarioForm(forms.ModelForm):
    class Meta:
        model = Usuario
        fields = ['nombreUsuario', 'password', 'nombreReal', 'mail']
        widgets = {
            'nombreUsuario': forms.TextInput(attrs={'class': 'form-control'}),
            'password': forms.PasswordInput(attrs={'class': 'form-control'}),
            'nombreReal': forms.TextInput(attrs={'class': 'form-control'}),
            'mail': forms.EmailInput(attrs={'class': 'form-control'}),
            'fecha': forms.DateInput(attrs={'class': 'form-control'}),
        }
        labels = {
            'nombreUsuario': 'Nombre de usuario',
            'password': 'Contraseña',
            'nombreReal': 'Nombre real',
            'mail': 'Correo electrónico',
            'fecha': 'Fecha de registro',
        }
        help_texts = {
            'nombreUsuario': 'Máximo 40 caracteres',
            'password': 'Máximo 255 caracteres',
            'nombreReal': 'Máximo 50 caracteres',
            'mail': 'Formato:'}
        

class MascotaForm(forms.ModelForm):
    class Meta:
        model = Mascota
        fields = ['nombre', 'descripcion']



class PublicacionForm(forms.ModelForm):
    class Meta:
        model = Publicacion
        fields = ['descripcion']

class ImagenForm(forms.ModelForm):
    class Meta:
        model = Imagen
        fields = ['urlImagen', 'descripcionImagen']