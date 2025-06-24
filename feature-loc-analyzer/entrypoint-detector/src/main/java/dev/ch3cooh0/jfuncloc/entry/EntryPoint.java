package dev.ch3cooh0.jfuncloc.entry;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * 機能のエントリーポイントとなるメソッドに付与するアノテーション。
 * 
 * <p>このアノテーションを使用することで、特定の機能に属するメソッドを
 * プログラマティックに識別できます。LOC計測時に、このアノテーションが
 * 付与されたメソッドを起点として、関連する関数・クラスを特定します。
 * 
 * <h3>使用例</h3>
 * <pre>
 * &#64;RestController
 * public class UserController {
 * 
 *     &#64;EntryPoint("user-management")
 *     &#64;PostMapping("/users")
 *     public ResponseEntity&lt;User&gt; createUser(&#64;RequestBody User user) {
 *         // ユーザー作成処理
 *         return ResponseEntity.ok(userService.createUser(user));
 *     }
 * 
 *     &#64;EntryPoint("user-management")
 *     &#64;PutMapping("/users/{id}")
 *     public ResponseEntity&lt;User&gt; updateUser(&#64;PathVariable Long id, &#64;RequestBody User user) {
 *         // ユーザー更新処理
 *         return ResponseEntity.ok(userService.updateUser(id, user));
 *     }
 * }
 * </pre>
 * 
 * <h3>SpringFrameworkとの連携</h3>
 * <p>このアノテーションはSpringFrameworkのアノテーション（@RestController、@Service等）
 * と組み合わせて使用できます。Spring Bootアプリケーションでの機能単位LOC計測に適用可能です。
 * 
 * @author JFuncLOC
 * @version 1.0
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EntryPoint {
    /**
     * この機能の識別子を指定します。
     * 
     * <p>同じ機能に属する複数のエントリーポイントには、同じ値を指定してください。
     * この値は機能定義ファイル（YAML/JSON）のキーと対応させることも可能です。
     * 
     * @return 機能の識別子
     */
    String value();
} 