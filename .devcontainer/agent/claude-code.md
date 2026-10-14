## claude-code — Claude Code CLI

Claude Code (`claude`) is installed globally and configured to run without per-call permission prompts (`bypassPermissions` mode). All tool calls are auto-approved — no interactive confirmation is needed.

**Credentials** — sourced via `/etc/profile.d/claude-code.sh` at every shell startup:

| Env var | Source | Purpose |
|---------|--------|---------|
| `ANTHROPIC_API_KEY` | `GOCODE_API_TOKEN` | GoDaddy gocode LiteLLM proxy key |
| `ANTHROPIC_BASE_URL` | `GOCODE_BASE_URL` | GoDaddy gocode proxy URL |
| `ANTHROPIC_MODEL` | baked in at image build | Claude model — set by `anthropicModel` option |
| `NODE_EXTRA_CA_CERTS` | `/etc/ssl/certs/ca-certificates.crt` | Required for TLS to the gocode proxy from Node.js |

**Static env vars (always set):**
- `CLAUDE_DANGEROUSLY_DISABLE_SANDBOX=true` — required for container operation
- `CLAUDE_CODE_DISABLE_EXPERIMENTAL_BETAS=1` — GoDaddy proxy compatibility

**Agent context:** `~/.claude/CLAUDE.md` is written on every container start via `postStartCommand`. It points to the project's `agentContextPath` configured in `devcontainer.json`.

**Default denied operations** (baked into `~/.claude/settings.json` at image build time):
```
git push        gh pr create    gh pr merge     gh pr close
gh pr reopen    gh issue create gh repo create  gh repo fork
gh api
```

Use the GitHub MCP server (configured by `devcontainer-commons` when `GIT_TOKEN` is set) for GitHub operations instead of the `gh` CLI.

**GitHub MCP and Atlassian MCP:** Wired into `~/.claude/settings.json` by `devcontainer-commons` on every container start when the relevant tokens are set. Check `~/.claude/settings.json` to see what MCP servers are active.

**Atlassian MCP authentication (first time only):**
```bash
npx -y mcp-remote@latest https://mcp.atlassian.com/v1/mcp 37777
# A browser tab opens → approve → Ctrl+C after redirect
```
Tokens are cached in `~/.mcp-auth/`. Mount `~/.mcp-auth` from the host to persist across rebuilds.

**Verify Claude Code is working:**
```bash
claude --version
echo $ANTHROPIC_API_KEY   # should be non-empty if GOCODE_API_TOKEN is set
```
