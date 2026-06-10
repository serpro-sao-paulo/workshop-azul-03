"use client";

/**
 * Pagamentos page — folha, conciliação e relatórios.
 * REQ-029..035. source_legacy: BATCHPGT.NSN, BATCHCON.NSN, BATCHREL.NSN
 */
export default function PagamentosPage() {
  return (
    <main className="p-8">
      <h1 className="text-xl font-bold">Pagamentos & Folha</h1>
      <p className="mt-2 text-sm text-gray-600">
        Geração de folha, conciliação CNAB 240, correção monetária e relatórios.
      </p>
    </main>
  );
}
