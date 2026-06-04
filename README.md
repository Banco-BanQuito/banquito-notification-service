# banquito-notification-service

Microservicio interno de notificaciones para BanQuito V2, responsable de enviar emails a beneficiarios cuando un pago fue procesado correctamente.

Este servicio pertenece a Anthony y forma parte del sistema Switch de Pagos Masivos.

## Que hace

- Expone un servicio gRPC interno para que otros microservicios soliciten el envio de emails.
- Mantiene un endpoint REST de prueba/compatibilidad para llamadas internas.
- Envia correos usando SMTP, pensado para Gmail con App Password.
- Registra auditoria en MongoDB `routingdb.beneficiary_notification`.
- Evita reenviar correos si ya existe una notificacion `ENVIADO` para el mismo `paymentDetailId`.
- Puede trabajar en modo simulado para desarrollo sin credenciales SMTP.

## Puertos

| Uso | Puerto | Descripcion |
| --- | --- | --- |
| REST interno | `8089` | Pruebas locales y compatibilidad |
| gRPC interno | `9099` | Comunicacion recomendada entre microservicios |

Importante: este servicio no debe exponerse en Kong. Solo lo consumen microservicios internos como `routing-service` o `report-service`.

## Comunicacion gRPC

Contrato:

```text
src/main/proto/notification.proto
```

Servicio:

```text
banquito.notification.v2.NotificationService/SendNotification
```

Request:

```json
{
  "paymentDetailId": "detalle-uuid-o-id",
  "emailTo": "beneficiario@correo.com",
  "subject": "Pago recibido - BanQuito",
  "bodyTemplate": "BENEFICIARY_PAYMENT",
  "variables": {
    "amount": "1000.00",
    "companyName": "Empresa ABC",
    "concept": "Nomina Mayo 2026",
    "date": "2026-05-30"
  }
}
```

Response:

```json
{
  "notificationId": "uuid",
  "status": "ENVIADO",
  "sentAt": "2026-05-30T14:04:00Z",
  "errorMessage": ""
}
```

Estados posibles:

- `ENVIADO`: correo enviado o ya existia auditoria enviada.
- `SIMULADO`: `SMTP_ENABLED=false`, no se envia correo real.
- `ERROR`: fallo SMTP u otro error al enviar.

## Endpoints REST internos

### Health

```http
GET http://localhost:8089/api/v2/notifications/health
```

Respuesta esperada:

```json
{
  "status": "UP",
  "service": "notification-service",
  "version": "2.0"
}
```

### Enviar notificacion de prueba

```http
POST http://localhost:8089/api/v2/notifications/send
Content-Type: application/json
```

Body:

```json
{
  "paymentDetailId": "detail-001",
  "emailTo": "beneficiario@correo.com",
  "subject": "Pago recibido - BanQuito",
  "bodyTemplate": "BENEFICIARY_PAYMENT",
  "variables": {
    "amount": "1000.00",
    "companyName": "Empresa ABC",
    "concept": "Nomina Mayo 2026",
    "date": "2026-05-30"
  }
}
```

## MongoDB

Usa la misma base `routingdb` del Switch.

Coleccion escrita:

```text
beneficiary_notification
```

Campos principales:

- `payment_detail_id`
- `email_to`
- `subject`
- `message_body`
- `status`
- `retry_count`
- `next_retry_at`
- `created_at`
- `sent_at`
- `error_message`

Si MongoDB no esta disponible, el servicio intenta enviar el correo de todas formas. La auditoria no debe bloquear el pago ni el envio.

## Variables de entorno

| Variable | Valor local recomendado | Valor Docker/infra | Descripcion |
| --- | --- | --- | --- |
| `SERVER_PORT` | `8089` | `8089` | Puerto REST |
| `GRPC_PORT` | `9099` | `9099` | Puerto gRPC |
| `MONGODB_URI` | `mongodb://localhost:27017/routingdb` | `mongodb://mongo:27017/routingdb` | Conexion a MongoDB |
| `SMTP_ENABLED` | `false` | `true` cuando haya credenciales | Activa envio real |
| `SMTP_HOST` | `smtp.gmail.com` | `smtp.gmail.com` | Host SMTP |
| `SMTP_PORT` | `587` | `587` | Puerto SMTP TLS |
| `SMTP_USER` | vacio o correo Gmail | correo Gmail | Usuario SMTP |
| `SMTP_PASS` | vacio o App Password | App Password | Password de aplicacion Gmail |
| `SMTP_FROM` | `no-reply@banquito.local` | correo remitente | Remitente |
| `NOTIFICATION_AUDIT_ENABLED` | `true` | `true` | Registrar auditoria en Mongo |

## Como levantar localmente

Requisitos:

- Java 21
- Maven 3.9+
- MongoDB si se quiere probar auditoria

Modo simulado, sin correo real:

```bash
mvn spring-boot:run
```

Modo Gmail real:

```bash
$env:SMTP_ENABLED="true"
$env:SMTP_USER="tu_correo@gmail.com"
$env:SMTP_PASS="tu_app_password"
$env:SMTP_FROM="tu_correo@gmail.com"
mvn spring-boot:run
```

Para Gmail se necesita un App Password, no la contrasena normal de la cuenta.

## Como levantar con Docker

Construir imagen:

```bash
docker build -t banquito-notification-service .
```

Ejecutar local con Mongo en la maquina:

```bash
docker run --rm -p 8089:8089 -p 9099:9099 ^
  -e MONGODB_URI=mongodb://host.docker.internal:27017/routingdb ^
  -e SMTP_ENABLED=false ^
  banquito-notification-service
```

En `banquito-infra`, este servicio debe usar:

```env
MONGODB_URI=mongodb://mongo:27017/routingdb
SMTP_ENABLED=true
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=correo@gmail.com
SMTP_PASS=app_password
SMTP_FROM=correo@gmail.com
NOTIFICATION_AUDIT_ENABLED=true
```

## Como lo consumen otros servicios

### routing-service de Paul

Cuando Paul procese un pago On-Us exitoso, puede llamar por gRPC a:

```text
notification-service:9099
```

Debe enviar `paymentDetailId`, email del beneficiario, asunto, plantilla y variables del pago.

### report-service de Anthony

`report-service` tambien consume este servicio por gRPC para notificar beneficiarios cuando genera comprobantes/reportes.

## Verificacion rapida

Compilar y correr tests:

```bash
mvn test
```

Probar health:

```bash
curl http://localhost:8089/api/v2/notifications/health
```

Probar envio simulado:

```bash
curl -X POST http://localhost:8089/api/v2/notifications/send ^
  -H "Content-Type: application/json" ^
  -d "{\"paymentDetailId\":\"detail-001\",\"emailTo\":\"test@example.com\",\"subject\":\"Pago recibido - BanQuito\",\"bodyTemplate\":\"BENEFICIARY_PAYMENT\",\"variables\":{\"amount\":\"1000.00\",\"companyName\":\"Empresa ABC\",\"concept\":\"Nomina\",\"date\":\"2026-05-30\"}}"
```

Con `SMTP_ENABLED=false`, la respuesta debe venir con `status: SIMULADO`.

## Notas para el equipo

- No agregar este servicio a Kong.
- No guardar credenciales SMTP en Git.
- Para pruebas locales usar `SMTP_ENABLED=false`.
- Para integracion real con Gmail usar App Password.
- La auditoria vive en `routingdb.beneficiary_notification`, para que Paul y Anthony puedan revisar el estado de envio.
