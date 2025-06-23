package dev.ch3cooh0.jfuncloc.callgraph;

/**
 * プログラム終了処理を抽象化するインターフェース。
 */
public interface ExitHandler {
    void exit(int status);
}

/**
 * 本番用のSystem.exitを呼び出す実装。
 */
class SystemExitHandler implements ExitHandler {
    @Override
    public void exit(int status) {
        System.exit(status);
    }
}

/**
 * テスト用の何もしない実装。
 */
class NoOpExitHandler implements ExitHandler {
    private int lastExitCode = 0;
    
    @Override
    public void exit(int status) {
        this.lastExitCode = status;
        // System.exitは呼ばずに、単に終了コードを記録
    }
    
    public int getLastExitCode() {
        return lastExitCode;
    }
} 