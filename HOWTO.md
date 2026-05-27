# Ch09-D｜Session、表單驗證與醫療資料安全

## 本週目標

在 Week 3 的基礎上，替 MiniClinic 加入**醫師登入機制**與基本的存取控制：

- 醫師可以登入、登出
- 登入後進入 Dashboard，只看到**自己的**當日掛號
- 醫師可以把自己的掛號標記為**取消**
- 未登入使用者無法訪問 Dashboard（自動導回登入頁）
- 登入、掛號表單加上輸入驗證
- 醫師可以修改自己的密碼

> ⚠️ **第一次啟動前請刪除 `miniclinic.db`**  
> Week 4 在 `doctor` 資料表新增了 `password_hash` 欄位。若沿用 Week 3 的資料庫，  
> SQLite 的 schema 差異可能導致啟動失敗。刪除後重新啟動，`data.sql` 會自動重建資料。

---

## 這週新增／修改了什麼

相較於 Week 3，Week 4 的改動：

| 檔案 | 說明 |
|---|---|
| `model/Doctor.java` | 加入 `passwordHash` 欄位與 `@JsonIgnore` |
| `model/LoginForm.java` | 登入表單 POJO，附帶 `@NotBlank`、`@Pattern` 驗證 |
| `model/AppointmentForm.java` | 掛號表單加上病歷號格式驗證 `TEST\d{5}` |
| **`model/PasswordForm.java`** | 修改密碼表單 POJO（舊密碼、新密碼、確認密碼） |
| `controller/LoginController.java` | `GET/POST /login`、`POST /logout` |
| `controller/DashboardController.java` | `GET /dashboard`（只顯示自己的當日掛號） |
| `controller/AppointmentApiController.java` | 新增 `PUT /api/appointments/{id}/status`（只能改自己的掛號） |
| **`controller/PasswordController.java`** | `GET/POST /password`（修改密碼，需登入） |
| `interceptor/LoginRequiredInterceptor.java` | 實作 `HandlerInterceptor`，未登入就攔截 |
| `config/WebConfig.java` | 註冊 Interceptor 並指定受保護路徑 |
| `templates/login.html` | 登入頁面 |
| **`templates/dashboard.html`** | Dashboard 頁，含「取消」按鈕與 JavaScript fetch |
| `templates/appointment-new.html` | 掛號表單，加上錯誤提示 `th:errors` |
| **`templates/password.html`** | 修改密碼頁面 |
| `resources/data.sql` | 醫師資料加入 BCrypt 雜湊的預設密碼（`pass1234`） |
| `pom.xml` | 新增 `spring-security-crypto`、`spring-boot-starter-validation` |

粗體為本次 Part B 新增的檔案。

---

## 你會看到什麼

啟動後可訪問：

| URL | 預期結果 |
|---|---|
| `http://localhost:8080/` | 首頁（導覽列含「醫師登入」連結） |
| `http://localhost:8080/login` | 登入表單（未登入時也可訪問） |
| `http://localhost:8080/dashboard` | 未登入 → 自動跳回 `/login` |
| `http://localhost:8080/dashboard` | 登入後 → 顯示自己的當日掛號 |
| `http://localhost:8080/password` | 未登入 → 自動跳回 `/login` |
| `http://localhost:8080/password` | 登入後 → 修改密碼表單 |
| `PUT /api/appointments/{id}/status` | 未登入 → 401；改別人的掛號 → 403 |

預設帳密（所有醫師）：
- 醫師編號：`D001` ~ `D005`
- 密碼：`pass1234`

---

## 環境需求

- **Java 17 JDK**（Eclipse Temurin，https://adoptium.net）
- **VS Code + Extension Pack for Java**
- **不需要另外安裝 Maven**（專案內含 Maven Wrapper）

---

## 怎麼跑

### Windows（命令提示字元）

```cmd
mvnw.cmd spring-boot:run
```

### Mac / Linux（Terminal）

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

啟動成功後，Terminal 會顯示類似：

```
Started MiniclinicApplication in X.XXX seconds
```

打開瀏覽器訪問 `http://localhost:8080/` 即可。按 `Ctrl+C` 停止服務。

---

## 專案結構

```
miniclinic/
├── pom.xml
├── .gitignore
├── mvnw / mvnw.cmd
├── api-tests.http
├── HOWTO.md
└── src/
    ├── main/
    │   ├── java/tw/edu/fju/miniclinic/
    │   │   ├── MiniclinicApplication.java
    │   │   ├── config/
    │   │   │   └── WebConfig.java               ← 本週新增
    │   │   ├── interceptor/
    │   │   │   └── LoginRequiredInterceptor.java ← 本週新增
    │   │   ├── controller/
    │   │   │   ├── HealthController.java
    │   │   │   ├── HomeController.java
    │   │   │   ├── DoctorPageController.java
    │   │   │   ├── DoctorApiController.java
    │   │   │   ├── PatientPageController.java
    │   │   │   ├── PatientApiController.java
    │   │   │   ├── AppointmentController.java
    │   │   │   ├── AppointmentPageController.java
    │   │   │   ├── AppointmentApiController.java ← 本週修改（新增 PUT status）
    │   │   │   ├── StatsController.java
    │   │   │   ├── LoginController.java          ← 本週新增
    │   │   │   ├── DashboardController.java      ← 本週新增
    │   │   │   └── PasswordController.java       ← 本週新增（Part B）
    │   │   └── model/
    │   │       ├── Doctor.java                   ← 本週修改（加 passwordHash）
    │   │       ├── DoctorRepository.java
    │   │       ├── Patient.java
    │   │       ├── PatientRepository.java
    │   │       ├── Appointment.java
    │   │       ├── AppointmentRepository.java
    │   │       ├── AppointmentForm.java          ← 本週修改（加驗證）
    │   │       ├── LoginForm.java                ← 本週新增
    │   │       ├── PasswordForm.java             ← 本週新增（Part B）
    │   │       └── LocalDateConverter.java
    │   └── resources/
    │       ├── application.properties
    │       ├── data.sql                          ← 本週修改（加密碼雜湊）
    │       └── templates/
    │           ├── home.html
    │           ├── doctors.html
    │           ├── doctor-detail.html
    │           ├── doctor-not-found.html
    │           ├── patients.html
    │           ├── appointments.html
    │           ├── appointment-new.html          ← 本週修改（加驗證提示）
    │           ├── appointment-result.html
    │           ├── stats.html
    │           ├── login.html                    ← 本週新增
    │           ├── dashboard.html                ← 本週新增（Part B 加取消鈕）
    │           └── password.html                 ← 本週新增（Part B）
    └── test/
        └── java/tw/edu/fju/miniclinic/
            ├── MiniclinicApplicationTests.java
            └── util/HashGeneratorTest.java
```

---

## 關鍵程式碼解說

### 1. Session 登入流程

登入成功後，把醫師 ID 存進 Session；後續每次請求就讀這個 Session 來判斷「是否已登入」：

```java
// LoginController.java — 登入成功
session.setAttribute("loggedInDoctorId", doctor.getDoctorId());

// DashboardController.java — 讀取登入者身份
String doctorId = (String) session.getAttribute("loggedInDoctorId");
```

### 2. Interceptor 統一保護路由

不用在每個 Controller 都寫「沒登入就跳轉」，由 `LoginRequiredInterceptor` 統一攔截：

```java
// preHandle() 回傳 false → 請求不進入 Controller
if (loggedIn == null) {
    response.sendRedirect("/login");   // 頁面請求 → 重導
    return false;
}
return true;  // 放行
```

在 `WebConfig.java` 指定受保護路徑：

```java
registry.addInterceptor(loginInterceptor)
    .addPathPatterns("/dashboard", "/dashboard/**", "/password",
                     "/api/auth/me", "/api/appointments/*/status")
    .excludePathPatterns("/login", "/logout");
```

### 3. Bean Validation（表單驗證）

在 Form 類別的欄位加上注解宣告規則：

```java
// LoginForm.java
@NotBlank(message = "請輸入醫師編號")
@Pattern(regexp = "D\\d{3}", message = "醫師編號格式錯誤")
private String doctorId;

// PasswordForm.java
@Size(min = 8, message = "新密碼至少 8 碼")
private String newPassword;
```

Controller 加 `@Valid` 啟動驗證，`BindingResult` 必須緊接在後：

```java
public String login(
        @Valid @ModelAttribute("loginForm") LoginForm form,
        BindingResult result, ...) {
    if (result.hasErrors()) {
        return "login";  // 回去顯示錯誤訊息
    }
}
```

Thymeleaf 模板顯示錯誤：

```html
<div class="field-error"
     th:if="${#fields.hasErrors('doctorId')}"
     th:errors="*{doctorId}">
</div>
```

### 4. Part B：取消按鈕 + JavaScript fetch

Dashboard 表格中，只有 `BOOKED` 狀態的掛號才顯示「取消」按鈕：

```html
<button class="btn-cancel"
        th:if="${appt.status == 'BOOKED'}"
        th:data-id="${appt.apptId}"
        onclick="cancelAppointment(this)">取消</button>
```

JavaScript 用 `fetch` 送出 PUT 請求，成功後重新整理頁面：

```javascript
function cancelAppointment(btn) {
    const apptId = btn.getAttribute('data-id');
    fetch('/api/appointments/' + apptId + '/status', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: 'CANCELLED' })
    })
    .then(function(response) {
        if (response.ok) {
            location.reload();
        }
    });
}
```

### 5. Part B：修改密碼

`PasswordController` 處理邏輯依序為：

1. Bean Validation（`@NotBlank`、`@Size(min=8)`）
2. 新密碼 == 確認密碼
3. `BCrypt.checkpw(oldPassword, storedHash)` 驗證舊密碼
4. `BCrypt.hashpw(newPassword, BCrypt.gensalt())` 雜湊新密碼後存入 DB

### 6. 401 vs 403 的差別

| 狀態碼 | 意義 | 本專案觸發時機 |
|---|---|---|
| **401** Unauthorized | 你沒登入 | 未登入呼叫受保護 API（Interceptor 回傳） |
| **403** Forbidden | 你有登入，但沒權限 | 醫師 D001 試圖修改 D002 的掛號 |

---

## 用 REST Client 測試 API

開啟 `api-tests.http`，點每個區塊上方的「Send Request」：

```http
### 登入（直接測試 API 層，不過 Session）
PUT http://localhost:8080/api/appointments/1/status
Content-Type: application/json

{"status": "COMPLETED"}

### 未登入呼叫受保護 API → 預期 401
```

> 注意：瀏覽器登入後的 Session Cookie 不會自動帶進 REST Client 的請求，  
> 因此測試 Session 相關功能建議直接在瀏覽器操作。

---

## 常見問題

**Q：登入成功但馬上又跳回 `/login`？**

A：`session.setAttribute` 和 `session.getAttribute` 的 key 字串要**完全一致**（含大小寫），確認 `LoginController` 和 `LoginRequiredInterceptor` 裡用的是同一個 key `"loggedInDoctorId"`。

**Q：BCrypt 每次雜湊同一個密碼，結果都不一樣，是 bug 嗎？**

A：這是正常的。BCrypt 每次會自動產生不同的 salt，所以雜湊值不同。驗證時要用 `BCrypt.checkpw(plainText, storedHash)`，而不是直接比對兩個雜湊字串。

**Q：表單送出後驗證錯誤訊息沒出現？**

A：依序確認：
1. Controller 方法參數有加 `@Valid` 嗎？
2. `BindingResult` 有緊接在 `@Valid` 參數後面嗎？
3. Thymeleaf 模板用的是 `*{fieldName}`（星號）而不是 `${fieldName}`（錢號）嗎？

**Q：`PUT /api/appointments/{id}/status` 回 404？**

A：HTTP Client 的 `Content-Type` 要設成 `application/json`，否則 Spring 無法解析 `@RequestBody`。

**Q：`PUT` 更新成功但 Dashboard 沒有變化？**

A：`fetch` 的回呼裡有沒有呼叫 `location.reload()`？確認 `response.ok` 判斷正確（HTTP 200 才是 ok）。

**Q：啟動時報錯 `no such column: password_hash`？**

A：刪除 `miniclinic.db` 後重啟，讓 Hibernate 用最新 schema 重建資料表並重跑 `data.sql`。

---

## 本週重點回顧

- **HTTP 無狀態**：每次請求都是全新的，`HttpSession` 是讓伺服器「記住你是誰」的機制
- **Cookie vs Session**：Session ID 存在 Cookie，實際資料存在伺服器端——使用者無法偽造
- **Interceptor**：在請求進入 Controller 前統一攔截，避免在每個 method 重複寫登入檢查
- **Bean Validation**：用注解宣告規則，`@Valid` + `BindingResult` 啟動，`th:errors` 顯示訊息
- **BCrypt**：密碼一定要雜湊後存，`checkpw` 驗證，永遠不比對明文
- **401 vs 403**：前者是「你是誰」的問題，後者是「你能不能做」的問題

---

## 繳交方式

將整個 `miniclinic` 資料夾壓縮為 zip 檔，繳交前請刪除：
- `target/`
- `miniclinic.db`

檔名：`學號_姓名_Ch09D.zip`，上傳至教學平台。
