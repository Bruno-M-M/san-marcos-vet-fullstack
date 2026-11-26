import clienteApi from "./client.js";

// Público: el formulario del Home no necesita sesión.
export async function publicarComentario(datos) {
    const { data } = await clienteApi.post("/api/v1/comentarios", datos);
    return data;
}

// Solo administrador (el token lo agrega el cliente).
export async function listarComentarios() {
    const { data } = await clienteApi.get("/api/v1/comentarios");
    return data;
}

export async function eliminarComentario(id) {
    await clienteApi.delete(`/api/v1/comentarios/${id}`);
}
