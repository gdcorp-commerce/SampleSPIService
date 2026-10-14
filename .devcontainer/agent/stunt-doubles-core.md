## stunt-doubles infrastructure

The root CA is trusted system-wide in this devcontainer. HTTPS calls to all mocked
service hostnames succeed without `--insecure` or certificate errors.

**CA certificate** — `/usr/local/share/stunt-doubles/ca/root-ca.crt`

Trust the CA in another container or process:
```bash
# Node.js (already set via NODE_EXTRA_CA_CERTS)
echo $NODE_EXTRA_CA_CERTS    # → /usr/local/share/stunt-doubles/ca/root-ca.crt

# curl
curl --cacert /usr/local/share/stunt-doubles/ca/root-ca.crt https://sso.godaddy.com/...

# Mount into a Docker container
docker run --rm \
  -v /usr/local/share/stunt-doubles/ca/root-ca.crt:/usr/local/share/ca-certificates/stunt.crt \
  ...
```

**Docker network** — `stunt-doubles-net`

Add a container to the mock network so it can reach service hostnames:
```bash
docker run --rm --network stunt-doubles-net ...
```

**Docker volume** — `stunt-doubles-certs`

Contains the root CA and all service TLS certs. Mount read-only in containers that
need to trust or present certificates:
```bash
-v stunt-doubles-certs:/certs:ro
```

No stunt-doubles services are currently wired in this repo (`ALL_SERVICES` is empty) — this feature is included so `stunt up <service>` is available if one is added later, e.g. via `/gdc:scaffold-devcontainer authz`.
