# cqrs-query

Este módulo é responsável por toda a lógica de leitura (Query) do sistema, mantendo uma visão otimizada dos pedidos e itens a partir dos eventos publicados pelo Command.

## Principais responsabilidades
- Consumir eventos do Kafka
- Atualizar a base de leitura no MongoDB
- Expor endpoints REST para consulta de pedidos e eventos

## Como rodar

### Opção 1: Via IDE (Recomendado para desenvolvimento)
```bash
# Execute CqrsQueryApplication.java na sua IDE
# Certifique-se de que a infraestrutura está rodando via docker-compose
```

### Opção 2: Build manual
```bash
./mvnw clean package
java -jar target/cqrs-query-0.0.1-SNAPSHOT.jar
```

### Opção 3: Docker Compose
```bash
# Execute o script da raiz do projeto
./start-local.sh
```

## Endpoints principais

- `GET /orders/{correlationId}` - Consultar pedido consolidado
- `GET /admin/pending-events/pending` - Listar eventos pendentes
- `GET /admin/pending-events/pending/{correlationId}` - Listar eventos pendentes por pedido
- `GET /admin/processed-events` - Listar eventos processados
- `GET /admin/processed-events/{correlationId}` - Listar eventos processados por pedido
- `POST /admin/pending-events/process` - Processar todos os eventos pendentes

## Integrações
- **Kafka**: Consumo de eventos
- **MongoDB**: Persistência da visão de leitura

Consulte o README da raiz para detalhes de arquitetura e infraestrutura. 