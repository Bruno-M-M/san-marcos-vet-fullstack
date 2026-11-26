import { useState } from "react";
import { mensajeDeError } from "../api/errores.js";

const Dominios_permitidos = ["duoc.cl", "profesor.duoc.cl", "gmail.com"];
const max_comentario = 500;

function correoValido(correo) {
    const partes = correo.trim().toLowerCase().split("@");
    return partes.length === 2 && partes[0] !== "" && Dominios_permitidos.includes(partes[1]);
}

function FormularioComentario({ onEnviar }) {
    const [form, setForm] = useState({ nombre: "", correo: "", comentario: "" });
    const [intento, setIntento] = useState(false);
    const [enviado, setEnviado] = useState(false);
    const [enviando, setEnviando] = useState(false);
    const [errorServidor, setErrorServidor] = useState("");

    const errores = {
        nombre: form.nombre.trim() === "",
        correo: !correoValido(form.correo),
        comentario: form.comentario.trim() === "",
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIntento(true);
        if (errores.nombre || errores.correo || errores.comentario) return;

        setEnviando(true);
        setEnviado(false);
        setErrorServidor("");
        try {
            await onEnviar({
                nombre: form.nombre.trim(),
                correo: form.correo.trim().toLowerCase(),
                comentario: form.comentario.trim(),
            });
            setEnviado(true);
            setForm({ nombre: "", correo: "", comentario: "" });
            setIntento(false);
        } catch (error) {
            // Si falla, el texto queda en el formulario para poder reintentar.
            setErrorServidor(mensajeDeError(error, "No se pudo enviar tu comentario."));
        } finally {
            setEnviando(false);
        }
    };

    const claseCampo = (campo) =>
        `form-control ${intento ? (errores[campo] ? "is-invalid" : "is-valid") : ""}`;

    return (
        <div className="p-4 p-md-5 bg-light-suave rounded-4">
            {enviado && (
                <div className="alert alert-success" role="alert">
                    <i className="bi bi-check-circle-fill"></i> ¡Gracias! Tu mensaje fue enviado
                    correctamente.
                </div>
            )}

            {errorServidor && (
                <div className="alert alert-danger" role="alert">
                    {errorServidor}
                </div>
            )}

            <form id="formContacto" className="form-brand" onSubmit={handleSubmit} noValidate>
                <div className="mb-3">
                    <label htmlFor="nombre" className="form-label">
                        Nombre completo *
                    </label>
                    <input
                        type="text"
                        className={claseCampo("nombre")}
                        id="nombre"
                        name="nombre"
                        maxLength={100}
                        placeholder="Ej: María Pérez"
                        value={form.nombre}
                        onChange={handleChange}
                    />
                    <div className="invalid-feedback">
                        El nombre es obligatorio (máx. 100 caracteres).
                    </div>
                </div>

                <div className="mb-3">
                    <label htmlFor="correo" className="form-label">
                        Correo electrónico *
                    </label>
                    <input
                        type="email"
                        className={claseCampo("correo")}
                        id="correo"
                        name="correo"
                        maxLength={100}
                        placeholder="Ej: maria@gmail.com"
                        value={form.correo}
                        onChange={handleChange}
                    />
                    <div className="form-text">
                        Dominios permitidos: duoc.cl, profesor.duoc.cl, gmail.com
                    </div>
                    <div className="invalid-feedback">
                        Ingresa un correo válido de un dominio permitido (máx. 100 caracteres).
                    </div>
                </div>

                <div className="mb-4">
                    <label htmlFor="comentario" className="form-label">
                        Comentario / Mensaje *
                    </label>
                    <textarea
                        className={claseCampo("comentario")}
                        id="comentario"
                        name="comentario"
                        rows={5}
                        maxLength={max_comentario}
                        placeholder="Cuéntanos en qué podemos ayudarte..."
                        value={form.comentario}
                        onChange={handleChange}
                    ></textarea>
                    <div className="d-flex justify-content-between">
                        <div className="invalid-feedback">
                            El comentario es obligatorio (máx. {max_comentario} caracteres).
                        </div>
                        <small className="text-muted ms-auto">
                            {form.comentario.length} / {max_comentario} caracteres
                        </small>
                    </div>
                </div>

                <button type="submit" className="btn btn-brand w-100" disabled={enviando}>
                    <i className="bi bi-send-fill"></i> Enviar mensaje
                </button>
            </form>
        </div>
    );
}

export default FormularioComentario;