// Convierte minutos en texto corto: 30 -> "30 min", 90 -> "1 h 30 min", 1440 -> "24 h".
export function formatearDuracion(minutos) {
    if (!Number.isFinite(minutos) || minutos <= 0) return "";
    if (minutos < 60) return `${minutos} min`;
    const horas = Math.floor(minutos / 60);
    const resto = minutos % 60;
    return resto === 0 ? `${horas} h` : `${horas} h ${resto} min`;
}

// Agrupa los servicios por categoría respetando el orden en que llegan de la API.
export function agruparPorCategoria(servicios) {
    const grupos = new Map();
    servicios.forEach((s) => {
        if (!grupos.has(s.categoria)) grupos.set(s.categoria, []);
        grupos.get(s.categoria).push(s);
    });
    return [...grupos.entries()].map(([categoria, items]) => ({ categoria, items }));
}
