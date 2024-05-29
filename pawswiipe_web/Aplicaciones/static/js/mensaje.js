let chatSocket = null;

// function getCookie(name) {
//     let cookieValue = null;
//     if (document.cookie && document.cookie !== '') {
//         const cookies = document.cookie.split(';');
//         for (let i = 0; i < cookies.length; i++) {
//             let cookie = cookies[i].trim();
//             // Does this cookie string begin with the name we want?
//             if (cookie.startsWith(name + '=')) {
//                 cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
//                 break;
//             }
//         }
//     }
//     return cookieValue;
// }

function iniciarWebSocket(slug, usuario_actual, el_otro) {
    if (chatSocket !== null) {
        chatSocket.close();
    }
    const roomSlug = slug; // Asegúrate de reemplazar esto con el slug de la sala actual.
    console.log('este es:', roomSlug);
    console.log('este es:', usuario_actual, el_otro);
    if (!roomSlug) {
        console.error('roomSlug is empty');
        return;
    }
    chatSocket = new WebSocket(
        'ws://127.0.0.1:8001/ws/chatt/' + roomSlug
    );

    const usuarioActual = usuario_actual;
    const elOtro = el_otro;

    chatSocket.onopen = function (e) {
        document.querySelector('#chat-message-submit').onclick = function (e) {
            const messageInputDom = document.querySelector('#chat-message-input');
            const message = messageInputDom.value;
            if (message === "") {
                return;
            }
            // Aquí asumimos que el WebSocket es solo para enviar el mensaje en tiempo real a otros usuarios conectados.
            chatSocket.send(JSON.stringify({
                'message': message,
                'emisor': usuarioActual,
                'receptor': elOtro,
                'slug': roomSlug,
            }));
            const csrfToken = document.querySelector('[name=csrfmiddlewaretoken]').value;
            console.log(csrfToken);
            fetch(`/chatt/guardar_dm/`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRFToken': csrfToken
                },
                body: JSON.stringify({
                    emisor: usuarioActual,
                    receptor: elOtro,
                    message: message,
                    slug: roomSlug
                })
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`HTTP error! status: ${response.status}, statusText: ${response.statusText}`);
                    }
                    return response.json();
                })
                .then(data => {
                    if (data.status === 'success') {
                        console.log('Mensaje guardado con éxito');
                    } else {
                        console.error('Error al guardar el mensaje: ' + data.message);
                    }
                })
                .catch((error) => {
                    console.error('Error AJAX:', error);
                });

            // Limpiar el campo de entrada
            messageInputDom.value = '';
        };
    };

    chatSocket.onmessage = function (e) {
        const data = JSON.parse(e.data);
        const mensaje = data['mensaje'];
        const emisor = data['emisor'];
        const receptor = data['receptor'];
        const sent = data['sent'];
        // const chatLog = document.querySelector('#chat-log');
        const chatLog = document.querySelector('#messages-container');
        const messageElement = document.createElement('div');
        //messageElement.innerHTML = `<strong>${emisor}:</strong> ${mensaje}`;

        // Si el mensaje fue enviado por el usuario actual, alinearlo a la derecha
        if (emisor === usuarioActual) {
            messageElement.classList.add('men-enviado');
        } else {
            messageElement.classList.add('men-recivido');
        }

        // Agregar el contenido del mensaje al elemento
        messageElement.textContent = mensaje;

        chatLog.prepend(messageElement);
        chatLog.scrollTop = chatLog.scrollHeight;  // Auto-scroll to the latest message
        chatLog.scrollTop = chatLog.scrollHeight;
    };

    chatSocket.onclose = function (e) {
        console.log('Cerrado el chat de slug:', slug);
    };

    document.querySelector("#chat-message-input").onkeyup = function (e) {
        if (e.keyCode === 13) {  // enter, return
            document.querySelector("#chat-message-submit").click();
        }
    };
};

function cargarChat(slug,request_user_mail,emisor_mail) {
    fetch(`/chatt/${slug}/`, { headers: { 'X-Requested-With': 'XMLHttpRequest' } })
        .then(response => response.json())
        .then(data => {
            const chatContainer = document.getElementById('chat-container');
            chatContainer.style.display = 'block';
            const nombreReceptor = document.getElementById('nombreReceptor');
            // nombreReceptor.innerHTML = data.el_otro_nombre + " dueño/a de " + data.nombre_mascota_receptor;
            
            if (request_user_mail == emisor_mail) {
                nombreReceptor.innerHTML = data.el_otro_nombre + " dueño/a de " + data.nombre_mascota_receptor;
            } else {
                nombreReceptor.innerHTML = data.el_otro_nombre + " dueño/a de " + data.nombre_mascota_emisor;
            }
            console.log("masc receptor:", data.nombre_mascota_receptor);
            console.log("masc emisor:", data.nombre_mascota_emisor);
            console.log("usuario actual:", data.usuario_actual);
            console.log("el otro:", data.el_otro);

            iniciarWebSocket(slug, data.usuario_actual, data.el_otro);  // Función para iniciar el WebSocket

            const mensajes = data.mensajes; // Ahora directamente un array de objetos
            const messagesContainer = document.getElementById('messages-container');

            messagesContainer.innerHTML = ''; // Limpiar el contenedor de mensajes anterior

            mensajes.forEach(mensaje => {
                const p = document.createElement('p');
                p.className = mensaje.es_emisor ? 'men-enviado' : 'men-recivido';
                p.innerHTML = ` ${mensaje.mensaje}`;
                messagesContainer.appendChild(p);
            });

            console.log(slug, data);
        })
        .catch(error => console.error('Error al cargar el chat:', error));
}

document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const slug = urlParams.get('slug');
    // const mascotaNombre = urlParams.get('mascota_nombre');

    if (slug) {
        console.log('Cargando chat:', slug);
        cargarChat(slug);  // Esta función debería iniciar la conexión WebSocket y cargar los detalles del chat.
    }
});



