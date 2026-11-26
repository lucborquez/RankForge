#!/usr/bin/env bash
set -euo pipefail
umask 077

# Spring listens on loopback. Caddy is the only process exposed by Render.
: "${MODULE:?}" "${MESH_SECRET:?}" "${PORT:=10000}"
[[ "$MESH_SECRET" =~ ^[A-Za-z0-9+/=]+$ ]]
[[ "$PORT" =~ ^[0-9]+$ ]]
conf=$(mktemp /tmp/rankforge-caddy.XXXXXX)
cat > "$conf" <<EOF
{
    admin off
    auto_https off
    persist_config off
}
http://:$PORT {
EOF
if [[ "$MODULE" != api-gateway ]]; then
    cat >> "$conf" <<'EOF'
    @unauthorized not header Proxy-Authorization {$MESH_SECRET}
    respond @unauthorized 403
EOF
fi
cat >> "$conf" <<'EOF'
    reverse_proxy 127.0.0.1:9090 {
        header_up -Proxy-Authorization
    }
}
EOF

# Each peer is an exact Render hostname verified in the workspace after provisioning.
# Keep the local Java clients' existing per-service ports.
while read -r num service hostname; do
    [[ -n "$num" ]] || continue
    [[ "$num" =~ ^80[0-9][0-9]$ && "$hostname" =~ ^[a-z0-9-]+\.onrender\.com$ ]]
    if [[ "$service" == "$MODULE" ]]; then
        upstream=127.0.0.1:9090
    else
        upstream=https://$hostname
    fi
    cat >> "$conf" <<EOF
http://127.0.0.1:$num {
    bind 127.0.0.1
    reverse_proxy $upstream {
        header_up Host {upstream_hostport}
        header_up Proxy-Authorization {\$MESH_SECRET}
    }
}
EOF
done < /app/peers
export XDG_CONFIG_HOME=/tmp XDG_DATA_HOME=/tmp
caddy run --config "$conf" --adapter caddyfile &
proxy_pid=$!
java -XX:MaxRAMPercentage=48 -XX:ActiveProcessorCount=2 -jar /app/application.jar --server.port=9090 --server.address=127.0.0.1 &
java_pid=$!
trap 'kill "$proxy_pid" "$java_pid" 2>/dev/null || true' EXIT
trap 'exit 143' TERM INT
wait -n "$proxy_pid" "$java_pid"
