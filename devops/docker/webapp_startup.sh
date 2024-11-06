#!/bin/sh
envsubst '\${API_EXTERNAL_URL} \${API_EXTERNAL_KEY} \${API_BCP_URL} \${API_GATEWAY_CLIENT_ID} \${API_GATEWAY_CLIENT_SECRET} \${API_GATEWAY_SCOPE}' < /etc/nginx/conf.d/nginx_tmp.conf > /etc/nginx/conf.d/default.conf
rm -v /etc/nginx/conf.d/nginx_tmp.conf
exec nginx -g 'daemon off;'