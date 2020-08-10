package org.example.config;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
public class SshConfig {
    private String ip;
    private String username;
    private String password;

    private String charset = Charset.defaultCharset().toString();
    private static final int TIME_OUT = 1000 * 5 * 60;

    private Connection conn;

    public SshConfig(String ip, String username, String password) {
        this.ip = ip;
        this.username = username;
        this.password = password;
    }

    /**
     * 登录指远程服务器
     * @return
     * @throws IOException
     */
    private boolean login() throws IOException {
        conn = new Connection(ip);
        conn.connect();
        boolean b=conn.authenticateWithPassword(username, password);
        return b;
    }

    public int exec(String shell) throws Exception {
        int ret = -1;
        try {
            if (login()) {
                Session session = conn.openSession();
                session.execCommand(shell);
                session.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT);
                InputStream stdout = session.getStdout();
                ret = session.getExitStatus();
            } else {
                throw new Exception("登录远程机器失败" + ip); // 自定义异常类 实现略
            }
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return ret;
    }

    public static void main(String [] args){
        try {
            SshConfig sshClient = new SshConfig("172.16.8.19", "jiangbingren", "jiang123");
            sshClient.login();
            String param1 = "sudo /usr/local/src/fabric-samples/test-network/network.sh createChannel -c myll";
            sshClient.exec(param1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
