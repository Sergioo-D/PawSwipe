let chatSocket = null;

function iniciarWebSocket(slug, usuario_actual, el_otro) {
            if(chatSocket !== null) {
                chatSocket.close();
            }
    const roomSlug = slug; // Asegúrate de reemplazar esto con el slug de la sala actual.
    console.log('este es:', roomSlug);
    console.log('este es:', usuario_actual,el_otro);
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
            chatSocket.send(JSON.stringify({
                'message': message,
                'emisor': usuarioActual,
                'receptor': elOtro,
                'sent': true
            }));
            messageInputDom.value = '';
        };
    };

    chatSocket.onmessage = function (e) {
        const data = JSON.parse(e.data);
        const mensaje = data['mensaje'];
        const emisor = data['emisor'];
        const receptor = data['receptor'];
        const sent = data['sent'];
        const chatLog = document.querySelector('#chat-log');
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

        chatLog.appendChild(messageElement);
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

function cargarChat(slug) {
        fetch(`/chatt/${slug}/`, { headers: { 'X-Requested-With': 'XMLHttpRequest' } })
        .then(response => response.json())
        .then(data => {
            const chatContainer = document.getElementById('chat-container');
            // chatContainer.innerHTML = data.html;
            chatContainer.style.display = 'block';
            iniciarWebSocket(slug, data.usuario_actual,data.el_otro);  // Función para iniciar el WebSocket
            console.log(slug, data)
        })
        .catch(error => console.error('Error al cargar el chat:', error));
    }

