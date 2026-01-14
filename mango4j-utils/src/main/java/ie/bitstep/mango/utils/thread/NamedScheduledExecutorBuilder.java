package ie.bitstep.mango.utils.thread;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.commons.lang3.time.DurationUtils.isPositive;

/**
 * Builder for a ScheduledExecutorService backed by ScheduledThreadPoolExecutor,
 * with named threads and common tunables.
 */
public final class NamedScheduledExecutorBuilder {

	private int poolSize = Runtime.getRuntime().availableProcessors();
	private String threadNamePrefix = "scheduled";
	private boolean daemon = false;
	private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = null;
	private boolean removeOnCancelPolicy = true;
	private long keepAliveTime = 0L; // seconds
	private boolean allowCoreThreadTimeout = false;
	private RejectedExecutionHandler rejectedExecutionHandler = null;

	private NamedScheduledExecutorBuilder() {
	}

	public static NamedScheduledExecutorBuilder builder() {
		return new NamedScheduledExecutorBuilder();
	}

	public NamedScheduledExecutorBuilder poolSize(int poolSize) {
		if (poolSize < 1) {
			throw new IllegalArgumentException("poolSize must be > 0");
		}
		this.poolSize = poolSize;
		return this;
	}

	public NamedScheduledExecutorBuilder threadNamePrefix(String prefix) {
		if (prefix == null || prefix.isBlank()) {
			throw new IllegalArgumentException("threadNamePrefix must be non-blank");
		}
		this.threadNamePrefix = prefix;
		return this;
	}

	public NamedScheduledExecutorBuilder daemon(boolean daemon) {
		this.daemon = daemon;
		return this;
	}

	public NamedScheduledExecutorBuilder uncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler) {
		this.uncaughtExceptionHandler = handler;
		return this;
	}

	public NamedScheduledExecutorBuilder removeOnCancelPolicy(boolean removeOnCancelPolicy) {
		this.removeOnCancelPolicy = removeOnCancelPolicy;
		return this;
	}

	public NamedScheduledExecutorBuilder keepAliveTime(long seconds) {
		if (seconds < 0) {
			throw new IllegalArgumentException("keepAliveTime must be >= 0");
		}
		this.keepAliveTime = seconds;
		return this;
	}

	public NamedScheduledExecutorBuilder allowCoreThreadTimeout(boolean allow) {
		this.allowCoreThreadTimeout = allow;
		return this;
	}

	public NamedScheduledExecutorBuilder rejectedExecutionHandler(RejectedExecutionHandler handler) {
		this.rejectedExecutionHandler = Objects.requireNonNull(handler, "handler");
		return this;
	}

	/**
	 * Build and return a ScheduledExecutorService configured per builder settings.
	 */
	public ScheduledExecutorService build() {
		ThreadFactory tf = new NamedThreadFactory(threadNamePrefix, daemon, uncaughtExceptionHandler);

		ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(poolSize, tf);

		// Optional tuning
		exec.setRemoveOnCancelPolicy(removeOnCancelPolicy);

		if (isPositive(Duration.ofSeconds(keepAliveTime))) {
			exec.setKeepAliveTime(keepAliveTime, TimeUnit.SECONDS);
		}

		exec.allowCoreThreadTimeOut(allowCoreThreadTimeout);

		if (rejectedExecutionHandler != null) {
			exec.setRejectedExecutionHandler(rejectedExecutionHandler);
		}

		return exec;
	}

	/**
	 * ThreadFactory that names threads with a prefix and index, optional daemon flag and handler.
	 */
	public static final class NamedThreadFactory implements ThreadFactory {
		private final String namePrefix;
		private final boolean daemon;
		private final Thread.UncaughtExceptionHandler handler;
		private final AtomicInteger counter = new AtomicInteger(1);

		public NamedThreadFactory(String namePrefix, boolean daemon, Thread.UncaughtExceptionHandler handler) {
			if (namePrefix == null || namePrefix.isBlank()) {
				throw new IllegalArgumentException("namePrefix must be non-blank");
			}
			this.namePrefix = namePrefix.endsWith("-") ? namePrefix : (namePrefix + "-");
			this.daemon = daemon;
			this.handler = handler;
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, namePrefix + counter.getAndIncrement());
			t.setDaemon(daemon);
			if (handler != null) {
				t.setUncaughtExceptionHandler(handler);
			}
			return t;
		}
	}
}