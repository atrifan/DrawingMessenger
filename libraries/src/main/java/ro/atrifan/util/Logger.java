package ro.atrifan.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by alexandru.trifan on 27.04.2016.
 */
public class Logger {

    private static Map<Class, org.apache.log4j.Logger> mapping = new HashMap<>();
    private static Map<Class, Logger> instances = new HashMap<>();
    enum LogType {
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL
    };

    class LoggingTask {
        Throwable t = null;
        Object message = "";
        LogType logType = LogType.DEBUG;
    }
    class ParallelLogger implements Runnable {

        private org.apache.log4j.Logger logger;
        private LoggingTask loggingTask;
        public ParallelLogger(Class className, LoggingTask loggingTask) {
            logger = mapping.get(className);
            this.loggingTask = loggingTask;
        }

        @Override
        public void run() {
            switch(this.loggingTask.logType) {
                case DEBUG:
                    logger.debug(this.loggingTask.message, this.loggingTask.t);
                    break;
                case WARN:
                    logger.warn(this.loggingTask.message, this.loggingTask.t);
                    break;
                case ERROR:
                    logger.error(this.loggingTask.message, this.loggingTask.t);
                    break;
                case INFO:
                    logger.info(this.loggingTask.message, this.loggingTask.t);
                    break;
                case FATAL:
                    logger.fatal(this.loggingTask.message, this.loggingTask.t);
                    break;
                default:
                    break;
            }
        }
    }


    public static Logger getLogger(Class className) {
        if(!mapping.containsKey(className)) {
            mapping.put(className, org.apache.log4j.Logger.getLogger(className));
            instances.put(className, new Logger(className));
        }

        return instances.get(className);
    }

    private ExecutorService threadPoolExecutor;
    private Class className;
    public Logger(Class className) {
         threadPoolExecutor = Executors.newFixedThreadPool(20,
                 new ThreadFactory() {
                     public Thread newThread(Runnable r) {
                         Thread t = Executors.defaultThreadFactory().newThread(r);
                         t.setDaemon(true);
                         return t;
                     }
                 });
         this.className = className;
    }

    public void debug(Object message) {
        LoggingTask loggingTask = new LoggingTask();
        loggingTask.logType = LogType.DEBUG;
        loggingTask.message = message;
        //threadPoolExecutor.submit(new ParallelLogger(this.className, loggingTask));
    }

    public void info(Object message) {
        LoggingTask loggingTask = new LoggingTask();
        loggingTask.logType = LogType.INFO;
        loggingTask.message = message;
        //threadPoolExecutor.submit(new ParallelLogger(this.className, loggingTask));
    }

    public void warn(Object message) {
        LoggingTask loggingTask = new LoggingTask();
        loggingTask.logType = LogType.WARN;
        loggingTask.message = message;
        threadPoolExecutor.submit(new ParallelLogger(this.className, loggingTask));
    }

    public void warn(Object message, Throwable t) {
        LoggingTask loggingTask = new LoggingTask();
        loggingTask.logType = LogType.DEBUG;
        loggingTask.message = message;
        loggingTask.t = t;
        threadPoolExecutor.submit(new ParallelLogger(this.className, loggingTask));
    }

    public void error(Object message) {
        LoggingTask loggingTask = new LoggingTask();
        loggingTask.logType = LogType.ERROR;
        loggingTask.message = message;
        threadPoolExecutor.submit(new ParallelLogger(this.className, loggingTask));
    }

    public void error(Object message, Throwable t) {
        LoggingTask loggingTask = new LoggingTask();
        loggingTask.logType = LogType.ERROR;
        loggingTask.message = message;
        loggingTask.t = t;
        threadPoolExecutor.submit(new ParallelLogger(this.className, loggingTask));
    }

    public void fatal(Object message) {
        LoggingTask loggingTask = new LoggingTask();
        loggingTask.logType = LogType.FATAL;
        loggingTask.message = message;
        threadPoolExecutor.submit(new ParallelLogger(this.className, loggingTask));
    }

    public void fatal(Object message, Throwable t) {
        LoggingTask loggingTask = new LoggingTask();
        loggingTask.logType = LogType.FATAL;
        loggingTask.message = message;
        loggingTask.t = t;
        threadPoolExecutor.submit(new ParallelLogger(this.className, loggingTask));
    }
}