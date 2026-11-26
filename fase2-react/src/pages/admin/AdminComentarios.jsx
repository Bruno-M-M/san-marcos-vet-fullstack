import { useState, useEffect } from "react";
import { eliminarComentario as eliminarEnApi, listarComentarios } from "../../api/comentarios.js";
import { mensajeDeError } from "../../api/errores.js";

// La API devuelve la fecha como "2026-11-20T10:30:00"; se muestra en formato chileno.
function formatearFecha(fecha) {
    const d = new Date(fecha);
    return Number.isNaN(d.getTime()) ? fecha : d.toLocaleString("es-CL");
}

function AdminComentarios() {
    const [comentarios, setComentarios] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        let activo = true;
        listarComentarios()
            .then((lista) => {
                if (activo) setComentarios(lista);
            })
            .catch((e) => {
                if (activo) setError(mensajeDeError(e, "No se pudieron cargar los comentarios."));
            })
            .finally(() => {
                if (activo) setCargando(false);
            });
        return () => {
            activo = false;
        };
    }, []);

    const eliminarComentario = async (id) => {
        setError("");
        try {
            await eliminarEnApi(id);
            setComentarios((prev) => prev.filter((c) => c.id !== id));
        } catch (e) {
            setError(mensajeDeError(e, "No se pudo eliminar el comentario."));
        }
    };

    return (
        <>
            <h2 className="mb-4">Comentarios ({comentarios.length})</h2>

            {error && (
                <div className="alert alert-danger" role="alert">
                    {error}
                </div>
            )}

            {cargando ? (
                <p className="text-muted">Cargando comentarios...</p>
            ) : comentarios.length === 0 ? (
                <p className="text-muted">Todavía no hay comentarios.</p>
            ) : (
                <div className="table-responsive">
                    <table className="table table-hover align-middle bg-white shadow-sm">
                        <thead>
                            <tr>
                                <th>Fecha</th>
                                <th>Nombre</th>
                                <th>Correo</th>
                                <th>Comentario</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {comentarios.map((c) => (
                                <tr key={c.id}>
                                    <td>{formatearFecha(c.fecha)}</td>
                                    <td>{c.nombre}</td>
                                    <td>{c.correo}</td>
                                    <td style={{ maxWidth: 350 }}>{c.comentario}</td>
                                    <td>
                                        <button
                                            className="btn btn-sm btn-outline-danger"
                                            onClick={() => {
                                                if (window.confirm("¿Eliminar este comentario?")) {
                                                    eliminarComentario(c.id);
                                                }
                                            }}
                                        >
                                            <i className="bi bi-trash"></i>
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </>
    );
}

export default AdminComentarios;