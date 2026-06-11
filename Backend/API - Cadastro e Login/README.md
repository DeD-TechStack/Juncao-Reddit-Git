# Junção Reddit/Git - Módulo de Login e cadastro de usuários!

Este repositório contém o módulo inicial de **Autenticação e Registro** (Login e Register) para o projeto Junção Reddit/Git, desenvolvido com **Spring Boot** e **Spring Security**. Esta branch, `Login-Register-Alpha`, foca na implementação da segurança básica utilizando **JWT (JSON Web Tokens)** para controle de acesso.

## Tecnologias Utilizadas

O projeto é construído com as seguintes tecnologias principais:

*   **Java 21**: Linguagem de programação.
*   **Spring Boot 3.5.7**: Framework principal.
*   **Spring Security**: Para autenticação e autorização.
*   **JWT (Java-JWT)**: Para geração e validação de tokens de acesso.
*   **Spring Data JPA**: Para persistência de dados.
*   **MySQL**: Banco de dados relacional.
*   **Maven**: Gerenciamento de dependências.
*   **Lombok**: Redução de código boilerplate.
*   **SpringDoc/Swagger**: Para documentação da API (endpoints públicos).
*   **Postman (Todos os testes de requisição)**: https://documenter.getpostman.com/view/48435237/2sBXVZmtvc

## Funcionalidades Implementadas

Esta versão Alpha implementa os seguintes endpoints de autenticação:

| Método | Endpoint | Descrição | Acesso |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Autentica um usuário e retorna um JWT. | Público |
| `POST` | `/api/auth/register` | Cria um novo usuário com a role `USER`. | Público |
| `POST` | `/api/auth/register/admin` | Cria um novo usuário com a role `ADMIN`. **(Apenas para desenvolvimento)** | Público |
| `GET` | `/api/admin/stats` | Exemplo de rota protegida. Requer um JWT de um usuário com a role `ADMIN`. | Privado (ADMIN) |
| `PUT` | `/api/admin/role` | Exemplo de rota protegida. Requer um JWT de um usuário com a role `ADMIN`. | Privado (ADMIN) |
| `GET` | `/api/user/profile` | Exemplo de rota protegida. Requer um JWT de um usuário com a role `USER` ou `ADMIN`. | Privado (USER/ADMIN) |

## Estrutura do Projeto

O projeto segue a arquitetura de camadas, com foco na separação de responsabilidades:

```
src/main/java/com/digitodael/redgit/
├── controllers/
│   ├── AuthController.java (Login e Register)
│   └── DTO/ (Objetos de Transferência de Dados)
├── infrastructure/
│   ├── entity/ (Modelos de Banco de Dados: User, UserRole)
│   ├── repository/ (Interfaces de Repositório)
│   └── security/ (Configurações de Spring Security, JWT Filter)
└── service/ (Lógica de Negócio: TokenService, CustomUserDetailsService)
```

## Configuração de Ambiente

Para rodar o projeto, é necessário configurar as variáveis de ambiente e as propriedades do Spring.

### 1. Variáveis de Ambiente (`.env`)

O projeto utiliza variáveis de ambiente para as credenciais do banco de dados e a chave secreta do JWT. Crie um arquivo `.env` na raiz do projeto com o seguinte conteúdo:

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

**Nota de Segurança**: A chave `JWT_SECRET` deve ser longa e complexa. A chave fornecida é apenas para fins de aprendizado e desenvolvimento.

### 2. Arquivo de Propriedades (`application.properties`)

O arquivo `src/main/resources/application.properties` configura o Spring Boot para utilizar as variáveis de ambiente e define o comportamento do JPA/Hibernate.

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

## Como Rodar o Projeto

### Pré-requisitos

*   **Java Development Kit (JDK) 21** ou superior.
*   **Maven**.
*   **Servidor MySQL** rodando (ou Docker para rodar o MySQL).
*   **Leitor de `.env`** (como o plugin Spring Boot para carregar variáveis de ambiente, ou exportar as variáveis manualmente).

### Passos para Execução

1.  **Clone o repositório e mude para a branch:**

    ```bash
    git clone https://github.com/Pradixx/Juncao-Reddit-Git.git
    cd Juncao-Reddit-Git
    git checkout Login-Register-Alpha
    ```

2.  **Configure o ambiente:**
    *   Crie e preencha o arquivo `.env` conforme a seção acima.
    *   Certifique-se de que o banco de dados MySQL está acessível.

3.  **Compile e execute a aplicação:**

    ```bash
    ./mvnw clean install
    ./mvnw spring-boot:run
    ```

A aplicação será iniciada na porta `8081`.

## Exemplos de Uso (com `curl`)

Assumindo que a API está rodando em `http://localhost:8081`.

### 1. Registro de Usuário

```bash
curl -X POST http://localhost:8081/api/auth/register \
-H "Content-Type: application/json" \
-d '{"name": "usuario_teste", "email": "teste@email.com", "password": "senha_segura"}'
```

**Resposta de Sucesso:** Retorna o nome do usuário e o JWT.

```json
{
    "name": "usuario_teste",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 2. Login de Usuário

```bash
curl -X POST http://localhost:8081/api/auth/login \
-H "Content-Type: application/json" \
-d '{"email": "teste@email.com", "password": "senha_segura"}'
```

**Resposta de Sucesso:** Retorna o nome do usuário e um novo JWT.

### 3. Acesso a Rota Protegida (Exemplo)

Para acessar rotas protegidas, utilize o token JWT retornado no login no cabeçalho `Authorization`.

```bash
# Substitua SEU_TOKEN_JWT pelo token real
curl -X GET http://localhost:8081/api/user/profile \
-H "Authorization: Bearer SEU_TOKEN_JWT"
```

## Contribuições

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou pull requests no repositório.

## Licença

Este projeto está licenciado sob a licença MIT.

## Autor

**Diego Silva Prado e Daniel Macedo Silva**
