"use client";

/**
 * Auditoria page — trilha de auditoria & conformidade.
 * REQ-036, REQ-037. Exclusões sempre visíveis (IN-TCU 63).
 * source_legacy: N/A (ADR-004)
 */
export default function AuditoriaPage() {
  return (
    <main className="p-8">
      <h1 className="text-xl font-bold">Trilha de Auditoria & Conformidade</h1>
      <p className="mt-2 text-sm text-gray-600">
        Todos os eventos, incluindo exclusões (IN-TCU 63 — exclusões sempre visíveis).
      </p>
      {/* TODO: Implement audit trail table with server component */}
    </main>
  );
}
