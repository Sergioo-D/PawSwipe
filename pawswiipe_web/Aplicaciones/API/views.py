import os
from django.db.models import Exists, OuterRef
from django.utils import timezone
from django.conf import settings
from django.forms import ImageField
from django.db.models import F, Case, When, Value, CharField, ImageField
from django.shortcuts import render, redirect, get_object_or_404
from django.http import HttpResponse, JsonResponse
from requests import Response
from Aplicaciones.bbdd.models import *
from django.contrib.auth.hashers import make_password
from django.contrib.auth import  login, logout, authenticate
from rest_framework import status
from rest_framework.decorators import api_view,  permission_classes
from Aplicaciones.bbdd.serializers import ComentarioSerializer, MascotaSerializer, PublicacionSerializer, UsuarioSerializer , LoginSerializer
from rest_framework.response import Response
from rest_framework.authtoken.models import Token
from django.db.models import Q
from django.core.exceptions import ObjectDoesNotExist
from rest_framework.permissions import IsAuthenticated
import base64
from django.core.files.storage import default_storage
from django.core.files.base import ContentFile
from PIL import Image
from io import BytesIO
import emoji
from django.core.paginator import Paginator


def crear_perfil_default(mascota):
    try:
        Perfil.objects.create(
            mascota=mascota, 
            fotoPerfil=""  
        )
    except Exception as e:
        print(f"No se pudo crear el perfil predeterminado para {mascota.nombre}: {e}")
        raise Exception(f"Error al crear el perfil predeterminado: {e}")

@api_view(['POST'])
def registrar_usuario(request):
    if request.method == 'POST':
        serializer = UsuarioSerializer(data=request.data)
        if serializer.is_valid():
            email = serializer.validated_data.get('mail')
            
            if Usuario.objects.filter(mail=email).exists():
                return Response({'message': 0,'error': 'Este email ya está registrado'},status=status.HTTP_226_IM_USED)

            password = serializer.validated_data.get('password')
            encriptar = make_password(password)
            serializer.validated_data['password'] = encriptar
            user = serializer.save()

            respuesta_data = serializer.data
            respuesta_data["message"] = 1
            return Response(respuesta_data, status=status.HTTP_201_CREATED)

        # Retornar errores del serializador si no es válido
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
@api_view(['POST'])
@permission_classes([IsAuthenticated])
def registrar_mascota(request):
    usuario = request.user  # El usuario ya está autenticado por el token
    if request.method == 'POST':
        serializer = MascotaSerializer(data=request.data)
        if serializer.is_valid():
            nombre = serializer.validated_data['nombre']
            if Mascota.objects.filter(nombre=nombre, usuario=usuario).exists():
                return Response({'error': 'El nombre de la mascota ya existe', 'message': 0}, status=status.HTTP_400_BAD_REQUEST)
            else:
                # Guarda la nueva mascota con el usuario autenticado
                nueva_mascota = serializer.save(usuario=usuario)
                try:
                    # Crea un perfil predeterminado para la nueva mascota
                    crear_perfil_default(nueva_mascota)
                except Exception as e:
                    return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

                return Response({'message': '1'}, status=status.HTTP_201_CREATED)
        else:
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    

@api_view(['POST'])
def login_usuario(request):
   
    if request.method == 'POST':
        serializer = LoginSerializer(data=request.data)
        print("Datos de la solicitud:", request.data)
        if serializer.is_valid():
            mail = serializer.validated_data.get('mail')
            password = serializer.validated_data.get('password')
            user = authenticate(request, username=mail, password=password)

            if user is not None:
                token, created = Token.objects.get_or_create(user=user)
                if user.mascotas.exists():
                    return Response({
                        'token': token.key,
                        'message': '1',
                        'has_mascotas': True
                    }, status=status.HTTP_200_OK)
                else:
                    return Response({
                        'token': token.key,
                        'message': '1',
                        'has_mascotas': False
                    }, status=status.HTTP_200_OK)
            else:
                return Response({'error': 'Usuario o contraseña incorrectos','message': '0'})
        else:
            print("Datos de la solicitud no válidos:", serializer.errors)
            return Response({'error': 'Datos de la solicitud no válidos', 'message': serializer.errors}, status=status.HTTP_400_BAD_REQUEST)

@api_view(['POST'])
def cerrar_sesion(request):
    print("hola")
    print(request.headers)
    try:
        request.user.auth_token.delete()
        return Response({'message': '1'}, status=status.HTTP_200_OK)
    except:
        return Response({'error': 'No se encontró token válido'}, status=status.HTTP_400_BAD_REQUEST)


@api_view(['POST'])
def obtener_datos_usuario(request):
    mail = request.data.get('email')
    print(mail)
    try:
        user = Usuario.objects.get(mail=mail)
        try: 
            perfil_usuario = perfil.objects.get(usuario=user)
            image_url = perfil_usuario.fotoPerfil
            with open(image_url, 'rb') as f:
                image_bytes = f.read()
                image_base64 = base64.b64encode(image_bytes).decode('utf-8')
        except ObjectDoesNotExist:
            return Response({'message': 'Perfil no encontrado'}, status=404)
        return Response({
            'username': user.nombreUsuario,
            'fullname': user.nombreReal,
            'email': user.mail,
            'fotoPerfil': image_base64,
            'message': '1'
        })
    except ObjectDoesNotExist:
        return Response({'message': 'Usuario no encontrado'}, status=404)


@api_view(['POST'])
def borrar_usuario(request):
    email = request.data.get('email')
    try:
        user = Usuario.objects.get(mail=email)
        user.delete()
        return Response({'message': '1'})
    except ObjectDoesNotExist:
        return Response({'message': 'Usuario no encontrado'}, status=404)


@api_view(['POST'])
def modificar_usuario(request):
    email = request.data.get('email')
    username = request.data.get('username')
    fullname = request.data.get('fullname')
    foto_base64 = request.data.get('fotoPerfil') # Obtén la imagen de perfil del request
    # foto_bytes = base64.b64decode(foto_base64)
    print(email, username, fullname)
    # print("foto de perfil:", foto_base64)
    try:    
        user = Usuario.objects.get(mail=email)

        # Solo actualiza los campos si se proporcionan
        if username:
            user.nombreUsuario = username
        if fullname:
            user.nombreReal = fullname

        user.save()
    
        #  Actualiza la imagen de perfil
        if foto_base64:
            foto_bytes = base64.b64decode(foto_base64)
            image_dir = os.path.join(settings.MEDIA_ROOT, 'perfil_images')
            os.makedirs(image_dir, exist_ok=True)  # Crea la carpeta si no existe
            image_path = os.path.join(image_dir, f'{user.mail}.jpg')
            with open(image_path, 'wb') as f:
                f.write(foto_bytes)

            # Guarda la URL de la imagen en la base de datos
            imagen_url = os.path.join(settings.MEDIA_ROOT, 'perfil_images', f'{user.mail}.jpg')
            try:
                perfil_usuario = Perfil.objects.get(usuario=user)
                perfil_usuario.fotoPerfil = imagen_url
                perfil_usuario.save()
            # Decodificar el string base64 y crear un objeto Image
            # Guardar la imagen en el campo fotoPerfil
            except Perfil.DoesNotExist:
                perfil_usuario = Perfil.objects.create(usuario=user, fotoPerfil=imagen_url)
        return Response({'message': '1'})
    except ObjectDoesNotExist as e:
        # return Response({'message': str(e)}, status=404)
        return Response({'message': 'Usuario no encontrado'}, status=404)



@api_view(['POST'])
def bloquear_cuenta(request):
    email = request.data.get('email')
    print(email)
    try:
        user = Usuario.objects.get(mail=email)
        user.is_active = False
        user.save()
        return Response({'message': '1'})
    except ObjectDoesNotExist:
        return Response({'message': 'Usuario no encontrado'}, status=404)
    
@api_view(['GET'])
@permission_classes([IsAuthenticated])
def perfil_completo(request):
    usuario = request.user
    mascota_id = request.query_params.get('mascota_id')

    if mascota_id is None:
        mascota_actual = Mascota.objects.filter(usuario=usuario).first()
    else:
        mascota_actual = Mascota.objects.filter(id=mascota_id).first()

    todas_las_mascotas = Mascota.objects.filter(usuario=usuario).exclude(id=mascota_actual.id)
    mascota_actual_serializer = MascotaSerializer(mascota_actual)
    todas_las_mascotas_serializer = MascotaSerializer(todas_las_mascotas, many=True)
    
    print(mascota_actual_serializer.data)
    
    return Response({
        'mascota_actual': mascota_actual_serializer.data,
        'todas_las_mascotas': todas_las_mascotas_serializer.data
    })
    
    
@api_view(['POST'])
@permission_classes([IsAuthenticated])
def perfil_completo_visitado(request):
    data = request.data
    mascota_id = data.get('id')
    
    if mascota_id:
        mascota_actual = Mascota.objects.get(id=mascota_id)
  
    usuario = get_object_or_404(Usuario, pk=mascota_actual.usuario.mail)
    todas_las_mascotas = Mascota.objects.filter(usuario=usuario).exclude(id=mascota_actual.id)
    mascota_actual_serializer = MascotaSerializer(mascota_actual)
    todas_las_mascotas_serializer = MascotaSerializer(todas_las_mascotas, many=True)
    
    print(mascota_actual_serializer.data)
    
    return Response({
        'mascota_actual': mascota_actual_serializer.data,
        'todas_las_mascotas': todas_las_mascotas_serializer.data
    })
    
@api_view(['POST'])
@permission_classes([IsAuthenticated])
def insert_comentario(request):
    print(request.body)
    usuario = request.user
    idMascota = request.data['idMascota']
    idPublicacion = request.data['idPublicacion']
    texto = request.data['texto']
    perfil = get_object_or_404(Perfil, mascota_id=idMascota)
    publicacion = get_object_or_404(Publicacion, pk=idPublicacion)
    
    if not texto:
        return JsonResponse({'error': 'El texto del comentario no puede estar vacío'}, status=400)
    

    try:
        publicacion = get_object_or_404(Publicacion, id=idPublicacion)
        nuevo_comentario = Comentario.objects.create(publicacion=publicacion, perfil=perfil, texto=convertir_emoji(texto))
        return JsonResponse({
            'nuevoComentario': {
                'autor': perfil.mascota.nombre,
                'texto': texto, 
                'fotoPerfil': perfil.fotoPerfil.url if perfil.fotoPerfil else None,
            }
            }, status=201)
    except Exception as e:
        return JsonResponse({'error': str(e)}, status=500)
    
    
@api_view(['GET'])
@permission_classes([IsAuthenticated])
def comments(request, publicacion_id):
    publicacion = get_object_or_404(Publicacion, id=publicacion_id)
    comentarios = Comentario.objects.filter(publicacion=publicacion).select_related('perfil')

    # Se utiliza el ComentarioSerializer para serializar los comentarios
    comentarios_serializer = ComentarioSerializer(comentarios, many=True, context={'request': request})

    return Response({'comentarios': comentarios_serializer.data})

@api_view(['POST'])
@permission_classes([IsAuthenticated])
def actualizar_mascota(request):
    if request.method == 'POST':
        data = request.data
        mascota_id = data['id']
        nombre = data['nombre']
        descripcion = data['descripcion']
        foto = data.get('fotoPerfil')
        print(foto)

        mascota = get_object_or_404(Mascota, id=mascota_id)
        mascota.nombre = nombre
        mascota.descripcion = convertir_emoji(descripcion)
        mascota.save()  # Guardar cambios en la mascota

        perfil = get_object_or_404(Perfil, mascota_id=mascota_id)
        
        if foto is not None:
            try:
                # Dividir los datos de la imagen y decodificar la parte de base64
                format, imgstr = foto.split(';base64,')
                ext = format.split('/')[-1]  # Extrae la extensión del archivo

                # Crear ContentFile
                image_data = base64.b64decode(imgstr)
                image_name = f'mascota_{mascota_id}.{ext}'
                image_file = ContentFile(image_data, name=image_name)

                # Asignar el archivo de imagen al ImageField y guardar
                perfil.fotoPerfil.save(image_name, image_file)
                perfil.save()
            except Exception as e:
                return JsonResponse({'error': f'Error al guardar la imagen: {str(e)}'}, status=500)

        # Devolver datos actualizados
        perfil_data = {
            'id': mascota.id,
            'nombre': mascota.nombre,
            'descripcion': recuperar_emoji(mascota.descripcion),
            'fotoPerfil': perfil.fotoPerfil.url if perfil.fotoPerfil else None,
        }
        return JsonResponse({'message': '1', 'perfil': perfil_data}, status=200)

    return JsonResponse({'error': 'Método no permitido'}, status=405)


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def delete_mascota(request):
    data = request.data
    mascota_id = data.get('id')
    usuario = request.user
    print(mascota_id)

    mascota = get_object_or_404(Mascota, id=mascota_id)

    mascota.delete()

    # Obtener la nueva mascota actual
    mascota_actual = Mascota.objects.filter(usuario=usuario).first()

    if mascota_actual:
        todas_las_mascotas = Mascota.objects.filter(usuario=usuario).exclude(id=mascota_actual.id)
    else:
        todas_las_mascotas = Mascota.objects.filter(usuario=usuario)

    mascota_actual_serializer = MascotaSerializer(mascota_actual)
    todas_las_mascotas_serializer = MascotaSerializer(todas_las_mascotas, many=True)
    
    perfil_data = {
        'mascota_actual': mascota_actual_serializer.data if mascota_actual else None,
        'todas_las_mascotas': todas_las_mascotas_serializer.data
     }

    return JsonResponse({'message': '1', 'perfil': perfil_data}, status=200)

@api_view(['POST'])
@permission_classes([IsAuthenticated])
def eliminar_publicacion (request):
    data = request.data
    usuario = request.user
    publicacionId = data.get('id')
    publicacion = get_object_or_404(Publicacion, pk=publicacionId)
    perfilId = publicacion.perfil.id
    perfil = get_object_or_404(Perfil, pk=perfilId)
    perfil.totalPublicaciones -= 1
    perfil.save()
    publicacion.delete()
    
    return Response({'message': '1'}, status=200)


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def eliminar_comentario (request):
    data = request.data
    usuario = request.user
    comentarioId = data.get('id')
    comentario = get_object_or_404(Comentario, pk=comentarioId)
    comentario.delete()
    
    return Response({'message': '1'}, status=200)

@api_view(['POST'])
@permission_classes([IsAuthenticated])
def crear_publicacion(request):
    if request.method == 'POST':
        idMascota = request.data.get('idMascota')
        descripcion = request.data['descripcion']
        imagenes = request.data.get('imagenes')
        perfil = get_object_or_404(Perfil, mascota_id=idMascota)
        
        if imagenes:
            publicacion = Publicacion(perfil=perfil, descripcion=convertir_emoji(descripcion), fechaPublicacion=timezone.now(), likes=0)
            publicacion.save()
            perfil.totalPublicaciones += 1
            perfil.save()
            
            for image in imagenes:
                try:
                    print(image[:10])  # Imprimir los primeros 10 caracteres de la cadena base64
                    format, imgstr = image.split(';base64,')
                    ext = format.split('/')[-1]  # Extrae la extensión del archivo
                    
                    # Crear ContentFile
                    image_data = base64.b64decode(imgstr)
                    image_name = f"{uuid.uuid4()}.{ext}"  # Nombre de archivo único
                    image_file = ContentFile(image_data, name=image_name)
                    
                    # Crear instancia de Imagen y guardar el archivo
                    imagen_obj = Imagen(publicacion=publicacion)
                    imagen_obj.urlImagen.save(image_name, image_file, save=True)
                    
                    # Guardar la imagen en la base de datos
                    imagen_obj.save()
                except Exception as e:
                    print(f'Error al procesar la imagen: {str(e)}')
                    return Response({'message': f'Error al procesar la imagen: {str(e)}'}, status=400)

        return Response({'message': '1'}, status=200)
    
    return Response({'message': 'Método no permitido.'}, status=405)


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def search_perfiles(request):
    query = request.GET.get('q', '')
    if query:
        # Filtra las mascotas que no pertenecen al usuario y que contienen el query en el nombre.
        mascotas = Mascota.objects.filter(nombre__icontains=query).exclude(usuario=request.user).select_related('perfil')
        resultados = [
            {
                'id': mascota.id,
                'nombre': mascota.nombre,
                'foto_url': mascota.perfil.fotoPerfil.url if mascota.perfil.fotoPerfil else None,
                'perfil_url': reverse('perfil-mascota', args=[mascota.id])
            }
            for mascota in mascotas
        ]
    else:
        resultados = []

    return JsonResponse({'resultados': resultados})


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def dar_like(request):
    idMascota = request.data.get('idMascota')
    publicacion_id = request.data.get("idPublicacion")
    publicacion = get_object_or_404(Publicacion, id=publicacion_id)
    perfil = get_object_or_404(Perfil, mascota_id=idMascota)

    try:
        like = Like.objects.get(perfil=perfil, publicacion=publicacion)
        like.delete()
        publicacion.likes -= 1
        publicacion.save()
        return Response({'message': 'Like removed', 'likes': publicacion.likes})
    except Like.DoesNotExist:
        Like.objects.create(perfil=perfil, publicacion=publicacion)
        publicacion.likes += 1
        publicacion.save()
        return Response({'message': 'Like added', 'likes': publicacion.likes})

    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
    
    
@api_view(['POST'])
@permission_classes([IsAuthenticated])
def follow_perfil(request):
    mascota_actual_id = request.data.get('idMascota')
    perfil_id = request.data.get('perfilId')
    print(perfil_id)
    if not mascota_actual_id:
        return Response({'error': 'No hay una mascota seleccionada en la sesión'}, status=400)

    perfil_a_seguir = get_object_or_404(Perfil, id=perfil_id)
    perfil_usuario = get_object_or_404(Perfil, mascota__id=mascota_actual_id)
    print(perfil_usuario.id)
    
    if perfil_usuario == perfil_a_seguir:
        return Response({'error': 'No puedes seguirte a ti mismo'}, status=400)
    try:
    # Comprobar si ya está siguiendo al perfil
        if perfil_a_seguir in perfil_usuario.siguiendo.all():
            perfil_usuario.siguiendo.remove(perfil_a_seguir)
            numSeguidores = perfil_a_seguir.seguidores.count()
            return Response({'message': 'Dejar de seguir', 'num_seguidores': numSeguidores})
        else:
            perfil_usuario.siguiendo.add(perfil_a_seguir)
            numSeguidores = perfil_a_seguir.seguidores.count()
            return Response({'message': 'Seguir', 'num_seguidores': numSeguidores})
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
    
    
    
@api_view(['POST'])
@permission_classes([IsAuthenticated])
def muro(request):
    user = request.user
    mascota_actual_id = request.data.get("idMascota")
    mascota_actual = Mascota.objects.get(id=mascota_actual_id) if mascota_actual_id else user.mascotas.first()

    perfil_mascota_actual = mascota_actual.perfil
    followed_profiles = perfil_mascota_actual.siguiendo.all()

    followed_publications = Publicacion.objects.filter(perfil__in=followed_profiles)
    other_publications = Publicacion.objects.exclude(perfil__in=followed_profiles)
    all_publications = (followed_publications | other_publications).order_by('-fechaPublicacion')

    # Anotar publicaciones con información de "like" del usuario actual
    all_publications = all_publications.annotate(
        has_user_liked=Exists(Like.objects.filter(
            publicacion_id=OuterRef('id'),
            perfil=perfil_mascota_actual
        )),
        nombreMascota= F('perfil__mascota__nombre'),  # Anota el nombre de la mascota directamente
        fotoPerfil=Case(
            When(perfil__fotoPerfil__isnull=False, then=F('perfil__fotoPerfil')),
            default=Value(None),
            output_field=ImageField()
        )
    )

    paginator = Paginator(all_publications, 10)  # Paginación para la respuesta de la API
    page_number = request.query_params.get('page')
    publicaciones = paginator.get_page(page_number)

    # Serializar la lista de publicaciones
    serializer = PublicacionSerializer(publicaciones, many=True, context={'request': request})
    print(serializer.data)

    return Response({
        'publicaciones': serializer.data,
        'has_next': publicaciones.has_next()
    })


    
    
def contains_emoji(text):
    return any(char in emoji.EMOJI_DATA for char in text)

def convertir_emoji(texto):
    texto = emoji.demojize(texto)
    return texto
def recuperar_emoji(texto):
    texto = emoji.emojize(texto)
    return texto