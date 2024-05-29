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


class LoginSerializer(serializers.Serializer):
        mail = serializers.CharField()
        password = serializers.CharField()
        
class LikeSerializer(serializers.ModelSerializer):
    perfil = serializers.SlugRelatedField(slug_field='id', read_only=True)

    class Meta:
        model = Like
        fields = ['perfil']

        
class ComentarioSerializer(serializers.ModelSerializer):
    perfil_info = serializers.SerializerMethodField()

    class Meta:
        model = Comentario
        fields = ['id','texto', 'fecha_creacion', 'perfil_info']

    def get_perfil_info(self, obj):
        if obj.perfil:
            # Suponiendo que el modelo Perfil tiene atributos fotoPerfil y nombreMascota
            return {
                "fotoPerfil": obj.perfil.fotoPerfil.url if obj.perfil.fotoPerfil else None, 
                "nombreMascota": obj.perfil.mascota.nombre if obj.perfil.mascota else None
            }
        return None
class ImagenSerializer(serializers.ModelSerializer):
    class Meta:
        model = Imagen
        fields = ['urlImagen', 'descripcionImagen']

class PublicacionSerializer(serializers.ModelSerializer):
    imagenes = ImagenSerializer(many=True, read_only=True)
    comentarios = ComentarioSerializer(many=True, read_only=True)
    likesPerfiles = serializers.SerializerMethodField()

    class Meta:
        model = Publicacion
        fields = ['id', 'descripcion', 'fechaPublicacion','likesPerfiles','likes', 'imagenes', 'comentarios']
        
    def get_likesPerfiles(self, obj):
        return [like.perfil.id for like in obj.like_set.all()]
       
class PerfilSerializer(serializers.ModelSerializer):
    publicaciones = PublicacionSerializer(many=True, read_only=True)
    total_seguidores = serializers.ReadOnlyField()
    total_siguiendo = serializers.ReadOnlyField()

    class Meta:
        model = Perfil
        fields = ['id','fotoPerfil', 'totalPublicaciones', 'siguiendo', 'seguidores', 'total_seguidores', 'total_siguiendo', 'publicaciones']
class MascotaSerializer(serializers.ModelSerializer):
    perfil = PerfilSerializer(read_only=True)

    class Meta:
        model = Mascota
        fields = ['id', 'nombre', 'descripcion', 'perfil']
        
