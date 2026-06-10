import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "SIFAP — Sistema de Fiscalização e Administração de Pagamentos",
  description: "Sistema modernizado de gestão de benefícios sociais",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="pt-BR">
      <body>{children}</body>
    </html>
  );
}
