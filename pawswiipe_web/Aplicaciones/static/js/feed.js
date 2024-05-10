function abrirModal(id) {
    document.getElementById(id).style.display = 'flex';
}

document.addEventListener('DOMContentLoaded', function () {
    let pageNum = 1;
    const observer = new IntersectionObserver(entries => {
        if (entries[0].isIntersecting) {
            pageNum++;
            fetch(`/feed/?page=${pageNum}`)
                .then(response => response.json())
                .then(data => {
                    if (!data.has_next) {
                        observer.unobserve(document.querySelector('#end-element'));
                    }
                    data.publicaciones.forEach(pub => {
                        const newPub = document.createElement('div');
                        newPub.innerHTML = `<div class="publicacion">
                                                <div class="cabecera-publicacion">
                                                    <img src="${pub.perfil__fotoPerfil}" alt="imagen de perfil">
                                                    <div class="detalles-usuario">
                                                        <strong>${pub.perfil__mascota__nombre}</strong>
                                                        <span>${new Date(pub.fecha_creacion).toLocaleDateString()}</span>
                                                    </div>
                                                </div>
                                                <p class="texto-publicacion">${pub.texto}</p>
                                                <div class="interacciones">
                                                    <span class="likes">${pub.likes} Me gusta</span>
                                                </div>
                                            </div>`;
                        document.querySelector('.contenedor-publicaciones').appendChild(newPub);
                    });
                })
                .catch(error => console.error('Error loading more posts:', error));
        }
    });

    observer.observe(document.querySelector('#end-element'));
});
