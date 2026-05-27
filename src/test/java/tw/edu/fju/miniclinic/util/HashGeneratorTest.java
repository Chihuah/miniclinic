package tw.edu.fju.miniclinic.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * 【工具用途，非功能測試】
 *
 * 執行方式（在 miniclinic/ 目錄下）：
 *   ./mvnw test -Dtest=HashGeneratorTest -q
 *
 * 將 console 輸出的 INSERT 區段複製，貼入 src/main/resources/data.sql，
 * 取代佔位符的 password_hash 欄位值。
 *
 * 上課示範重點：
 *   1. 每次執行，5 個雜湊值都不同（BCrypt 每次 salt 不同）
 *   2. 但 BCrypt.checkpw("pass1234", hash) 對所有這些雜湊都回傳 true
 *   3. 這就是為什麼 data.sql 必須「先跑一次、固定下來」而不是每次動態產生
 */
class HashGeneratorTest {

    private static final String PASSWORD = "pass1234";

    private static final String[] DOCTOR_IDS   = {"D001", "D002", "D003", "D004", "D005"};
    private static final String[] NAMES        = {"陳志明醫師", "林佩君醫師", "王建華醫師", "李美玲醫師", "張雅筑醫師"};
    private static final String[] DEPARTMENTS  = {"家醫科", "內科", "復健科", "小兒科", "身心科"};
    private static final String[] SPECIALTIES  = {
        "一般內科、慢性病管理",
        "心臟血管、高血壓",
        "運動傷害、脊椎復健",
        "兒童感冒、疫苗接種",
        "焦慮、失眠、情緒調適"
    };

    @Test
    void generateHashesForDataSql() {
        System.out.println();
        System.out.println("=== 示範：同一明文，每次雜湊結果不同 ===");
        String h1 = BCrypt.hashpw(PASSWORD, BCrypt.gensalt());
        String h2 = BCrypt.hashpw(PASSWORD, BCrypt.gensalt());
        System.out.println("第 1 次：" + h1);
        System.out.println("第 2 次：" + h2);
        System.out.println("兩者相同？" + h1.equals(h2));                      // false
        System.out.println("checkpw 第 1 次：" + BCrypt.checkpw(PASSWORD, h1)); // true
        System.out.println("checkpw 第 2 次：" + BCrypt.checkpw(PASSWORD, h2)); // true

        System.out.println();
        System.out.println("=== 複製以下 INSERT 到 data.sql（取代舊的 doctor INSERT）===");
        System.out.println("INSERT OR IGNORE INTO doctor (doctor_id, name, department, specialty, password_hash) VALUES");
        for (int i = 0; i < DOCTOR_IDS.length; i++) {
            String hash = BCrypt.hashpw(PASSWORD, BCrypt.gensalt());
            String comma = (i < DOCTOR_IDS.length - 1) ? "," : ";";
            System.out.printf("  ('%s', '%s', '%s', '%s', '%s')%s%n",
                DOCTOR_IDS[i], NAMES[i], DEPARTMENTS[i], SPECIALTIES[i], hash, comma);
        }
        System.out.println();
    }
}
