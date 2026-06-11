# Juncão Reddit-Git — Backlog Consolidado

> Gerado em: 2026-06-03
> Cobertura: Sistema Principal · API Reputação · API Trending · Front-end
> Total: **31 tasks** | P0: 10 | P1: 16 | P2: 5

---

## Legenda

| Símbolo | Significado |
|---|---|
| 🔴 P0 | Crítico — deve ser resolvido antes de qualquer deploy |
| 🟡 P1 | Importante — resolve bugs e gaps funcionais relevantes |
| 🟢 P2 | Melhoria — qualidade, consistência, experiência |
| `[ ]` | Critério de aceite pendente |
| `[x]` | Critério de aceite concluído |

---

## SISTEMA PRINCIPAL

### 🔴 TASK-001 — Proteger endpoint de registro de admin
**Tipo:** security | **Prioridade:** P0
**Arquivo(s):**
- `API - Cadastro e Login/src/main/java/com/redgit/auth/config/SecurityConfig.java`
- `API - Cadastro e Login/src/main/java/com/redgit/auth/controller/AuthController.java`

**Descrição:**
`POST /api/auth/register/admin` está mapeado como rota pública. Qualquer usuário anônimo pode criar uma conta ADMIN.

**Critérios de aceite:**
- [ ] `POST /api/auth/register/admin` retorna 401 sem token
- [ ] Retorna 403 para usuário sem role ADMIN
- [ ] Usuário ADMIN autenticado consegue criar outro ADMIN
- [ ] Nenhum endpoint público permite criação de conta privilegiada
- [ ] Teste de integração cobre os três cenários

**Implementação:**
```java
// SecurityConfig.java
.requestMatchers("/api/auth/register/admin").hasRole("ADMIN")

// AuthController.java
@PostMapping("/register/admin")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<UserDTO> registerAdmin(@RequestBody @Valid RegisterRequestDTO dto) { ... }
```

**Commit:** `security(auth): restringir criação de admin a usuários com role ADMIN`

---

### 🔴 TASK-002 — Corrigir `CustomUserDetailsService` para carregar authorities do banco
**Tipo:** security | **Prioridade:** P0
**Arquivo(s):**
- `API - Cadastro e Login/src/main/java/com/redgit/auth/service/CustomUserDetailsService.java`

**Descrição:**
`loadUserByUsername` retorna `new ArrayList<>()` como authorities. O Spring Security nunca enxerga as roles do usuário — `@PreAuthorize("hasRole('ADMIN')")` pode falhar silenciosamente.

**Critérios de aceite:**
- [ ] `loadUserByUsername` retorna `UserDetails` com `GrantedAuthority` do banco
- [ ] `GET /api/admin/users` retorna 403 para usuário com role USER
- [ ] `GET /api/admin/users` retorna 200 para usuário com role ADMIN
- [ ] Login de usuário comum não concede acesso a rotas de admin

**Implementação:**
```java
List<GrantedAuthority> authorities = List.of(
    new SimpleGrantedAuthority(user.getRole().name()) // ex: "ROLE_ADMIN"
);
return new org.springframework.security.core.userdetails.User(
    user.getEmail(), user.getPassword(),
    user.isEnabled(), true, true, user.isAccountNonLocked(),
    authorities
);
```

> Verificar se `UserRole` já usa prefixo `ROLE_`. Se não, usar `"ROLE_" + user.getRole().name()`.

**Commit:** `security(auth): carregar authorities do banco no CustomUserDetailsService`

---

### 🔴 TASK-003 — Remover fallback hardcoded do JWT secret
**Tipo:** security | **Prioridade:** P0
**Arquivo(s):**
- `API - Cadastro e Login/src/main/resources/application.properties`
- `API - Ideias Hub/src/main/resources/application.properties`
- `API - Profile/src/main/resources/application.properties`
- `API - Cadastro e Login/src/main/java/com/redgit/auth/service/TokenService.java`

**Descrição:**
`${JWT_SECRET:my-secret-key-from-digito}` expõe um segredo padrão no código-fonte. Qualquer pessoa com acesso ao repositório pode forjar tokens JWT válidos.

**Critérios de aceite:**
- [ ] Nenhum `application.properties` contém valor padrão para `JWT_SECRET`
- [ ] Aplicação falha no startup com mensagem clara se `JWT_SECRET` não estiver definido
- [ ] Os três serviços aplicam a mesma política
- [ ] `.env.example` documenta a variável obrigatória

**Implementação:**
```properties
jwt.secret=${JWT_SECRET}
```
```java
@PostConstruct
private void validateSecret() {
    if (secret == null || secret.isBlank() || secret.length() < 32)
        throw new IllegalStateException("JWT_SECRET não configurado ou inválido.");
}
```

**Commit:** `security(auth): remover fallback hardcoded do JWT_SECRET e validar no startup`

---

### 🔴 TASK-003-B — Remover `application.properties` do rastreamento git
**Tipo:** security | **Prioridade:** P0
**Arquivo(s):**
- `.gitignore` dos três serviços
- `application.properties` dos três serviços

**Descrição:**
Os três `application.properties` estão commitados com URL de banco, porta Redis e fallback de JWT. Devem ser removidos do rastreamento e substituídos por templates.

**Critérios de aceite:**
- [ ] `git ls-files | grep application.properties` não retorna resultados
- [ ] Existe `application.properties.example` com placeholders em cada serviço
- [ ] `.gitignore` ignora `application.properties`
- [ ] Todas as credenciais sensíveis usam `${VAR_NAME}` sem fallback

**Implementação:**
```bash
git rm --cached "API - Cadastro e Login/API - Cadastro e Login/src/main/resources/application.properties"
git rm --cached "API - Ideias Hub/API - Ideias Hub/src/main/resources/application.properties"
git rm --cached "API - Profile/API - Profile/src/main/resources/application.properties"
```
Adicionar ao `.gitignore` de cada serviço:
```gitignore
src/main/resources/application.properties
uploads/
*.p12
*.jks
*.pem
*.key
```

**Commit:** `security(config): remover application.properties do git e adicionar ao gitignore`

---

### 🔴 TASK-004 — Corrigir CORS nas APIs de Ideias e Perfil
**Tipo:** security | **Prioridade:** P0
**Arquivo(s):**
- `API - Ideias Hub/src/main/java/com/redgit/ideas/config/SecurityConfig.java`
- `API - Profile/src/main/java/com/redgit/profile/config/SecurityConfig.java`
- Respectivos `application.properties`

**Descrição:**
Ideas Hub e Profile usam `allowedOriginPatterns("*")`. A configuração aberta com `allowCredentials(true)` permite que qualquer origem faça requisições autenticadas.

**Critérios de aceite:**
- [ ] Nenhum serviço usa `allowedOriginPatterns("*")`
- [ ] Origem lida de `${CORS_ALLOWED_ORIGINS}` em todos os serviços
- [ ] Requisição de origem não autorizada recebe 403
- [ ] `.env.example` documenta a variável

**Implementação:**
```java
@Value("${cors.allowed-origins}")
private String allowedOrigins;

config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
```
```properties
cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:5173}
```

**Commit:** `security(cors): restringir origens permitidas via variável de ambiente`

---

### 🟡 TASK-005 — Adicionar validação de tamanho e sanitização HTML no Ideas Hub
**Tipo:** security | **Prioridade:** P1
**Arquivo(s):**
- `API - Ideias Hub/src/main/java/com/redgit/ideas/dto/IdeaDTO.java`
- `API - Ideias Hub/src/main/java/com/redgit/ideas/validation/NoHtml.java` *(criar)*
- `API - Ideias Hub/src/main/java/com/redgit/ideas/validation/NoHtmlValidator.java` *(criar)*

**Descrição:**
`IdeaDTO` tem apenas `@NotBlank` — sem limite de tamanho nem sanitização XSS. Profile API já possui `@NoHtml`; replicar no Ideas Hub.

**Critérios de aceite:**
- [ ] `title` aceita no máximo 200 caracteres
- [ ] `description` aceita no máximo 5000 caracteres
- [ ] Payload com `<script>` retorna 400
- [ ] Payload com `javascript:` ou `onerror=` retorna 400
- [ ] Payload válido continua sendo aceito

**Implementação:**
```java
@NotBlank @Size(max = 200) @NoHtml
private String title;

@NotBlank @Size(max = 5000) @NoHtml
private String description;
```

**Commit:** `security(ideas): adicionar validação de tamanho e sanitização HTML no IdeaDTO`

---

### 🟡 TASK-006 — Remover `authorId` do `IdeaDTO` e criar `IdeaCreateDTO`
**Tipo:** security | **Prioridade:** P1
**Arquivo(s):**
- `API - Ideias Hub/src/main/java/com/redgit/ideas/dto/IdeaDTO.java`
- `API - Ideias Hub/src/main/java/com/redgit/ideas/dto/IdeaCreateDTO.java` *(criar)*
- `API - Ideias Hub/src/main/java/com/redgit/ideas/controller/IdeaController.java`
- `API - Ideias Hub/src/main/java/com/redgit/ideas/service/IdeaService.java`

**Descrição:**
`authorId` público no DTO permite spoofing de autoria. Separar DTO de criação (sem `authorId`) do DTO de resposta.

**Critérios de aceite:**
- [ ] `POST /api/ideas` não aceita `authorId` no body
- [ ] `authorId` na ideia salva sempre corresponde ao email do token JWT
- [ ] `GET /api/ideas/{id}` continua retornando `authorId`
- [ ] PUT e PATCH não permitem alterar `authorId`

**Implementação:**
```java
public record IdeaCreateDTO(
    @NotBlank @Size(max = 200) @NoHtml String title,
    @NotBlank @Size(max = 5000) @NoHtml String description
) {}
```

**Commit:** `refactor(ideas): separar IdeaCreateDTO para impedir spoofing de authorId`

---

### 🟡 TASK-007 — Adicionar paginação em `getAllIdeas`
**Tipo:** refactor | **Prioridade:** P1
**Arquivo(s):**
- `API - Ideias Hub/src/main/java/com/redgit/ideas/service/IdeaService.java`
- `API - Ideias Hub/src/main/java/com/redgit/ideas/controller/IdeaController.java`

**Descrição:**
`findAll()` sem paginação carrega todos os documentos MongoDB em memória — risco de OOM e DoS com crescimento de dados.

**Critérios de aceite:**
- [ ] `GET /api/ideas` aceita `page` (default 0) e `size` (default 20, máximo 100)
- [ ] Resposta retorna envelope com `content`, `totalElements`, `totalPages`, `number`
- [ ] `GET /api/ideas/my-ideas` também paginado
- [ ] `size` acima de 100 é limitado automaticamente

**Implementação:**
```java
@Bean
public PageableHandlerMethodArgumentResolverCustomizer pagingCustomizer() {
    return resolver -> resolver.setMaxPageSize(100);
}

@GetMapping
public ResponseEntity<Page<IdeaResponseDTO>> listAll(
        @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(ideaService.getAllIdeas(pageable));
}
```

**Commit:** `refactor(ideas): adicionar paginação em getAllIdeas e getIdeasByAuthor`

---

### 🟡 TASK-008 — Substituir `RuntimeException` por `ResponseStatusException` no `IdeaService`
**Tipo:** bugfix | **Prioridade:** P1
**Arquivo(s):**
- `API - Ideias Hub/src/main/java/com/redgit/ideas/service/IdeaService.java`

**Descrição:**
`findById` lança `RuntimeException` genérica que não é capturada como 404 — resulta em HTTP 500 para recursos inexistentes.

**Critérios de aceite:**
- [ ] `GET /api/ideas/{id}` com ID inexistente retorna 404
- [ ] Body segue o padrão `ErrorResponse` do projeto
- [ ] Nenhum `RuntimeException` genérico permanece no `IdeaService`
- [ ] `replaceIdea`, `updateIdea` e `deleteIdeaById` usam o mesmo padrão

**Implementação:**
```java
private Idea findEntityById(String id) {
    return ideaRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Ideia não encontrada: " + id));
}
```

**Commit:** `fix(ideas): retornar HTTP 404 para ideias inexistentes no IdeaService`

---

### 🟡 TASK-009 — Corrigir validação de path traversal no `FileStorageService`
**Tipo:** security | **Prioridade:** P1
**Arquivo(s):**
- `API - Profile/src/main/java/com/redgit/profile/service/FileStorageService.java`

**Descrição:**
Verificação de `".."` por string pode ser bypassed por encodings como `%2e%2e`. Usar API `Path` do Java para validação canônica.

**Critérios de aceite:**
- [ ] Upload com `..` no filename retorna 400
- [ ] Upload com `%2e%2e` retorna 400
- [ ] Upload válido funciona normalmente
- [ ] Checagem via comparação canônica de `Path`

**Implementação:**
```java
Path targetPath = uploadDir.resolve(safeFilename).normalize();
if (!targetPath.startsWith(uploadDir)) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Caminho inválido");
}
```

**Commit:** `security(profile): substituir checagem de path traversal por validação canônica de Path`

---

### 🟢 TASK-010 — Alinhar versão do Spring Boot para 3.5.7 no Ideas Hub
**Tipo:** refactor | **Prioridade:** P2
**Arquivo(s):**
- `API - Ideias Hub/API - Ideias Hub/pom.xml`

**Descrição:**
Ideas Hub usa Spring Boot 3.5.1 enquanto Auth e Profile usam 3.5.7. Patches de segurança das versões intermediárias estão ausentes.

**Critérios de aceite:**
- [ ] `pom.xml` declara Spring Boot 3.5.7
- [ ] Build conclui sem erros
- [ ] Endpoints respondem corretamente após upgrade

**Commit:** `refactor(ideas): atualizar Spring Boot de 3.5.1 para 3.5.7`

---

### 🟢 TASK-011 — Adicionar audit logging para operações sensíveis
**Tipo:** refactor | **Prioridade:** P2
**Arquivo(s):**
- `API - Cadastro e Login/src/main/java/com/redgit/auth/controller/AdminController.java`
- `API - Ideias Hub/src/main/java/com/redgit/ideas/controller/IdeaController.java`
- `API - Profile/src/main/java/com/redgit/profile/controller/ProfileController.java`

**Descrição:**
Operações destrutivas (deleção, troca de role, lock/unlock) não geram log de auditoria. Impossível investigar incidentes.

**Critérios de aceite:**
- [ ] Cada operação sensível gera log `INFO` com: `[AUDIT] ação | executadoPor | alvoId | timestamp`
- [ ] Logs de auditoria distinguíveis pelo prefixo `[AUDIT]`
- [ ] Nenhum dado sensível (senha, token) nos logs

**Implementação:**
```java
private static final Logger audit = LoggerFactory.getLogger("AUDIT");

audit.info("[AUDIT] DELETE_USER | executadoPor={} | alvo={} | ts={}",
    admin.getUsername(), id, Instant.now());
```

**Commit:** `refactor(audit): adicionar logs de auditoria em operações sensíveis`

---

## API DE REPUTAÇÃO

### 🔴 TASK-R01 — Remover JWT secret hardcoded da API de Reputação
**Tipo:** security | **Prioridade:** P0
**Arquivo(s):**
- `API - Reputation/src/main/resources/application.properties`
- `API - Reputation/src/main/java/.../security/TokenService.java`

**Descrição:**
`security.jwt.secret-key` contém valor literal commitado publicamente no GitHub. Qualquer pessoa pode forjar tokens JWT válidos.

**Critérios de aceite:**
- [ ] `application.properties` não contém valor literal para o secret
- [ ] Startup falha com mensagem clara se `SECURITY_JWT_SECRET_KEY` não estiver definida
- [ ] `application.properties` no `.gitignore`
- [ ] `application.properties.example` com placeholder

**Implementação:**
```properties
security.jwt.secret-key=${SECURITY_JWT_SECRET_KEY}
```
```java
@PostConstruct
private void validateSecret() {
    if (secret == null || secret.isBlank() || secret.length() < 32)
        throw new IllegalStateException("SECURITY_JWT_SECRET_KEY inválido ou ausente.");
}
```

**Commit:** `security(reputation): remover JWT secret hardcoded e exigir variável de ambiente`

---

### 🔴 TASK-R02 — Proteger endpoints de eventos XP contra chamadas diretas do cliente
**Tipo:** security | **Prioridade:** P0
**Arquivo(s):**
- `API - Reputation/src/main/java/.../controller/ReputationController.java`
- `API - Reputation/src/main/java/.../security/SecurityConfig.java`
- `API - Reputation/src/main/java/.../security/ServiceTokenFilter.java` *(criar)*

**Descrição:**
Qualquer usuário autenticado pode chamar `POST /reputation/events/IDEA_TRENDING` e ganhar +100 XP ilimitadamente. Eventos devem ser originados apenas por serviços internos.

**Critérios de aceite:**
- [ ] `POST /reputation/events/**` retorna 403 para tokens JWT de usuário comum
- [ ] Endpoint aceita apenas requisições com header `X-Service-Token` válido
- [ ] Token inválido retorna 401
- [ ] `GET /reputation/me` continua funcionando para usuários

**Implementação:**
```java
@Component
public class ServiceTokenFilter extends OncePerRequestFilter {
    @Value("${internal.service.secret}")
    private String serviceSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
            HttpServletResponse res, FilterChain chain) throws ... {
        if (req.getRequestURI().startsWith("/reputation/events")) {
            String token = req.getHeader("X-Service-Token");
            if (!serviceSecret.equals(token)) {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Service token inválido");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
```

**Commit:** `security(reputation): restringir endpoints de eventos XP a service tokens internos`

---

### 🟡 TASK-R03 — Corrigir race condition no acúmulo de XP
**Tipo:** bugfix | **Prioridade:** P1
**Arquivo(s):**
- `API - Reputation/src/main/java/.../infrastructure/entity/UserReputation.java`
- `API - Reputation/src/main/java/.../service/ReputationService.java`
- `API - Reputation/pom.xml`

**Descrição:**
`applyEvent` faz read-modify-write sem lock otimista. Dois eventos simultâneos para o mesmo usuário podem resultar em perda silenciosa de XP.

**Critérios de aceite:**
- [ ] Dois eventos simultâneos resultam em XP correto (soma dos dois)
- [ ] `OptimisticLockException` capturada com retry transparente (máx 3 tentativas)
- [ ] `spring-retry` adicionado ao `pom.xml`

**Implementação:**
```java
// UserReputation.java
@Version
private Long version;

// ReputationService.java
@Transactional
@Retryable(retryFor = OptimisticLockException.class, maxAttempts = 3)
public ReputationEventResponseDTO applyEvent(...) { ... }
```

**Commit:** `fix(reputation): adicionar lock otimista para prevenir race condition no XP`

---

### 🟡 TASK-R04 — Substituir `userEmail` por `userId` como chave de reputação
**Tipo:** refactor | **Prioridade:** P1
**Arquivo(s):**
- `API - Reputation/src/main/java/.../infrastructure/entity/UserReputation.java`
- `API - Reputation/src/main/java/.../service/ReputationService.java`
- `API - Reputation/src/main/java/.../dto/ReputationResponseDTO.java`

**Descrição:**
Reputação indexada por e-mail é frágil. Troca de e-mail na Auth API cria orphaned records. Migrar para `userId` (UUID) como chave principal.

> **[DÚVIDA resolvida]:** Verificar se o `sub` do JWT atual é o e-mail ou o UUID antes de implementar. Se for e-mail, a Auth API precisa ser ajustada para incluir o UUID no `sub` ou em claim customizado.

**Critérios de aceite:**
- [ ] `UserReputation` usa `userId` (String/UUID) como campo único
- [ ] `ReputationResponseDTO` não expõe `userEmail`
- [ ] Auth API emite JWT com `userId` no `sub`

**Commit:** `refactor(reputation): substituir userEmail por userId como chave de reputação`

---

### 🟡 TASK-R05 — Criar `GlobalExceptionHandler` na API de Reputação
**Tipo:** refactor | **Prioridade:** P1
**Arquivo(s):**
- `API - Reputation/src/main/java/.../exception/GlobalExceptionHandler.java` *(criar)*

**Descrição:**
Sem `@ControllerAdvice`, erros não tratados expõem stack traces via HTTP 500.

**Critérios de aceite:**
- [ ] Recurso inexistente retorna 404 com `ErrorResponse` padronizado
- [ ] Validação falha retorna 400 com mapa de campos
- [ ] Erro inesperado retorna 500 com mensagem genérica (sem stack trace)

**Commit:** `refactor(reputation): adicionar GlobalExceptionHandler`

---

### 🟢 TASK-R06 — Normalizar contrato REST do endpoint de eventos
**Tipo:** refactor | **Prioridade:** P2
**Arquivo(s):**
- `API - Reputation/src/main/java/.../controller/ReputationController.java`
- `API - Reputation/src/main/java/.../dto/ReputationEventRequestDTO.java` *(criar)*

**Descrição:**
`POST /reputation/events/{type}` sem body impossibilita evolução do contrato. Mover tipo para body com DTO que suporta metadados futuros.

**Critérios de aceite:**
- [ ] `POST /reputation/events` aceita `{ "type": "LIKE_GAINED", "referenceId": "uuid-opcional" }`
- [ ] `type` inválido retorna 400
- [ ] Resposta mantém formato de `ReputationEventResponseDTO`

**Commit:** `refactor(reputation): normalizar endpoint de eventos para aceitar body com DTO`

---

## API DE TRENDING

### 🔴 TASK-T01 — Remover JWT secret hardcoded da API de Trending
**Tipo:** security | **Prioridade:** P0
**Arquivo(s):**
- `API - Trending/src/main/resources/application.properties`
- `API - Trending/src/main/resources/application-docker.properties`
- `API - Trending/src/main/java/.../security/TokenService.java`

**Descrição:**
Ambos os arquivos contêm o JWT secret como fallback literal — mesmo valor da API de Reputação, exposto publicamente.

**Critérios de aceite:**
- [ ] Nenhum arquivo `.properties` contém valor literal para o secret
- [ ] Startup falha se `SECURITY_JWT_SECRET_KEY` não estiver definida
- [ ] `application.properties` no `.gitignore`
- [ ] `application.properties.example` com placeholder

**Commit:** `security(trending): remover JWT secret hardcoded e exigir variável de ambiente`

---

### 🔴 TASK-T02 — Proteger endpoints de bump contra manipulação direta pelo cliente
**Tipo:** security | **Prioridade:** P0
**Arquivo(s):**
- `API - Trending/src/main/java/.../controller/TrendingController.java`
- `API - Trending/src/main/java/.../security/SecurityConfig.java`
- `API - Trending/src/main/java/.../security/ServiceTokenFilter.java` *(criar)*

**Descrição:**
`POST /trending/ideas/{id}/like` e `/score` permitem que qualquer usuário autenticado manipule o score de qualquer ideia, inflando trending artificialmente.

**Critérios de aceite:**
- [ ] `POST /trending/**` retorna 401 sem header `X-Service-Token` válido
- [ ] `GET /trending/daily` e `/weekly` continuam públicos
- [ ] Service token configurado via `${INTERNAL_SERVICE_SECRET}`

**Commit:** `security(trending): restringir endpoints de bump a service tokens internos`

---

### 🟡 TASK-T03 — Rejeitar `delta` negativo ou zero no endpoint de score
**Tipo:** bugfix | **Prioridade:** P1
**Arquivo(s):**
- `API - Trending/src/main/java/.../controller/TrendingController.java`

**Descrição:**
`delta` negativo permite sabotagem de ideias concorrentes decrementando seu score.

**Critérios de aceite:**
- [ ] `delta <= 0` retorna 400 com mensagem clara
- [ ] `delta` positivo funciona normalmente
- [ ] `POST .../like` não é afetado

**Implementação:**
```java
@RequestParam @Positive(message = "delta deve ser positivo") double delta
```

**Commit:** `fix(trending): rejeitar delta negativo ou zero no endpoint de score`

---

### 🟡 TASK-T04 — Fixar timezone UTC e WeekFields.ISO no `TrendingKeys`
**Tipo:** bugfix | **Prioridade:** P1
**Arquivo(s):**
- `API - Trending/src/main/java/.../util/TrendingKeys.java`

**Descrição:**
`ZoneId.systemDefault()` e `Locale.getDefault()` tornam chaves Redis dependentes do servidor. Dev local vs Docker geram chaves diferentes para o mesmo instante.

**Critérios de aceite:**
- [ ] `zone()` retorna sempre `ZoneId.of("UTC")`
- [ ] `weekly()` usa `WeekFields.ISO`
- [ ] Chaves idênticas em qualquer ambiente para o mesmo instante

**Implementação:**
```java
public static ZoneId zone() { return ZoneId.of("UTC"); }

public static String weekly(LocalDate date) {
    int week = date.get(WeekFields.ISO.weekOfWeekBasedYear());
    int year = date.get(WeekFields.ISO.weekBasedYear());
    return String.format("trending:ideas:week:%dW%02d", year, week);
}
```

**Commit:** `fix(trending): fixar timezone UTC e WeekFields.ISO no TrendingKeys`

---

### 🟡 TASK-T05 — Adicionar CORS configuration na API de Trending
**Tipo:** bugfix | **Prioridade:** P1
**Arquivo(s):**
- `API - Trending/src/main/java/.../config/CorsConfig.java` *(criar)*
- `API - Trending/src/main/resources/application.properties`

**Descrição:**
Sem `CorsConfigurationSource` bean o comportamento cross-origin é imprevisível. Frontend pode ser bloqueado em produção.

**Critérios de aceite:**
- [ ] Requisição do frontend recebe headers CORS corretos
- [ ] Origem não autorizada recebe 403
- [ ] Origem configurada via `${CORS_ALLOWED_ORIGINS}`

**Commit:** `fix(trending): adicionar configuração CORS com origem via variável de ambiente`

---

### 🟡 TASK-T06 — Adicionar authorities no `SecurityFilter` da API de Trending
**Tipo:** bugfix | **Prioridade:** P1
**Arquivo(s):**
- `API - Trending/src/main/java/.../security/SecurityFilter.java`

**Descrição:**
`new UsernamePasswordAuthenticationToken(subject, null, List.of())` — authorities vazia. Qualquer `@PreAuthorize` futuro não funcionará.

**Critérios de aceite:**
- [ ] Token válido resulta em autenticação com `ROLE_USER`
- [ ] `@PreAuthorize("hasRole('USER')")` funciona corretamente

**Implementação:**
```java
new UsernamePasswordAuthenticationToken(
    subject, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
```

**Commit:** `fix(trending): adicionar ROLE_USER nas authorities do SecurityFilter`

---

### 🟢 TASK-T07 — Adotar `TrendingResponseDTO` como envelope de resposta
**Tipo:** refactor | **Prioridade:** P2
**Arquivo(s):**
- `API - Trending/src/main/java/.../dto/TrendingResponseDTO.java`
- `API - Trending/src/main/java/.../controller/TrendingController.java`

**Descrição:**
`TrendingResponseDTO` existe mas não é usado (dead code). Adotar como envelope com metadados (`period`, `generatedAt`) melhora a UX da API.

**Critérios de aceite:**
- [ ] `GET /trending/daily` e `/weekly` retornam `TrendingResponseDTO`
- [ ] DTO inclui `period`, `reference` (data), `generatedAt` e `items`
- [ ] Testes do controller atualizados

**Commit:** `refactor(trending): adotar TrendingResponseDTO como envelope de resposta`

---

### 🟢 TASK-T08 — Alinhar versão da lib JWT para 4.5.0 em todos os serviços
**Tipo:** refactor | **Prioridade:** P2
**Arquivo(s):**
- `API - Reputation/pom.xml` (4.4.0 → 4.5.0)

**Descrição:**
Reputation usa `java-jwt:4.4.0`; Trending usa `4.5.0`. Divergência desnecessária.

**Critérios de aceite:**
- [ ] Todos os serviços usam `java-jwt:4.5.0`
- [ ] Build sem erros após atualização

**Commit:** `refactor(reputation): alinhar java-jwt para 4.5.0`

---

## FRONT-END

### 🔴 TASK-F01 — Externalizar URLs de API para variáveis de ambiente
**Tipo:** security | **Prioridade:** P0
**Arquivo(s):**
- `src/api.js`
- `src/contexts/AuthContext.tsx`
- `src/contexts/IdeasContext.tsx`
- `.env.example` *(criar)*

**Descrição:**
URLs hardcoded (`http://localhost:8081`, `8082`) impedem qualquer deploy sem editar código-fonte.

**Critérios de aceite:**
- [ ] Nenhuma URL hardcoded no código-fonte
- [ ] `.env.example` documenta as três variáveis
- [ ] `.env` no `.gitignore`
- [ ] Build de produção usa variáveis corretamente

**Implementação:**
```ts
// src/lib/apiConfig.ts
export const AUTH_API = import.meta.env.VITE_AUTH_API_URL;
export const IDEAS_API = import.meta.env.VITE_IDEAS_API_URL;
export const PROFILE_API = import.meta.env.VITE_PROFILE_API_URL;
```

**Commit:** `security(front): externalizar URLs de API para variáveis de ambiente Vite`

---

### 🟡 TASK-F02 — Corrigir logout para blacklistar token no backend
**Tipo:** bugfix | **Prioridade:** P1
**Arquivo(s):**
- `src/contexts/AuthContext.tsx`

**Descrição:**
`logout()` só limpa localStorage — não chama `POST /api/auth/logout`. Token permanece válido no Redis após logout.

**Critérios de aceite:**
- [ ] `logout()` chama `POST /api/auth/logout` com token antes de limpar localStorage
- [ ] Falha na chamada não impede logout local
- [ ] Token invalidado no backend após logout

**Implementação:**
```ts
const logout = async () => {
  try {
    await fetch(`${AUTH_API}/auth/logout`, {
      method: "POST",
      headers: { Authorization: `Bearer ${token}` },
    });
  } finally {
    localStorage.removeItem("user");
    localStorage.removeItem("token");
    setUser(null);
    setToken(null);
  }
};
```

**Commit:** `fix(front): chamar endpoint de logout no backend para blacklistar token`

---

### 🟡 TASK-F03 — Adicionar dialog de confirmação antes de deletar ideia
**Tipo:** bugfix | **Prioridade:** P1
**Arquivo(s):**
- `src/components/IdeaCard.tsx`
- `src/pages/EditIdeaPage.tsx`

**Descrição:**
"Excluir" dispara deleção imediata sem confirmação. Ação irreversível por clique acidental.

**Critérios de aceite:**
- [ ] Clicar em "Excluir" abre `AlertDialog` do Radix UI
- [ ] Deleção só ocorre após confirmação no dialog
- [ ] Cancelar fecha o dialog sem deletar

**Commit:** `fix(front): adicionar confirmação antes de deletar ideia`

---

### 🟡 TASK-F04 — Implementar integração completa com Profile API
**Tipo:** feature | **Prioridade:** P1
**Arquivo(s):**
- `src/contexts/ProfileContext.tsx` *(criar)*
- `src/pages/ProfilePage.tsx` *(refatorar)*
- `src/pages/PublicProfilePage.tsx` *(criar)*
- `src/app/App.tsx`

**Descrição:**
ProfilePage só exibe dados cacheados do AuthContext. Todos os 6 endpoints da Profile API estão ausentes no frontend.

**Critérios de aceite:**
- [ ] `ProfilePage` exibe e edita: `username`, `displayName`, `bio`, `location`, `website`, `socialLinks`, `isPublic`
- [ ] Upload e remoção de avatar funcionais
- [ ] Rota `/profiles/:username` exibe perfil público
- [ ] Avatar exibido via `GET /api/profiles/{username}/avatar`

**Commit:** `feat(front): implementar integração completa com Profile API`

---

### 🟡 TASK-F05 — Corrigir `ViewIdeaPage` para buscar ideia diretamente da API
**Tipo:** bugfix | **Prioridade:** P1
**Arquivo(s):**
- `src/pages/ViewIdeaPage.tsx`

**Descrição:**
Ideia é buscada apenas do contexto em memória. URL direta ou refresh da página quebra a visualização.

**Critérios de aceite:**
- [ ] Acessar `/view-idea/:id` diretamente retorna a ideia corretamente
- [ ] Reload da página funciona
- [ ] Loading state exibido durante fetch
- [ ] 404 retorna mensagem adequada

**Commit:** `fix(front): buscar ideia diretamente da API em ViewIdeaPage`

---

### 🟡 TASK-F06 — Corrigir `lang` do HTML e adicionar títulos dinâmicos por rota
**Tipo:** bugfix | **Prioridade:** P1
**Arquivo(s):**
- `index.html`
- `src/app/App.tsx`

**Descrição:**
`<html lang="en">` com conteúdo em português. Título da página nunca muda. Falhas WCAG AA 2.4.2 e 3.1.1.

**Critérios de aceite:**
- [ ] `<html lang="pt-BR">` em `index.html`
- [ ] Cada rota atualiza `document.title` com nome descritivo
- [ ] WCAG 2.4.2 e 3.1.1 corrigidas

**Commit:** `fix(front): corrigir lang para pt-BR e adicionar títulos dinâmicos por rota`

---

### 🟡 TASK-F07 — Adicionar interceptor global de 401 para sessão expirada
**Tipo:** feature | **Prioridade:** P1
**Arquivo(s):**
- `src/api.js`
- `src/contexts/AuthContext.tsx`

**Descrição:**
Token JWT expirado resulta em falhas silenciosas. Usuário continua "logado" mas operações não funcionam.

**Critérios de aceite:**
- [ ] Resposta 401 dispara logout automático
- [ ] Toast exibe "Sua sessão expirou. Faça login novamente."
- [ ] Redirecionamento para `/login`

**Commit:** `feat(front): interceptar 401 globalmente e redirecionar para login`

---

### 🟢 TASK-F08 — Implementar paginação na listagem de ideias
**Tipo:** feature | **Prioridade:** P2
**Arquivo(s):**
- `src/pages/IdeasListPage.tsx`
- `src/contexts/IdeasContext.tsx`

**Descrição:**
Backend terá paginação (TASK-007), mas o front carrega tudo de uma vez. Com crescimento, a lista se torna inutilizável.

**Critérios de aceite:**
- [ ] Listagem consome parâmetros `page` e `size`
- [ ] Controles de paginação exibidos
- [ ] Tamanho de página padrão: 20

**Commit:** `feat(front): implementar paginação na listagem de ideias`

---

## Visão Geral por Prioridade

### 🔴 P0 — Críticos (10 tasks)

| Task | Título | Escopo |
|---|---|---|
| TASK-001 | Proteger endpoint de registro de admin | Auth API |
| TASK-002 | Carregar authorities do banco | Auth API |
| TASK-003 | Remover fallback JWT hardcoded | Auth + Ideas + Profile |
| TASK-003-B | Remover application.properties do git | Todos |
| TASK-004 | Corrigir CORS nas APIs | Ideas + Profile |
| TASK-R01 | Remover JWT secret hardcoded | Reputation |
| TASK-R02 | Proteger endpoints de eventos XP | Reputation |
| TASK-T01 | Remover JWT secret hardcoded | Trending |
| TASK-T02 | Proteger endpoints de bump | Trending |
| TASK-F01 | Externalizar URLs de API | Front-end |

### 🟡 P1 — Importantes (16 tasks)

| Task | Título | Escopo |
|---|---|---|
| TASK-005 | Sanitização HTML no Ideas Hub | Ideas API |
| TASK-006 | Remover authorId do IdeaDTO | Ideas API |
| TASK-007 | Paginação em getAllIdeas | Ideas API |
| TASK-008 | RuntimeException → ResponseStatusException | Ideas API |
| TASK-009 | Path traversal canônico | Profile API |
| TASK-R03 | Race condition no XP | Reputation |
| TASK-R04 | userEmail → userId | Reputation |
| TASK-R05 | GlobalExceptionHandler | Reputation |
| TASK-T03 | Rejeitar delta negativo | Trending |
| TASK-T04 | Fixar timezone UTC | Trending |
| TASK-T05 | CORS na Trending API | Trending |
| TASK-T06 | Authorities no SecurityFilter | Trending |
| TASK-F02 | Logout blacklista token | Front-end |
| TASK-F03 | Dialog de confirmação de deleção | Front-end |
| TASK-F04 | Integração com Profile API | Front-end |
| TASK-F05 | ViewIdeaPage busca da API | Front-end |
| TASK-F06 | lang e títulos dinâmicos | Front-end |
| TASK-F07 | Interceptor 401 global | Front-end |

### 🟢 P2 — Melhorias (5 tasks)

| Task | Título | Escopo |
|---|---|---|
| TASK-010 | Atualizar Spring Boot 3.5.7 | Ideas API |
| TASK-011 | Audit logging | Todos |
| TASK-R06 | Normalizar contrato REST de eventos | Reputation |
| TASK-T07 | Adotar TrendingResponseDTO | Trending |
| TASK-T08 | Alinhar java-jwt 4.5.0 | Reputation |
| TASK-F08 | Paginação no front | Front-end |
