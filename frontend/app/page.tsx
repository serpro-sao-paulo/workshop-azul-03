export default function HomePage() {
  return (
    <main className="p-8">
      <h1 className="text-2xl font-bold">SIFAP — Sistema Modernizado</h1>
      <nav className="mt-4 space-y-2">
        <p><a href="/beneficiarios" className="text-blue-600 underline">Beneficiários</a></p>
        <p><a href="/programas" className="text-blue-600 underline">Programas Sociais</a></p>
        <p><a href="/pagamentos" className="text-blue-600 underline">Pagamentos</a></p>
        <p><a href="/auditoria" className="text-blue-600 underline">Auditoria</a></p>
      </nav>
    </main>
  );
}
