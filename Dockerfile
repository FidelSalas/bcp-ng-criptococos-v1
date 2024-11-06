FROM alpine:3.13.7
# Librerias requeridas
#TODO RUN apk update && apk --no-cache add gettext
RUN apk update
RUN apk add nginx-mod-http-headers-more
RUN apk add gettext
RUN mkdir -p /run/nginx/

ARG API_BCP_URL
ENV API_BCP_URL  "${API_BCP_URL}"
# Eliminar el archivo de configuracion de NGINX por defecto
RUN rm -v /etc/nginx/conf.d/default.conf

# Copiar el bundle generado a la ruta publica del NGINX
ADD ./dist/bcp-ng-criptococos-v1 /var/www/code

# Copiar el archivo de configuracion de NGINX
ADD ./devops/docker/nginx.webapp.conf /etc/nginx/conf.d/nginx_tmp.conf
ADD ./devops/docker/webapp_startup.sh /usr/bin/webapp_startup.sh

RUN ln -sf /dev/stdout /var/log/nginx/access.log \
	&& ln -sf /dev/stderr /var/log/nginx/error.log

RUN chmod +x /usr/bin/webapp_startup.sh
# Ejecutar el comando envsubst para reemplazar las variables de entorno en el archivo de configuracion del NGINX
# Iniciar el servicio de NGINX
CMD ["webapp_startup.sh"]

# Exponer el puerto especificado en la variable de entorno $WEB_APP_PORT
EXPOSE 80