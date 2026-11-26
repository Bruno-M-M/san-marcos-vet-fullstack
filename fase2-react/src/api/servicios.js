import clienteApi from "./client.js";

// Público: el catálogo se puede ver sin iniciar sesión.
export async function listarServicios() {
    const { data } = await clienteApi.get("/api/v1/servicios");
    return data;
}
