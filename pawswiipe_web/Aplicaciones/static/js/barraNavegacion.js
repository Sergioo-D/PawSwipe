function cerrarModal() {
    var preview = document.getElementById("imageVista");
    preview.innerHTML = ''; 
    document.getElementById("uploadModal").style.display = 'none';
}

function abrirModal(id) {
    document.getElementById(id).style.display = 'flex';
}
function previewImages() {
    var preview = document.getElementById('imageVista');
    preview.innerHTML = '';  // Limpia el contenedor de la vista previa
    if (this.files) {
        [].forEach.call(this.files, readAndPreview);
    }

    function readAndPreview(file) {

        // Asegúrate de que `file.name` coincida con nuestro filtro de extensiones
        if (!/\.(jpe?g|png|gif)$/i.test(file.name)) {
            return alert(file.name + " is not an image");
        } // Si no es una imagen, salimos de la función

        var reader = new FileReader();

        reader.addEventListener("load", function () {
            var image = new Image();
            image.height = 300; // Establece la altura deseada para las imágenes de vista previa
            image.width = 300
            image.title = file.name;
            image.src = this.result;
            preview.appendChild(image);
        });

        reader.readAsDataURL(file);
    }
}

function cerrarModalSearch(id) {
    
    document.getElementById(id).style.display = 'none';
}
// Añade el evento 'change' manualmente
document.getElementById('images').onchange = previewImages;

document.addEventListener("DOMContentLoaded", function() {
    const inputBusqueda = document.getElementById('input-busqueda');
    const resultadosDiv = document.getElementById('resultados-busqueda');

    inputBusqueda.addEventListener('keyup', function() {
        const query = inputBusqueda.value;

        if (query.length > 0) {
            fetch(`/buscar_perfiles/?q=${encodeURIComponent(query)}`)
                .then(response => response.json())
                .then(data => {
                    resultadosDiv.innerHTML = ''; // Limpiar resultados anteriores
                    if (data.resultados && data.resultados.length > 0) {
                        data.resultados.forEach(mascota => {
                            const div = document.createElement('div');
                            const imageUrl = mascota.foto_url;
                            if (imageUrl) {
                                // Si hay una URL de imagen, usarla para el <img>
                                div.innerHTML = `<a href="${mascota.perfil_url}"><img src="${imageUrl}" alt="Foto de ${mascota.nombre}" style="width:29px; height:29px; border-radius:50%;"> ${mascota.nombre} </a>`;
                            } else {
                                // Si no hay imagen, usar un ícono de FontAwesome
                                div.innerHTML = `<a href="${mascota.perfil_url}"><i class="fas fa-paw"></i> ${mascota.nombre}</a>`;
                            }
                            resultadosDiv.appendChild(div);
                        });
                    } else {
                        resultadosDiv.innerHTML = '<div>No se encontraron resultados.</div>';
                    }
                })
                .catch(error => console.error('Error:', error));
        } else {
            resultadosDiv.innerHTML = ''; // Limpiar resultados si la query es demasiado corta
        }
    });
});
