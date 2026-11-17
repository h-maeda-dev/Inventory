// src/main/java/com/example/inventory/InventoryApplication.java
package com.example.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Bootアプリケーションの起動クラス。
 * プロジェクトのルートパッケージに配置され、@SpringBootApplicationにより
 * 必要なコンポーネントの自動スキャンと設定を行います。
 */
@SpringBootApplication
public class InventoryApplication {
    
    /**
     * アプリケーションのエントリーポイント。
     * Tomcatサーバーを組み込みモードで起動し、アプリケーションを実行します。
     *
     * @param args 起動時のコマンドライン引数
     */
    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}