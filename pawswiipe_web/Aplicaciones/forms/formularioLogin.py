from django import forms
from django.contrib.auth.forms import AuthenticationForm


class LoginForm(AuthenticationForm):
    nombreUsuario = forms.CharField(widget=forms.TextInput(attrs={'class': 'form-control'}))
    password = forms.CharField(widget=forms.PasswordInput(attrs={'class': 'form-control'}))

class mailForm(forms.Form):
    mail = forms.EmailField(widget=forms.EmailInput(attrs={'class': 'form-control'}), label='Correo electrónico')
    help_texts = {
        'mail': 'Formato:'
    } 

class passwordResetForm(forms.Form):
    password1 = forms.CharField(widget=forms.PasswordInput(attrs={'class': 'form-control'}), label='Nueva contraseña')
    password2 = forms.CharField(widget=forms.PasswordInput(attrs={'class': 'form-control'}), label='Repetir contraseña')

    help_texts = {
        'password1': 'Tu contraseña debe tener al menos 8 caracteres.',
        'password2': 'Ingresa la misma contraseña que antes, para verificar que la has escrito correctamente.'
    }
    def clean(self):
        cleaned_data = super().clean()
        password1 = cleaned_data.get('password1')
        password2 = cleaned_data.get('password2')

        if password1 != password2:
            raise forms.ValidationError('Las contraseñas no coinciden.')
        return cleaned_data       
