// Minimal Inertia SSR sidecar for generated starters.
// Serves POST /render (any POST path): { page } -> { head, body }.
// Run: npm run build:ssr && node ssr-server.mjs (listens on 13714).
import { createServer } from 'node:http'

const PORT = Number.parseInt(process.env.SSR_PORT ?? '13714', 10)
const { render } = await import('./dist-ssr/assets/ssr.js')

const server = createServer((req, res) => {
  if (req.method !== 'POST') {
    res.writeHead(404, { 'Content-Type': 'application/json' })
    res.end(JSON.stringify({ error: 'POST only' }))
    return
  }
  let raw = ''
  req.on('data', (chunk) => {
    raw += chunk
  })
  req.on('end', async () => {
    try {
      const page = JSON.parse(raw)
      const { head, body } = await render(page)
      res.writeHead(200, { 'Content-Type': 'application/json' })
      res.end(JSON.stringify({ head, body }))
    } catch (error) {
      res.writeHead(500, { 'Content-Type': 'application/json' })
      res.end(JSON.stringify({ error: String(error).slice(0, 300) }))
    }
  })
})

server.listen(PORT, () => {
  console.log(`Inertia SSR sidecar listening on http://localhost:${PORT}`)
})
