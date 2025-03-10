package cn.hippo4j.common.web.executor;

import cn.hippo4j.common.model.PoolParameter;
import cn.hippo4j.common.model.PoolParameterInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.WebServer;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tomcat web thread pool handler.
 *
 * @author chen.ma
 * @date 2022/1/19 20:57
 */
@Slf4j
@AllArgsConstructor
public class TomcatWebThreadPoolHandler extends AbstractWebThreadPoolService {

    private final AtomicBoolean cacheFlag = new AtomicBoolean(Boolean.FALSE);

    private static String EXCEPTION_MESSAGE;

    @Override
    protected Executor getWebThreadPoolByServer(WebServer webServer) {
        if (cacheFlag.get()) {
            log.warn("Exception getting Tomcat thread pool. Exception message :: {}", EXCEPTION_MESSAGE);
            return null;
        }

        Executor tomcatExecutor = null;
        try {
            tomcatExecutor = ((TomcatWebServer) webServer).getTomcat().getConnector().getProtocolHandler().getExecutor();
        } catch (Exception ex) {
            cacheFlag.set(Boolean.TRUE);
            EXCEPTION_MESSAGE = ex.getMessage();
            log.error("Failed to get Tomcat thread pool. Message :: {}", EXCEPTION_MESSAGE);
        }

        return tomcatExecutor;
    }

    @Override
    public PoolParameter getWebThreadPoolParameter() {
        PoolParameterInfo parameterInfo = null;
        try {
            parameterInfo = new PoolParameterInfo();
            ThreadPoolExecutor tomcatExecutor = (ThreadPoolExecutor) executor;
            int minThreads = tomcatExecutor.getCorePoolSize();
            int maxThreads = tomcatExecutor.getMaximumPoolSize();
            long keepAliveTime = tomcatExecutor.getKeepAliveTime(TimeUnit.SECONDS);

            parameterInfo.setCoreSize(minThreads);
            parameterInfo.setMaxSize(maxThreads);
            parameterInfo.setKeepAliveTime((int) keepAliveTime);
        } catch (Exception ex) {
            log.error("Failed to get the tomcat thread pool parameter.", ex);
        }

        return parameterInfo;
    }

    @Override
    public void updateWebThreadPool(PoolParameterInfo poolParameterInfo) {
        try {
            ThreadPoolExecutor tomcatExecutor = (ThreadPoolExecutor) executor;
            int originalCoreSize = tomcatExecutor.getCorePoolSize();
            int originalMaximumPoolSize = tomcatExecutor.getMaximumPoolSize();
            long originalKeepAliveTime = tomcatExecutor.getKeepAliveTime(TimeUnit.SECONDS);

            tomcatExecutor.setCorePoolSize(poolParameterInfo.getCoreSize());
            tomcatExecutor.setMaximumPoolSize(poolParameterInfo.getMaxSize());
            tomcatExecutor.setKeepAliveTime(poolParameterInfo.getKeepAliveTime(), TimeUnit.SECONDS);
            log.info(
                    "[TOMCAT] Changed web thread pool. " +
                            "\n    coreSize :: [{}]" +
                            "\n    maxSize :: [{}]" +
                            "\n    keepAliveTime :: [{}]",
                    String.format("%s => %s", originalCoreSize, poolParameterInfo.getCoreSize()),
                    String.format("%s => %s", originalMaximumPoolSize, poolParameterInfo.getMaxSize()),
                    String.format("%s => %s", originalKeepAliveTime, poolParameterInfo.getKeepAliveTime())
            );
        } catch (Exception ex) {
            log.error("Failed to modify the Tomcat thread pool parameter.", ex);
        }
    }

}
