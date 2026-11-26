import { vi } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import Home from "./Home.jsx";

const api = vi.hoisted(() => ({ publicarComentario: vi.fn() }));
vi.mock("../api/comentarios.js", () => api);

// Home usa <Link>, así que necesita un Router alrededor
function renderHome() {
  return render(
    <MemoryRouter>
      <Home />
    </MemoryRouter>
  );
}

function escribirComentario({ nombre, correo, comentario }) {
  fireEvent.change(screen.getByLabelText(/nombre completo/i), { target: { value: nombre } });
  fireEvent.change(screen.getByLabelText(/correo electrónico/i), { target: { value: correo } });
  fireEvent.change(screen.getByLabelText(/comentario \/ mensaje/i), { target: { value: comentario } });
  fireEvent.click(screen.getByRole("button", { name: /enviar mensaje/i }));
}

describe("Home", () => {
  beforeEach(() => {
    api.publicarComentario.mockReset();
  });

  it("muestra el título principal del hero", () => {
    renderHome();
    const h1 = screen.getByRole("heading", { level: 1 });
    expect(h1.textContent).toMatch(/Cuidamos a tu mejor amigo/i);
  });

  it("muestra las 4 tarjetas de información de la tienda", () => {
    renderHome();
    ["Horario", "Ubicación", "Despacho", "Garantía"].forEach((titulo) => {
      expect(screen.getByText(titulo)).toBeTruthy();
    });
  });

  it("los botones del hero apuntan a /servicios y /blogs", () => {
    renderHome();
    expect(screen.getByRole("link", { name: /agendar una hora/i }).getAttribute("href")).toBe("/servicios");
    expect(screen.getByRole("link", { name: /ver blogs/i }).getAttribute("href")).toBe("/blogs");
  });

  it("un comentario válido se envía a la API con el correo en minúsculas", async () => {
    api.publicarComentario.mockResolvedValue({ id: 1 });
    renderHome();
    escribirComentario({
      nombre: "María Pérez",
      correo: "MARIA@gmail.com",
      comentario: "Excelente atención",
    });

    expect(await screen.findByText(/tu mensaje fue enviado correctamente/i)).toBeTruthy();
    expect(api.publicarComentario).toHaveBeenCalledTimes(1);
    expect(api.publicarComentario).toHaveBeenCalledWith({
      nombre: "María Pérez",
      correo: "maria@gmail.com", // el formulario lo pasa a minúsculas
      comentario: "Excelente atención",
    });
  });

  it("un comentario con dominio de correo no permitido NO se envía", () => {
    renderHome();
    escribirComentario({
      nombre: "Pedro",
      correo: "pedro@hotmail.com",
      comentario: "Hola",
    });

    expect(api.publicarComentario).not.toHaveBeenCalled();
    expect(screen.queryByText(/tu mensaje fue enviado correctamente/i)).toBeNull();
  });

  it("si el servidor rechaza el comentario, muestra el error y conserva el texto", async () => {
    api.publicarComentario.mockRejectedValue({
      response: { data: { mensaje: "Hay datos inválidos.", errores: { correo: "Correo no permitido." } } },
    });
    renderHome();
    escribirComentario({
      nombre: "Ana",
      correo: "ana@gmail.com",
      comentario: "Hola",
    });

    expect(await screen.findByText("Correo no permitido.")).toBeTruthy();
    expect(screen.queryByText(/tu mensaje fue enviado correctamente/i)).toBeNull();
    await waitFor(() => {
      expect(screen.getByLabelText(/nombre completo/i).value).toBe("Ana");
    });
  });

  it("si no hay conexión con el servidor, avisa al visitante", async () => {
    api.publicarComentario.mockRejectedValue(new Error("Network Error"));
    renderHome();
    escribirComentario({
      nombre: "Ana",
      correo: "ana@gmail.com",
      comentario: "Hola",
    });

    expect(await screen.findByText("No se pudo conectar con el servidor.")).toBeTruthy();
  });
});
