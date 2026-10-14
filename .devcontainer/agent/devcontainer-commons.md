## devcontainer-commons — agent-agnostic container utilities

Provides git/gh safety wrappers, outbound network blocking, and MCP server configuration. Works with any AI coding agent feature (claude-code, codex, pi).

**Git safety**

Git `safe.directory` is configured at container creation (defaults to `/workspace`). An SSH auth wrapper forces git to use HTTPS with `GIT_TOKEN` for all remote operations — SSH key auth is blocked. A credential helper injects `GIT_TOKEN` for all `github.com` HTTPS operations, and git identity is set from `GIT_AUTHOR_NAME` / `GIT_AUTHOR_EMAIL`.

**AWS credentials**

Add a bind mount directly to `devcontainer.json` — no feature option required:
```jsonc
"mounts": [
  "source=${localEnv:HOME}/.aws,target=/home/vscode/.aws,type=bind,consistency=cached,readonly"
]
```

**Outbound network blocking** (when `blockedHosts` is set)

iptables `OUTPUT DROP` rules are re-applied on every container start. Connections to blocked hostnames fail at the kernel level. Rules are resolved at start time — IP changes between starts are picked up automatically.

**GitHub MCP server** (when `GIT_TOKEN` is set at container start)

`github-mcp-server` is registered via `claude mcp add-json --scope user` into `~/.claude.json` and written to `~/.codex/github-mcp.toml` on every container start. Use this for all GitHub read/write operations — the `gh` CLI write commands are denied in claude-code's settings.

Enabled toolsets: set by the `githubToolsets` option (default: `context,repos,issues,pull_requests,users`).

**Atlassian MCP server** (when `ATLASSIAN_API_TOKEN` and `ATLASSIAN_USERNAME` are set)

`mcp-atlassian` (via `uvx`) is configured for Claude Code and Codex on every container start. Covers both Jira and Confluence. Write access is controlled by the `allowAtlassianWrite` option — default is read-only.

**AGENT_CONTEXT_PATH** (when `agentContextPath` is set)

Exported via `/etc/profile.d/devcontainer-commons.sh` so AI agent features can load it in their own `postStartCommand` without repeating the option.
