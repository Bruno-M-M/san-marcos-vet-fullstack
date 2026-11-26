import { describe, test, expect, beforeEach, vi } from "vitest";
import { eliminarComentario, listarComentarios, publicarComentario } from "./comentarios.js";

const cliente = vi.hoisted(() => ({ get: vi.fn(), post: vi.fn(), delete: vi.fn() }));
vi.mock("./client.js", () => ({ default: cliente }));

describe("api/comentarios", () => {
    beforeEach(() => {
        cliente.get.mockReset();
        cliente.post.mockReset();
        cliente.delete.mockReset();
    });

    test("publicarComentario hace POST /api/v1/comentarios con los datos y devuelve el creado", async () => {
        const datos = { nombre: "Ana", correo: "ana@gmail.com", comentario: "Hola" };
        cliente.post.mockResolvedValue({ data: { id: 7, ...datos } });

        await expect(publicarComentario(datos)).resolves.toEqual({ id: 7, ...datos });
        expect(cliente.post).toHaveBeenCalledWith("/api/v1/comentarios", datos);
    });

    test("listarComentarios pide GET /api/v1/comentarios y devuelve la lista", async () => {
        cliente.get.mockResolvedValue({ data: [{ id: 1 }, { id: 2 }] });

        await expect(listarComentarios()).resolves.toEqual([{ id: 1 }, { id: 2 }]);
        expect(cliente.get).toHaveBeenCalledWith("/api/v1/comentarios");
    });

    test("eliminarComentario hace DELETE /api/v1/comentarios/{id}", async () => {
        cliente.delete.mockResolvedValue({});

        await eliminarComentario(5);

        expect(cliente.delete).toHaveBeenCalledWith("/api/v1/comentarios/5");
    });

    test("si la API falla, el error llega a quien llamó", async () => {
        const error = new Error("sin conexión");
        cliente.post.mockRejectedValue(error);

        await expect(publicarComentario({})).rejects.toBe(error);
    });
});
