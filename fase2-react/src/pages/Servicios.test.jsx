import { vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import Servicios from "./Servicios.jsx";

const api = vi.hoisted(() => ({ listarServicios: vi.fn() }));
vi.mock("../api/servicios.js", () => api);

const serviciosDePrueba = [
  { id: 1, codigo: "CI001", categoria: "Cirugía", nombre: "Esterilización hembra canina", especie: "Perra", duracionMin: 90, precio: 80000, observaciones: "Incluye anestesia", agendable: true },
  { id: 2, codigo: "SV001", categoria: "Consultas", nombre: "Consulta general", especie: "Perro / Gato", duracionMin: 30, precio: 15000, observaciones: null, agendable: true },
  { id: 3, codigo: "OT004", categoria: "Otros", nombre: "Hospitalización (por día)", especie: "Perro / Gato", duracionMin: 1440, precio: 30000, observaciones: "Incluye monitoreo", agendable: false },
];

describe("Servicios", () => {
  beforeEach(() => {
    api.listarServicios.mockReset();
  });

  it("muestra el estado de carga mientras espera a la API", () => {
    api.listarServicios.mockReturnValue(new Promise(() => {}));
    render(<Servicios />);

    expect(screen.getByText("Cargando servicios...")).toBeTruthy();
  });

  it("lista los servicios agrupados por categoría con duración y precio", async () => {
    api.listarServicios.mockResolvedValue(serviciosDePrueba);
    render(<Servicios />);

    expect(await screen.findByText("Esterilización hembra canina")).toBeTruthy();
    expect(screen.getByRole("heading", { name: "Cirugía" })).toBeTruthy();
    expect(screen.getByRole("heading", { name: "Consultas" })).toBeTruthy();
    expect(screen.getByText("1 h 30 min")).toBeTruthy();
    expect(screen.getByText("30 min")).toBeTruthy();
    expect(screen.getByText("24 h")).toBeTruthy();
    expect(screen.getByText("$80.000")).toBeTruthy();
    expect(screen.getByText("$15.000")).toBeTruthy();
    expect(screen.getByText("Incluye anestesia")).toBeTruthy();
  });

  it("al elegir una categoría muestra solo esa", async () => {
    api.listarServicios.mockResolvedValue(serviciosDePrueba);
    render(<Servicios />);
    await screen.findByText("Consulta general");

    fireEvent.click(screen.getByRole("button", { name: "Consultas" }));

    expect(screen.getByText("Consulta general")).toBeTruthy();
    expect(screen.queryByText("Esterilización hembra canina")).toBeNull();
    expect(screen.getByRole("button", { name: "Consultas" }).getAttribute("aria-pressed")).toBe("true");
  });

  it("el botón Todas vuelve a mostrar todo", async () => {
    api.listarServicios.mockResolvedValue(serviciosDePrueba);
    render(<Servicios />);
    await screen.findByText("Consulta general");

    fireEvent.click(screen.getByRole("button", { name: "Consultas" }));
    fireEvent.click(screen.getByRole("button", { name: "Todas" }));

    expect(screen.getByText("Esterilización hembra canina")).toBeTruthy();
    expect(screen.getByText("Consulta general")).toBeTruthy();
  });

  it("si no hay servicios, lo avisa", async () => {
    api.listarServicios.mockResolvedValue([]);
    render(<Servicios />);

    expect(await screen.findByText("Por ahora no hay servicios disponibles.")).toBeTruthy();
  });

  it("si la API falla, muestra el error", async () => {
    api.listarServicios.mockRejectedValue(new Error("Network Error"));
    render(<Servicios />);

    expect(await screen.findByText("No se pudo conectar con el servidor.")).toBeTruthy();
  });
});
