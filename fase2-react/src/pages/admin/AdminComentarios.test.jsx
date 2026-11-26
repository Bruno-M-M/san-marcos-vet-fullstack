import { vi } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import AdminComentarios from "./AdminComentarios.jsx";

const api = vi.hoisted(() => ({
  listarComentarios: vi.fn(),
  eliminarComentario: vi.fn(),
}));
vi.mock("../../api/comentarios.js", () => api);

const comentariosDePrueba = [
  { id: 1, fecha: "2026-10-01T10:30:00", nombre: "Ana", correo: "ana@gmail.com", comentario: "Muy buena atención" },
  { id: 2, fecha: "2026-10-02T15:00:00", nombre: "Luis", correo: "luis@duoc.cl", comentario: "Rápido y amable" },
];

describe("AdminComentarios", () => {
  beforeEach(() => {
    api.listarComentarios.mockReset();
    api.eliminarComentario.mockReset();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("sin comentarios muestra el mensaje de lista vacía", async () => {
    api.listarComentarios.mockResolvedValue([]);
    render(<AdminComentarios />);

    expect(await screen.findByText("Todavía no hay comentarios.")).toBeTruthy();
    expect(screen.getByText("Comentarios (0)")).toBeTruthy();
  });

  it("lista los comentarios que devuelve la API", async () => {
    api.listarComentarios.mockResolvedValue(comentariosDePrueba);
    render(<AdminComentarios />);

    expect(await screen.findByText("Ana")).toBeTruthy();
    expect(screen.getByText("Comentarios (2)")).toBeTruthy();
    expect(screen.getByText("Luis")).toBeTruthy();
    expect(screen.getByText("Muy buena atención")).toBeTruthy();
  });

  it("si la API falla al cargar, muestra el error", async () => {
    api.listarComentarios.mockRejectedValue({ response: { data: { mensaje: "Tu rol no tiene permiso para esta acción" } } });
    render(<AdminComentarios />);

    expect(await screen.findByText("Tu rol no tiene permiso para esta acción")).toBeTruthy();
  });

  it("al confirmar, elimina el comentario en la API y lo quita de la lista", async () => {
    api.listarComentarios.mockResolvedValue(comentariosDePrueba);
    api.eliminarComentario.mockResolvedValue();
    const confirmMock = vi.spyOn(window, "confirm").mockReturnValue(true); // el usuario acepta
    render(<AdminComentarios />);
    await screen.findByText("Ana");

    fireEvent.click(screen.getAllByRole("button")[0]);

    expect(confirmMock).toHaveBeenCalledWith("¿Eliminar este comentario?");
    await waitFor(() => expect(screen.queryByText("Ana")).toBeNull());
    expect(api.eliminarComentario).toHaveBeenCalledWith(1);
    expect(screen.getByText("Luis")).toBeTruthy();
  });

  it("si el usuario cancela el confirm, no elimina nada", async () => {
    api.listarComentarios.mockResolvedValue(comentariosDePrueba);
    vi.spyOn(window, "confirm").mockReturnValue(false); // el usuario cancela
    render(<AdminComentarios />);
    await screen.findByText("Ana");

    fireEvent.click(screen.getAllByRole("button")[0]);

    expect(api.eliminarComentario).not.toHaveBeenCalled();
    expect(screen.getByText("Ana")).toBeTruthy();
  });

  it("si la API falla al eliminar, muestra el error y deja el comentario", async () => {
    api.listarComentarios.mockResolvedValue(comentariosDePrueba);
    api.eliminarComentario.mockRejectedValue({ response: { data: { mensaje: "No existe el comentario con id 1." } } });
    vi.spyOn(window, "confirm").mockReturnValue(true);
    render(<AdminComentarios />);
    await screen.findByText("Ana");

    fireEvent.click(screen.getAllByRole("button")[0]);

    expect(await screen.findByText("No existe el comentario con id 1.")).toBeTruthy();
    expect(screen.getByText("Ana")).toBeTruthy();
  });
});
