document.addEventListener('DOMContentLoaded', function () {
    let pageNum = 1;
    const observer = new IntersectionObserver(entries => {
        if (entries[0].isIntersecting) {
            pageNum++;
            fetch(`/feed/?page=${pageNum}`, {
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            })
            .then(response => response.json())
            .then(data => {
                if (!data.has_next) {
                    observer.unobserve(document.querySelector('#end-element'));
                }
                document.querySelector('.contenedor-publicaciones').insertAdjacentHTML('beforeend', data.html);
            })
            .catch(error => console.error('Error loading more posts:', error));
        }
    });

    observer.observe(document.querySelector('#end-element'));
});


function openModalLikes(publicacionId) {
    fetch(`/get_likes/${publicacionId}/`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Cache-Control': 'no-cache'
        }
    })
    .then(response => response.json())
    .then(data => {
        const nombres = data.lista_likes; 
        const lista = document.createElement('div');
        lista.id = 'modalLikes';
        lista.innerHTML = '<div class="closeLike"><span style="font-size: 30px; cursor:pointer" onclick="closeModalLikes()">&times;</span></div><h5 style="text-align: center">Likes</h5><ul>' +
            nombres.map(data =>
                `<li>` +
                (data.fotoPerfil ? `<img src="${data.fotoPerfil}" class="comentario-imagen">` : `<i class="fas fa-paw comentario-imagen"></i>`) +
                `<a class="modalLikes-nombre" href="${data.perfilUrl}">${data.mascotaNombre}</a>` +
                `<span class="seguir-modal" onclick="toggleFollow(${data.perfilId}, ${data.siguiendo}, this)">${data.siguiendo ? 'Dejar de seguir' : 'Seguir'}</span>` +
                `</li>`
            ).join('') +
            '</ul>';
        document.body.appendChild(lista);
    })
    .catch(error => console.error('Error:', error));
}

function closeModalLikes() {
    document.getElementById('modalLikes').remove();
}

function toggleLike(publicacionId) {
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
            const likeIcon = document.querySelector(`#likeIcon-${publicacionId}`);
            const likeCountElement = document.querySelector(`#imageLikes-${publicacionId}`);

            likeCountElement.textContent = `${data.likes} Likes`; // Actualizar directamente desde la respuesta

            if (data.message === 'Like added') {
                likeIcon.className = 'fas fa-heart iconHeart'; 
                likeIcon.style = 'color: #ff9900;' // Corazón relleno
            } else if (data.message === 'Like removed') {
                likeIcon.className = 'far fa-heart iconHeart';  // Corazón sin relleno
                likeIcon.style = 'color: black;'
            }
        })
        .catch(error => console.error('Error:', error));
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

let currentImageIndex = 0;
let currentImages = [];

function openModal(element) {
    const publicacionId = element.getAttribute('data-id');
    const modal = document.getElementById('imageModal');
    const dataElement = element;
    const images = dataElement.getAttribute('data-images').split(',');
    const description = dataElement.getAttribute('data-description');

    currentImageIndex = 0; // Resetear el índice al abrir
    document.getElementById('modalImage').src = images[currentImageIndex];
    document.getElementById('imageDescription').textContent = description;
    document.getElementById('idPublicacion').textContent = publicacionId;
    currentImages = images; // Guardar las imágenes en una variable global si necesitas cambiar entre ellas

    // Mostrar u ocultar botones de navegación de imágenes
    document.querySelector('.prev').style.display = images.length > 1 ? 'block' : 'none';
    document.querySelector('.next').style.display = images.length > 1 ? 'block' : 'none';

    getComments(publicacionId);
    getLikes(publicacionId);

    checkUserLike(publicacionId);
    modal.style.display = 'flex';
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.style.display = 'none';
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
                li.className = 'comentario-item';

                const nombre = document.createElement('a');
                nombre.href = comentario.urlPerfil;
                nombre.textContent = comentario.autor;
                nombre.className = 'comentario-nombre';

                const texto = document.createElement('span');
                texto.textContent = ` ${comentario.texto}`;
                texto.className = 'comentario-texto';

                const fecha = document.createElement('span');
                fecha.textContent = comentario.fecha;
                fecha.className = 'comentario-fecha';

                if (comentario.fotoPerfil) {
                    const img = document.createElement('img');
                    img.src = comentario.fotoPerfil;
                    img.className = 'comentario-imagen';
                    li.appendChild(img);
                } else {
                    const icon = document.createElement('i');
                    icon.className = 'fas fa-paw comentario-imagen';
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

function changeImageG(containerId, direction) {
    const container = document.getElementById(containerId);
    const images = container.querySelectorAll('.publicacion-img');
    let currentIdx = Array.from(images).findIndex(img => img.style.display === 'block' || img.style.display === '');

    images[currentIdx].style.display = 'none'; // Ocultar la imagen actual
    currentIdx += direction; // Cambiar índice

    if (currentIdx >= images.length) {
        currentIdx = 0; // Volver al inicio si supera el número total
    } else if (currentIdx < 0) {
        currentIdx = images.length - 1; // Volver al final si es menor que 0
    }

    images[currentIdx].style.display = 'block'; // Mostrar la nueva imagen
}

function changeImage(direction) {
    if (currentImages) {
        currentImageIndex += direction;
        if (currentImageIndex >= currentImages.length) {
            currentImageIndex = 0; // Volver al inicio si supera el número total
        } else if (currentImageIndex < 0) {
            currentImageIndex = currentImages.length - 1; // Volver al final si es menor que 0
        }
        document.getElementById('modalImage').src = currentImages[currentImageIndex];
    }
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
