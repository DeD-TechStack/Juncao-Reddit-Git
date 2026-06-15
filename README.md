# Junção Reddit/Git - Projeto de Estudo e Desenvolvimento

Este repositório, **Junção Reddit/Git**, é um projeto pessoal e de aprendizado contínuo, focado na aplicação prática de arquiteturas de software modernas, como **Microsserviços** e **APIs RESTful**, utilizando o ecossistema **Spring Boot** e **Java**.

O objetivo principal é simular a estrutura de uma plataforma social, como o Reddit, com foco inicial na construção de módulos de backend robustos e escaláveis.

## 💡 Foco em Aprendizado e Aperfeiçoamento

Este projeto é um **laboratório de estudos** onde as técnicas e as melhores práticas de desenvolvimento são constantemente aperfeiçoadas. A estrutura atual reflete a evolução do aprendizado, com a migração de bancos de dados relacionais para NoSQL e a adoção de padrões de segurança como JWT.

**Enfatizamos que o projeto está em desenvolvimento ativo e as branches refletem diferentes fases de estudo e reconfiguração.**

## Módulos Atuais

A branch `main` integra os seguintes módulos de backend, cada um focado em uma responsabilidade específica:

### 1. API - Auth (Autenticação)

*   **Tecnologia Principal:** Spring Boot, Spring Security, JWT, Redis.
*   **Banco de Dados:** MySQL (configurado via JPA).
*   **Funcionalidade:** Gerenciamento de usuários, registro (USER e ADMIN) e autenticação via JSON Web Tokens (JWT). Utiliza Redis para cache ou sessões.
*   **Endpoints Principais:** `/api/auth/register`, `/api/auth/login`.

### 2. API - Ideias Hub

*   **Tecnologia Principal:** Spring Boot, Spring Security, JWT.
*   **Banco de Dados:** MongoDB (NoSQL).
*   **Funcionalidade:** CRUD (Create, Read, Update, Delete) para o recurso "Ideia", permitindo que usuários autenticados criem e gerenciem suas propostas.
*   **Endpoints Principais:** `/api/ideas`.

### 3. API - Profile (Gerenciamento de Perfis)

*   **Tecnologia Principal:** Spring Boot, Spring Security, JWT, Redis.
*   **Banco de Dados:** MySQL (configurado via JPA).
*   **Funcionalidade:** Gerenciamento de perfis de usuário, incluindo visualização pública, atualização de dados e upload/remoção de avatares. Utiliza Redis para cache de perfis e avatares.
*   **Endpoints Principais:** `/api/profiles/me`, `/api/profiles/{username}`.

## 🛠️ Tecnologias Comuns

Os módulos compartilham um conjunto de tecnologias base:

*   **Linguagem:** Java 21
*   **Framework:** Spring Boot 3.x
*   **Segurança:** Spring Security e JWT (com `com.auth0:java-jwt`)
*   **Build Tool:** Maven
*   **Documentação:** SpringDoc/Swagger

## Próximos Passos (Roadmap)

O desenvolvimento futuro do projeto Junção Reddit/Git está focado em:

1.  **Desenvolvimento do Front-end com React:** Iniciar a construção de uma interface de usuário dinâmica e responsiva para consumir as APIs de backend.
2.  **Módulos Adicionais:** Implementar novos módulos de microsserviços para funcionalidades como:
    *   **Comentários e Votação:** Estrutura para interações sociais.
    *   **Notificações:** Sistema de alerta em tempo real.
3.  **Escalabilidade e Dinamismo:** Continuar a aperfeiçoar a arquitetura para garantir alta disponibilidade e desempenho, explorando tecnologias como **Spring Cloud** e **Docker Compose** para orquestração.

## Configuração de Ambiente

Devido à arquitetura de microsserviços, cada API possui sua própria configuração de ambiente e deve ser executada separadamente.

### 1. API - Auth

*   **Porta:** `8081`
*   **Banco de Dados:** MySQL
*   **Cache:** Redis
*   **Variáveis de Ambiente (`.env`):**

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

*   **Arquivo de Propriedades (`application.properties`):**

    ```properties
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
    server.port=8081

    # Security
    security.jwt.secret-key=${JWT_SECRET:my-secret-key-from-digito}

    # Redis Configuration
    spring.data.redis.host=${REDIS_HOST:localhost}
    spring.data.redis.port=${REDIS_PORT:6379}
    spring.data.redis.password=${REDIS_PASSWORD:}
    spring.data.redis.database=${REDIS_DB:0}
    spring.data.redis.timeout=60000
    ```

### 2. API - Ideias Hub

*   **Porta:** `8082`
*   **Banco de Dados:** MongoDB
*   **Variáveis de Ambiente (`.env`):**

    ```dotenv
    JWT_SECRET=3246918694727278232479912314703835454208642542872406260685881546
    ```

*   **Arquivo de Propriedades (`application.properties`):**

    ```properties
    spring.application.name=ideas-hub

    # MongoDB
    spring.config.import=mongo.properties

    # Server
    server.port=8082

    # JWT
    security.jwt.secret-key=${JWT_SECRET:my-secret-key-from-digito}
    ```

*   **Configuração do MongoDB (`mongo.properties`):** (Este arquivo deve ser criado em `src/main/resources/`)

    ```properties
    spring.data.mongodb.connection-string=mongodb+srv://digito:GXylmjKyw0fAVkm4@project-ideas-cluster.dcrxch1.mongodb.net/ideas-db?retryWrites=true&w=majority
mongodb.databaseName=ideas-db
    ```

### 3. API - Profile (Gerenciamento de Perfis)

*   **Porta:** `8083`
*   **Banco de Dados:** MySQL
*   **Cache:** Redis
*   **Variáveis de Ambiente (`.env`):**

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

*   **Arquivo de Propriedades (`application.properties`):**

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

**Nota de Segurança**: As chaves `JWT_SECRET`, as strings de conexão do banco de dados e as senhas do Redis fornecidas são apenas para fins de aprendizado e desenvolvimento. Em produção, as credenciais devem ser gerenciadas de forma segura e não expostas diretamente no código ou em arquivos de configuração versionados.

## Como Rodar o Projeto

### Pré-requisitos Gerais

*   **Java Development Kit (JDK) 21** ou superior.
*   **Maven**.
*   **Servidor MySQL** rodando (para os módulos de Autenticação e Perfil).
*   **Servidor Redis** rodando (para os módulos de Autenticação e Perfil).
*   **Acesso ao Cluster MongoDB** (ou um servidor MongoDB local para o módulo Ideias Hub).
*   **Leitor de `.env`** (para carregar variáveis de ambiente, ou exportar as variáveis manualmente).

### Passos para Execução de Cada Módulo

1.  **Clone o repositório:**

    ```bash
    git clone https://github.com/Pradixx/Juncao-Reddit-Git.git
    cd Juncao-Reddit-Git
    ```

2.  **Navegue para o diretório do módulo desejado:**

    *   Para Autenticação: `cd "API - Auth"`
    *   Para Ideias Hub: `cd "API - Ideias Hub"`
    *   Para Perfil: `cd "API - Profile"`

3.  **Configure o ambiente:**
    *   Crie e preencha o arquivo `.env` na raiz do repositório principal com as variáveis de ambiente necessárias para todos os módulos.
    *   Para o módulo Ideias Hub, crie e preencha o arquivo `src/main/resources/mongo.properties` dentro do diretório do módulo.

4.  **Compile e execute a aplicação:**

    ```bash
    ./mvnw clean install
    ./mvnw spring-boot:run
    ```

Cada aplicação será iniciada em sua respectiva porta (8081 para Autenticação, 8082 para Ideias Hub, 8083 para Perfil).

## Contribuições

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou pull requests no repositório.

## Licença

Este projeto está licenciado sob a licença MIT.

## Autor

**Diego Silva Prado e Daniel Macedo Silva**