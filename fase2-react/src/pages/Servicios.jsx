import { useEffect, useState } from "react";
import { listarServicios } from "../api/servicios.js";
import { mensajeDeError } from "../api/errores.js";
import { agruparPorCategoria, formatearDuracion } from "../utils/servicios.js";

function Servicios() {
    const [servicios, setServicios] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState("");
    const [categoria, setCategoria] = useState("todas");

    useEffect(() => {
        let activo = true;
        listarServicios()
            .then((lista) => {
                if (activo) setServicios(lista);
            })
            .catch((e) => {
                if (activo) setError(mensajeDeError(e, "No se pudieron cargar los servicios."));
            })
            .finally(() => {
                if (activo) setCargando(false);
            });
        return () => {
            activo = false;
        };
    }, []);

    if (cargando) return <p className="aviso-no-encontrado">Cargando servicios...</p>;
    if (error) return <p className="aviso-no-encontrado">{error}</p>;

    const grupos = agruparPorCategoria(servicios);
    const visibles = categoria === "todas" ? grupos : grupos.filter((g) => g.categoria === categoria);

    return (
        <section id="servicios" className="container py-5">
            <h2 className="servicios-nombre mb-3">Nuestros Servicios</h2>

            {grupos.length === 0 ? (
                <p className="text-muted">Por ahora no hay servicios disponibles.</p>
            ) : (
                <>
                    <div className="d-flex flex-wrap gap-2 mb-4" role="group" aria-label="Filtrar por categoría">
                        {["todas", ...grupos.map((g) => g.categoria)].map((c) => (
                            <button
                                key={c}
                                type="button"
                                className={`btn btn-sm ${categoria === c ? "btn-primary" : "btn-outline-primary"}`}
                                aria-pressed={categoria === c}
                                onClick={() => setCategoria(c)}
                            >
                                {c === "todas" ? "Todas" : c}
                            </button>
                        ))}
                    </div>

                    {visibles.map((grupo) => (
                        <div key={grupo.categoria} className="mb-5">
                            <h3 className="h4 mb-3">{grupo.categoria}</h3>
                            <div className="row g-4">
                                {grupo.items.map((s) => (
                                    <div key={s.id} className="col-12 col-md-6 col-lg-4">
                                        <div className="card h-100 shadow-sm">
                                            <div className="card-body">
                                                <h4 className="h6 card-title">{s.nombre}</h4>
                                                <p className="mb-2">
                                                    <span className="badge text-bg-light">{s.especie}</span>
                                                </p>
                                                <p className="mb-1 text-muted">
                                                    <i className="bi bi-clock me-1"></i>
                                                    {formatearDuracion(s.duracionMin)}
                                                </p>
                                                <p className="fw-bold mb-1">
                                                    ${s.precio.toLocaleString("es-CL")}
                                                </p>
                                                {s.observaciones && (
                                                    <p className="small text-muted mb-0">{s.observaciones}</p>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    ))}
                </>
            )}
        </section>
    );
}

export default Servicios;
