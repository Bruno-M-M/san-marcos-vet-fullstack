import { describe, test, expect } from "vitest";
import { agruparPorCategoria, formatearDuracion } from "./servicios.js";

describe("formatearDuracion", () => {
    test.each([
        [5, "5 min"],
        [30, "30 min"],
        [45, "45 min"],
        [60, "1 h"],
        [90, "1 h 30 min"],
        [120, "2 h"],
        [1440, "24 h"],
    ])("%i minutos se muestra como %s", (minutos, esperado) => {
        expect(formatearDuracion(minutos)).toBe(esperado);
    });

    test("un valor inválido devuelve texto vacío", () => {
        expect(formatearDuracion(0)).toBe("");
        expect(formatearDuracion(undefined)).toBe("");
        expect(formatearDuracion(null)).toBe("");
    });
});

describe("agruparPorCategoria", () => {
    test("agrupa respetando el orden de llegada", () => {
        const lista = [
            { id: 1, categoria: "Cirugía" },
            { id: 2, categoria: "Cirugía" },
            { id: 3, categoria: "Consultas" },
        ];

        const grupos = agruparPorCategoria(lista);

        expect(grupos.map((g) => g.categoria)).toEqual(["Cirugía", "Consultas"]);
        expect(grupos[0].items.map((s) => s.id)).toEqual([1, 2]);
        expect(grupos[1].items.map((s) => s.id)).toEqual([3]);
    });

    test("una lista vacía no genera grupos", () => {
        expect(agruparPorCategoria([])).toEqual([]);
    });
});
