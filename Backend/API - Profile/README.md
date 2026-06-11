# Junção Reddit/Git - Módulo de Perfil (API - Profile)

Este módulo, **API - Profile**, é um microsserviço dedicado ao gerenciamento de perfis de usuário para o projeto Junção Reddit/Git. Ele permite que usuários autenticados visualizem, criem, atualizem e gerenciem seus perfis, incluindo o upload de avatares.

## Tecnologias Utilizadas

O projeto é construído com as seguintes tecnologias principais:

*   **Java 21**: Linguagem de programação.
*   **Spring Boot 3.5.7**: Framework principal.
*   **Spring Data JPA**: Para persistência de dados.
*   **MySQL**: Banco de dados relacional.
*   **Spring Security & JWT**: Para autenticação e autorização.
*   **Maven**: Gerenciamento de dependências.
*   **Lombok**: Redução de código boilerplate.
*   **SpringDoc/Swagger**: Para documentação da API.

## Funcionalidades Implementadas

O módulo de Perfil oferece endpoints para gerenciamento privado e visualização pública de perfis:

### Endpoints Privados (Requerem JWT)

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `GET` | `/api/profiles/me` | Retorna o perfil completo do usuário autenticado. |
| `PUT` | `/api/profiles/me` | Atualiza os dados do perfil do usuário autenticado. |
| `POST` | `/api/profiles/me/avatar` | Faz o upload de um novo avatar (imagem) para o perfil. |
| `DELETE` | `/api/profiles/me/avatar` | Remove o avatar do perfil. |

### Endpoints Públicos

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `GET` | `/api/profiles/{username}` | Retorna o perfil público de um usuário, se não for privado. |
| `GET` | `/api/profiles/{username}/avatar` | Retorna a imagem do avatar de um usuário. |

## Estrutura do Projeto

O projeto segue a arquitetura de microsserviços, com foco na separação de responsabilidades:

```
API - Profile/
├── src/main/java/com/redgit/profile/
│   ├── controller/
│   │   └── ProfileController.java (Endpoints da API)
│   ├── infrastructure/
│   │   ├── entities/ (Modelos de Banco de Dados: Profile, User)
│   │   ├── repository/ (Interfaces de Repositório)
│   │   ├── security/ (Configurações de Segurança)
│   │   └── storage/ (Serviço de Upload de Arquivos)
│   └── service/ (Lógica de Negócio: ProfileService, TokenService)
└── src/main/resources/
    └── application.properties
```

## Configuração de Ambiente

O módulo de Perfil utiliza o MySQL para persistência de dados e requer a chave secreta do JWT para validação de tokens.

### 1. Variáveis de Ambiente Necessárias

O projeto espera as seguintes variáveis de ambiente (geralmente definidas em um arquivo `.env` na raiz do projeto principal):

```dotenv
DB_URL=jdbc:mysql://localhost:3306/mydatabase
DB_USERNAME=myuser
DB_PASSWORD=secret

JWT_SECRET=3246918694727278232479912314703835454208642542872406260685881546

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=secret
REDIS_DB=0
```

### 2. Arquivo de Propriedades (`application.properties`)

O arquivo `src/main/resources/application.properties` configura o Spring Boot para este módulo:

```properties
spring.application.name=profile-api

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
spring.jpa.show-sql=true

# Database
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/mydatabase}
spring.datasource.username=${DB_USERNAME:myuser}
spring.datasource.password=${DB_PASSWORD:secret}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Test
spring.test.database.replace=none

# Server
server.port=8083

# Security
security.jwt.secret-key=${JWT_SECRET:my-secret-key-from-digito}

# File Upload
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=2MB
file.upload-dir=uploads/avatars

# Redis Configuration
spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}
spring.data.redis.password=${REDIS_PASSWORD:}
spring.data.redis.database=${REDIS_DB:1}
spring.data.redis.timeout=60000

# Redis Connection Pool (Jedis)
spring.data.redis.jedis.pool.max-active=8
spring.data.redis.jedis.pool.max-idle=8
spring.data.redis.jedis.pool.min-idle=0
spring.data.redis.jedis.pool.max-wait=-1ms

# Redis Cache TTL (em segundos)
cache.profile.ttl=300
cache.avatar.ttl=600

# Logging
logging.level.com.redgit.profile=DEBUG
```

## Como Rodar o Projeto

### Pré-requisitos

*   **Java Development Kit (JDK) 21** ou superior.
*   **Maven**.
*   **Servidor MySQL** rodando.

### Passos para Execução

1.  **Clone o repositório e mude para a branch:**

    ```bash
    git clone https://github.com/Pradixx/Juncao-Reddit-Git.git
    cd Juncao-Reddit-Git
    git checkout Module-Profile
    ```

2.  **Navegue para o módulo:**

    ```bash
    cd "API - Profile"
    ```

3.  **Configure o ambiente:**
    *   Certifique-se de que as variáveis de ambiente necessárias estão configuradas (ou exportadas) no seu terminal.

4.  **Compile e execute a aplicação:**

    ```bash
    ./mvnw clean install
    ./mvnw spring-boot:run
    ```

A aplicação será iniciada na porta `8083`.

## Exemplo de Uso (Atualização de Perfil)

Assumindo que você já possui um JWT válido (obtido pelo módulo de Autenticação).

### 1. Atualizar Perfil

```bash
# Substitua SEU_TOKEN_JWT pelo token real
curl -X PUT http://localhost:8083/api/profiles/me \
-H "Content-Type: application/json" \
-H "Authorization: Bearer SEU_TOKEN_JWT" \
-d '{"username": "novo_username", "bio": "Minha nova biografia", "public": true}'
```

### 2. Upload de Avatar

O upload de arquivos requer o uso de `multipart/form-data`.

```bash
# Substitua SEU_TOKEN_JWT pelo token real e /caminho/para/sua/imagem.png pelo caminho do arquivo
curl -X POST http://localhost:8083/api/profiles/me/avatar \
-H "Authorization: Bearer SEU_TOKEN_JWT" \
-F "file=@/caminho/para/sua/imagem.png"
```

## Contribuições

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou pull requests no repositório.

## Licença

Este projeto está licenciado sob a licença MIT.

## Autor

**Diego Silva Prado e Daniel Macedo Silva**
