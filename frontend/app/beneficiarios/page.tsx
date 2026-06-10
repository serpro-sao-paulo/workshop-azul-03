"use client";

/**
 * Beneficiários page — cadastro e consulta.
 * REQ-001..015. CPF always masked in display (REQ-015).
 * source_legacy: CADBENEF.NSN, CONSBENEF.NSN
 */
export default function BeneficiariosPage() {
  return (
    <main className="p-8">
      <h1 className="text-xl font-bold">Gestão de Beneficiários</h1>
      <p className="mt-2 text-sm text-gray-600">
        Cadastro, consulta e elegibilidade de beneficiários.
      </p>
      {/* TODO: Implement form and list using server actions */}
      <div className="mt-4 p-4 border rounded bg-yellow-50">
        <p className="text-sm">
          Aviso: CPF exibido sempre mascarado por lei (LGPD, REQ-015).
        </p>
      </div>
    </main>
  );
}
