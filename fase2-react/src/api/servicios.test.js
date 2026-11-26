import { describe, test, expect, beforeEach, vi } from "vitest";
import { listarServicios } from "./servicios.js";

const cliente = vi.hoisted(() => ({ get: vi.fn() }));
vi.mock("./client.js", () => ({ default: cliente }));

describe("api/servicios", () => {
    beforeEach(() => {
        cliente.get.mockReset();
    });

    test("listarServicios pide GET /api/v1/servicios y devuelve la lista", async () => {
        cliente.get.mockResolvedValue({ data: [{ id: 1, nombre: "Consulta general" }] });

        await expect(listarServicios()).resolves.toEqual([{ id: 1, nombre: "Consulta general" }]);
        expect(cliente.get).toHaveBeenCalledWith("/api/v1/servicios");
    });

    test("si la API falla, el error llega a quien llamó", async () => {
        const error = new Error("sin conexión");
        cliente.get.mockRejectedValue(error);

        await expect(listarServicios()).rejects.toBe(error);
    });
});
