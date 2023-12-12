from rest_framework import serializers
from .models import *

class UsuarioSerializer(serializers.ModelSerializer):
    class Meta:
        model = Usuario
        fields = '__all__'

class RegistroInicioSessionSerializer(serializers.ModelSerializer):
    class Meta:
        model = RegistroInicioSession
        fields = '__all__'
