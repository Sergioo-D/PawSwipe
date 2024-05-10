let currentImageIndex = 0;
let images = [];
// Para abrir el modal
// function openModalDelete() {
//     document.getElementById('deleteModal').style.display = 'flex';
// }

function abrirModal(id) {
    document.getElementById(id).style.display = 'flex';
}

function openDeleteModal(publicacion_id) {
    var modal = document.getElementById('deleteModal');
    var form = modal.querySelector('#deleteForm');
    form.action = `/delete_post/${publicacion_id}`;
    var publicacionIdInput = modal.querySelector('#publicacionId');
    publicacionIdInput.value = publicacion_id;
    modal.style.display = "flex";
}

function cambiarPerfil(mascotaId) {
    var container = document.querySelector('.mascota-miniatura-container');
    var urlBase = container.getAttribute('data-url-base');
    var urlGuardarMascota = urlBase.replace('9999', mascotaId);

    fetch(urlGuardarMascota, {
        method: 'GET', // O POST, asegúrate de manejar CSRF si es necesario
    })
        .then(response => {
            if (response.ok) {
                var esPropietario = "{{ es_propietario }}"; // Esta línea toma el valor de es_propietario de tu contexto Django
                if (esPropietario === "True") {
                    window.location.href = `/perfil/${mascotaId}/`;
                } else {
                    window.location.href = `/perfil-mascota/${mascotaId}/`;
                }
            } else {
                alert(`/perfil/${mascotaId}/`);
            }
        })
        .catch(error => console.error('Error:', error));
}


function vistaImages() {
    var file = document.getElementById('img').files[0];
    if (file) {
        var reader = new FileReader();
        reader.onload = function (e) {
            var previewArea = document.getElementById('imagePreview');
            var existingImage = document.getElementById('profileImage');
            var icon = document.getElementById('profileIcon');

            if (existingImage) {
                existingImage.src = this.result;
            } else {
                if (icon) {
                    previewArea.removeChild(icon);
                }
                var newImage = new Image();
                newImage.id = 'profileImage';
                newImage.src = this.result;
                previewArea.appendChild(newImage);
            }
        };
        reader.readAsDataURL(file);
    }
}

document.getElementById('img').addEventListener('change', vistaImages);

function openModal(element) {
    const publicacionId = element.getAttribute('data-id');
    const modal = document.getElementById('imageModal');
    var dataElement = element.querySelector('span[data-images]');
    var images = dataElement.getAttribute('data-images').split(',');
    var description = dataElement.getAttribute('data-description');

    currentImageIndex = 0; // Resetear el índice al abrir
    document.getElementById('modalImage').src = images[currentImageIndex];
    document.getElementById('imageDescription').textContent = description;
    document.getElementById('idPublicacion').textContent = publicacionId;
    window.currentImages = images; // Guardar las imágenes en una variable global si necesitas cambiar entre ellas

    // Mostrar u ocultar botones de navegación de imágenes
    document.querySelector('.prev').style.display = images.length > 1 ? 'block' : 'none';
    document.querySelector('.next').style.display = images.length > 1 ? 'block' : 'none';

    getComments(publicacionId);
    getLikes(publicacionId);

    checkUserLike(publicacionId);
    modal.style.display = 'flex';
}

function getLikes(publicacionId) {
    fetch(`/get_likes/${publicacionId}/`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Cache-Control': 'no-cache'
        }
    })
        .then(response => response.json())
        .then(data => {
            document.getElementById('imageLikes').textContent = `${data.likes} Likes`;
            document.getElementById('imageLikes').setAttribute('data-nombres', JSON.stringify(data.lista_likes));
        })
        .catch(error => console.error('Error:', error));
}

function getComments(publicacionId) {
    fetch(`/get_comments/${publicacionId}/`)
        .then(response => response.json())
        .then(data => {
            const listaComentarios = document.getElementById('listaComentarios');
            listaComentarios.innerHTML = '';
            data.comentarios.forEach(comentario => {
                const li = document.createElement('li');
                li.className = 'comentario-item'; // Clase para estilos

                // Agregar nombre de la mascota
                const nombre = document.createElement('a');
                nombre.href = comentario.urlPerfil;
                nombre.textContent = comentario.autor;
                nombre.className = 'comentario-nombre'; // Clase para estilos

                // Agregar texto del comentario
                const texto = document.createElement('span');
                texto.textContent = ` ${comentario.texto}`;
                texto.className = 'comentario-texto'; // Clase para estilos

                // Agregar fecha del comentario
                const fecha = document.createElement('span');
                fecha.textContent = comentario.fecha; // Asegúrate de que 'fecha' sea el campo correcto en tu respuesta del servidor
                fecha.className = 'comentario-fecha'; // Clase para estilos

                // Componer el elemento li
                if (comentario.fotoPerfil) {
                    // Agregar imagen de perfil
                    const img = document.createElement('img');
                    img.src = comentario.fotoPerfil;
                    img.className = 'comentario-imagen';
                    li.appendChild(img);
                } else {
                    const icon = document.createElement('i');
                    icon.className = "fas fa-paw comentario-imagen";
                    li.appendChild(icon);
                }

                li.appendChild(nombre);
                li.appendChild(texto);
                li.appendChild(fecha);

                if (comentario.esPropietario) {
                    const eliminar = document.createElement('button');
                    eliminar.textContent = 'x';
                    eliminar.className = 'comentario-eliminar';
                    eliminar.onclick = function () { eliminarComentario(comentario.id, publicacionId); };
                    li.appendChild(eliminar);
                }

                listaComentarios.appendChild(li);
            });
        })
        .catch(error => console.error('Error al cargar comentarios:', error));
}

function eliminarComentario(comentarioId, publicacionId) {
    fetch(`/delete_comentario/${comentarioId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRFToken': getCookie('csrftoken') // Asegúrate de enviar el token CSRF si es necesario
        } // Asegúrate de configurar correctamente CSRF si es necesario
    })
        .then(response => {
            if (response.ok) {
                getComments(publicacionId);  // Recargar comentarios tras eliminar
            } else {
                console.error('No se pudo eliminar el comentario');
            }
        })
        .catch(error => console.error('Error al eliminar comentario:', error));
}

function changeImage(direction) {
    if (window.currentImages) {
        currentImageIndex += direction;
        if (currentImageIndex >= window.currentImages.length) {
            currentImageIndex = 0; // Volver al inicio si supera el número total
        } else if (currentImageIndex < 0) {
            currentImageIndex = window.currentImages.length - 1; // Volver al final si es menor que 0
        }
        document.getElementById('modalImage').src = window.currentImages[currentImageIndex];
    }
}
function closeModal(id) {
    document.getElementById(id).style.display = 'none';
}

function checkUserLike(publicacionId) {
    fetch(`/has_liked/${publicacionId}/`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Cache-Control': 'no-cache'
        }
    })
        .then(response => response.json())
        .then(data => {
            const likeIcon = document.getElementById('likeIcon');
            if (data.has_liked) {
                likeIcon.className = 'fas fa-heart';  // Corazón relleno
            } else {
                likeIcon.className = 'far fa-heart';  // Corazón sin relleno
            }
        })
        .catch(error => console.error('Error:', error));
}

function toggleLikeInModal() {
    const publicacionId = document.getElementById('idPublicacion').textContent;
    const url = `/like_post/${publicacionId}/`;
    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRFToken': getCookie('csrftoken') // Asegurar que el CSRF token es enviado
        }
    })
        .then(response => response.json())
        .then(data => {
            const likeIcon = document.getElementById('likeIcon');
            const likeCountElement = document.getElementById('imageLikes'); // Asegúrate de que este es el ID correcto

            likeCountElement.textContent = `${data.likes} Likes`; // Actualizar directamente desde la respuesta

            if (data.message === 'Like added') {
                likeIcon.className = 'fas fa-heart';  // Corazón relleno
            } else if (data.message === 'Like removed') {
                likeIcon.className = 'far fa-heart';  // Corazón sin relleno
            }
        })
        .catch(error => console.error('Error:', error));
}

function openModalLikes() {
    const nombres = JSON.parse(document.getElementById('imageLikes').getAttribute('data-nombres'));
    const lista = document.createElement('div');
    lista.id = 'modalLikes';
    lista.innerHTML = '<div class="closeLike"><span style="font-size: 30px; cursor:pointer" onclick="closeModalLikes()">&times;</span></div><h5>Me gusta</h5><ul>' +
        nombres.map(data =>
            `<li>` +
            (data.fotoPerfil ? `<img src="${data.fotoPerfil}" class="comentario-imagen">` : `<i class="fas fa-paw comentario-imagen"></i>`) +
            `<a class="modalLikes-nombre" href="${data.perfilUrl}">${data.mascotaNombre}</a>` +
            `<span class="seguir-modal"  onclick="toggleFollow(${data.perfilId}, ${data.siguiendo}, this)">${data.siguiendo ? 'Dejar de seguir' : 'Seguir'}</span>` +
            `</li>`
        ).join('') +
        '</ul>';
    document.body.appendChild(lista);
}

function closeModalLikes() {
    document.getElementById('modalLikes').remove();
}

function comentarioPost() {
    const publicacionId = document.getElementById('idPublicacion').textContent;
    const textoComentario = document.getElementById('nuevoComentario').value;

    if (!textoComentario.trim()) {
        alert("El comentario no puede estar vacío.");
        return;
    }

    fetch(`/comentario_post/${publicacionId}/`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRFToken': getCookie('csrftoken')
        },
        body: JSON.stringify({ texto: textoComentario })
    })
        .then(response => response.json())
        .then(data => {
            if (data.error) {
                alert(data.error);
            } else {
                getComments(publicacionId);

                document.getElementById('nuevoComentario').value = ''; // Limpiar el campo de texto
            }
        })
        .catch(error => console.error('Error:', error));
}

function actualizarComentarios(comentario) {
    const listaComentarios = document.getElementById('listaComentarios'); // Corrige el ID aquí
    let li = document.createElement('li');
    li.className = 'comentario-item';
    const img = document.createElement('img');
    img.src = comentario.fotoPerfil;
    img.className = 'comentario-imagen';

    const nombre = document.createElement('span');
    nombre.textContent = comentario.autor;
    nombre.className = 'comentario-nombre';

    const texto = document.createElement('span');
    texto.textContent = ` ${comentario.texto}`;
    texto.className = 'comentario-texto';

    const fecha = document.createElement('span');
    fecha.textContent = comentario.fecha;
    fecha.className = 'comentario-fecha';

    // Componer el elemento li
    li.appendChild(img);
    li.appendChild(nombre);
    li.appendChild(texto);
    li.appendChild(fecha);

    listaComentarios.appendChild(li);
}



function getCookie(name) {
    let cookieValue = null;
    if (document.cookie && document.cookie !== '') {
        const cookies = document.cookie.split(';');
        for (let i = 0; i < cookies.length; i++) {
            const cookie = cookies[i].trim();
            if (cookie.substring(0, name.length + 1) === (name + '=')) {
                cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
                break;
            }
        }
    }
    return cookieValue;
}

document.addEventListener('DOMContentLoaded', function () {
    const botonSeguir = document.getElementById('boton-seguir');
    if (botonSeguir) {
        botonSeguir.addEventListener('click', function () {
            const mascotaId = botonSeguir.getAttribute('data-mascota-id');
            const isFollowing = botonSeguir.getAttribute('data-siguiendo') === 'true';
            toggleFollow(mascotaId, isFollowing, botonSeguir);
        });
    }
});

// document.addEventListener('DOMContentLoaded', function () {
//     const botonSeguir = document.getElementById('boton-seguir');

//     if (botonSeguir) {
//         botonSeguir.addEventListener('click', function () {
//             const url = botonSeguir.dataset.url;

//             fetch(url, {
//                 method: 'POST',
//                 headers: {
//                     'Content-Type': 'application/json',
//                     'X-CSRFToken': getCookie('csrftoken')  // Asegúrate de tener esta función definida para obtener el CSRF token
//                 },
//                 body: JSON.stringify({})
//             })
//                 .then(response => response.json())
//                 .then(data => {
//                     botonSeguir.textContent = botonSeguir.textContent.includes('Seguir') ? 'Dejar de seguir' : 'Seguir';
//                     document.getElementById('numSeguidores').textContent = data.numSeguidores;
//                 })
//                 .catch(error => console.error('Error:', error));
//         });
//     }
// });


function toggleFollow(perfilId, siguiendo, button) {
    const csrfToken = getCookie('csrftoken'); // Asegúrate de que tienes esta función para obtener el CSRF token

    fetch(`/seguir_perfil/${perfilId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRFToken': csrfToken
        },
        body: JSON.stringify({ seguir: !siguiendo })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            button.textContent = siguiendo ? 'Seguir' : 'Dejar de seguir';
            button.onclick = () => toggleFollow(perfilId, !siguiendo, button);
            if (data.numSeguidores !== undefined) {
                document.getElementById('numSeguidores').textContent = data.numSeguidores;
            }
        } else {
            console.error('Error al cambiar el estado de seguimiento');
        }
    })
    .catch(error => console.error('Error:', error));
}