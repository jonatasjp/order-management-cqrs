# cqrs-command

Este módulo é responsável por toda a lógica de escrita (Command) do sistema, incluindo criação, alteração, confirmação e cancelamento de pedidos, além de publicação de eventos de domínio.

## Principais responsabilidades
- Expor endpoints REST para comandos
- Persistir dados no PostgreSQL
- Gerar e publicar eventos via Outbox Pattern
- Integrar com Kafka e SQS (LocalStack)

## Como rodar

### Opção 1: Via IDE (Recomendado para desenvolvimento)
```bash
# Execute CqrsCommandApplication.java na sua IDE
# Certifique-se de que a infraestrutura está rodando via docker-compose
```

### Opção 2: Build manual
```bash
./mvnw clean package
java -jar target/cqrs-command-0.0.1-SNAPSHOT.jar
```

### Opção 3: Docker Compose
```bash
# Execute o script da raiz do projeto
./start-local.sh
```

## Endpoints principais

- `POST /orders` - Criar pedido
- `POST /orders/{correlationId}/items` - Adicionar item
- `POST /orders/{correlationId}/confirm` - Confirmar pedido
- `POST /orders/{correlationId}/cancel` - Cancelar pedido
- `DELETE /orders/{correlationId}/items/{productId}` - Remover item

## Integrações
- **PostgreSQL**: Persistência dos dados
- **Kafka**: Publicação de eventos
- **SQS (LocalStack)**: Orquestração do processamento de eventos outbox

Consulte o README da raiz para detalhes de arquitetura e infraestrutura. 