export default function Footer() {
  return (
    <footer className="app-footer">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-6 text-sm">
        <p>© {new Date().getFullYear()} IdeasHub</p>
        <p>RedGit • Ideas Service</p>
      </div>
    </footer>
  );
}
